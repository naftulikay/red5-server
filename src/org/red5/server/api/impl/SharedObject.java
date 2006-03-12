package org.red5.server.api.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ByteBuffer;
import org.red5.io.amf.Input;
import org.red5.io.amf.Output;
import org.red5.io.object.Deserializer;
import org.red5.server.api.Red5;
import org.red5.server.api.IConnection;
import org.red5.server.api.ISharedObject;
import org.red5.server.context.ZScope;
import org.red5.server.net.rtmp.Channel;
import org.red5.server.net.rtmp.RTMPConnection;
import org.red5.server.net.rtmp.message.Constants;
import org.red5.server.net.rtmp.message.SharedObjectEvent;
import org.red5.server.net.servlet.ServletUtils;
import org.red5.server.persistence.IPersistable;
import org.red5.server.persistence.IPersistentStorage;

public class SharedObject implements ISharedObject, Constants {

	protected static Log log =
        LogFactory.getLog(SharedObject.class.getName());

	public final static String PERSISTENT_ID_PREFIX = "_RED5_SO_";
	
	protected String name = "";
	protected IPersistentStorage storage = null;
	protected int version = 0;
	protected boolean persistent = false;
	protected AttributeStore data = new AttributeStore();
	protected HashSet connections = new HashSet();
	protected int updateCounter = 0;
	protected boolean modified = false;
	
	private org.red5.server.net.rtmp.message.SharedObject ownerMessage;
	private org.red5.server.net.rtmp.message.SharedObject syncMessage;

	public SharedObject() {
		this.ownerMessage = new org.red5.server.net.rtmp.message.SharedObject();
		this.ownerMessage.setName(name);
		this.ownerMessage.setTimestamp(0);
		this.ownerMessage.setType(persistent ? 2 : 0);
		
		this.syncMessage = new org.red5.server.net.rtmp.message.SharedObject();
		this.syncMessage.setName(name);
		this.syncMessage.setTimestamp(0);
		this.syncMessage.setType(persistent ? 2 : 0);
	}

	public SharedObject(String name, boolean persistent, IPersistentStorage storage) {
		this.name = name;
		this.persistent = persistent;
		this.storage = storage;
		
		this.ownerMessage = new org.red5.server.net.rtmp.message.SharedObject();
		this.ownerMessage.setName(name);
		this.ownerMessage.setTimestamp(0);
		this.ownerMessage.setType(persistent ? 2 : 0);
		
		this.syncMessage = new org.red5.server.net.rtmp.message.SharedObject();
		this.syncMessage.setName(name);
		this.syncMessage.setTimestamp(0);
		this.syncMessage.setType(persistent ? 2 : 0);
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean isPersistent() {
		return this.persistent;
	}
	
	private void sendUpdates() {
		if (!this.ownerMessage.getEvents().isEmpty()) {
			// Send update to "owner" of this update request
			this.ownerMessage.setSoId(this.version);
			this.ownerMessage.setSealed(false);
			Channel channel = ZScope.getChannel();
			if (channel != null) {
				channel.write(this.ownerMessage);
				log.debug("Owner: " + channel);
			} else
				log.warn("No channel found for owner changes!?");
			this.ownerMessage.getEvents().clear();
		}
		
		if (!this.syncMessage.getEvents().isEmpty()) {
			// Synchronize updates with all registered clients of this shared object
			IConnection conn = Red5.getConnectionLocal();
			this.syncMessage.setSoId(this.version);
			this.syncMessage.setSealed(false);
			// Acquire the packet, this will stop the data inside being released
			this.syncMessage.acquire();
			Iterator connections = this.connections.iterator();
			while (connections.hasNext()) {
				IConnection connection = (IConnection) connections.next();
				if (connection == conn) {
					// Don't re-send update to active client
					log.debug("Skipped " + connection);
					continue;
				}
				
				if (!(connection instanceof RTMPConnection)) {
					log.warn("Can't send sync message to unknown connection " + connection);
					continue;
				}
				
				Channel c = ((RTMPConnection) connection).getChannel((byte) 3);
				log.debug("Send to " + c);
				c.write(this.syncMessage);
				this.syncMessage.setSealed(false);
			}
			// After sending the packet down all the channels we can release the packet, 
			// which in turn will allow the data buffer to be released
			this.syncMessage.release();
			this.syncMessage.getEvents().clear();
		}
	}
	
	private void notifyModified() {
		if (this.updateCounter > 0)
			// we're inside a beginUpdate...endUpdate block
			return;
		
		if (this.modified)
			// The client sent at least one update -> increase version of SO
			this.updateVersion();
		
		if (this.modified && this.storage != null) {
			try {
				this.storage.storeObject((IPersistable)this);
			} catch (IOException e) {
				log.error("Could not store shared object.", e);
			}
		}
		
		this.sendUpdates();
	}
	
	public boolean hasAttribute(String name) {
		return this.data.hasAttribute(name);
	}
	
	public Set getAttributeNames() {
		return this.data.getAttributeNames();
	}
	
	public Object getAttribute(String name) {
		return this.data.getAttribute(name);
	}
	
	public boolean setAttribute(String name, Object value) {
		this.ownerMessage.addEvent(new SharedObjectEvent(SO_CLIENT_UPDATE_ATTRIBUTE, name, null));
		if (this.data.setAttribute(name, value)) {
			this.modified = true;
			// only sync if the attribute changed 
			this.syncMessage.addEvent(new SharedObjectEvent(SO_CLIENT_UPDATE_DATA, name, value));
			this.notifyModified();
			return true;
		} else {
			this.notifyModified();
			return false;
		}
	}
	
	public void setAttributes(Map values) {
		beginUpdate();
		Iterator it = values.keySet().iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			setAttribute(name, values.get(name));
		}
		endUpdate();
	}
	
	public void setAttributes(org.red5.server.api.IAttributeStore values) {
		beginUpdate();
		Iterator it = values.getAttributeNames().iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			setAttribute(name, values.getAttribute(name));
		}
		endUpdate();
	}
	
	public boolean removeAttribute(String name) {
		boolean result = this.data.removeAttribute(name);
		// Send confirmation to client
		this.ownerMessage.addEvent(new SharedObjectEvent(SO_CLIENT_DELETE_DATA, name, null));
		if (result) {
			this.modified = true;
			this.syncMessage.addEvent(new SharedObjectEvent(SO_CLIENT_DELETE_DATA, name, null));
		}
		this.notifyModified();
		return result;
	}
	
	public void sendMessage(String handler, List arguments) {
		this.ownerMessage.addEvent(new SharedObjectEvent(SO_CLIENT_SEND_MESSAGE, handler, arguments));
		this.syncMessage.addEvent(new SharedObjectEvent(SO_CLIENT_SEND_MESSAGE, handler, arguments));
	}
	
	public void setData(Map data) {
		this.data.removeAttributes();
		this.data.setAttributes(data);
		this.modified = false;
	}
	
	public Map getData() {
		Map result = new HashMap();
		Iterator it = this.data.getAttributeNames().iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			Object value = this.data.getAttribute(name);
			result.put(name, value);
		}
		
		return result;
	}
	
	public int getVersion() {
		return this.version;
	}
	
	private void updateVersion() {
		this.version += 1;
	}
	
	public void removeAttributes() {
		// TODO: there must be a direct way to clear the SO on the client side...
		Iterator keys = this.data.getAttributeNames().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			this.ownerMessage.addEvent(new SharedObjectEvent(SO_CLIENT_DELETE_DATA, key, null));
			this.syncMessage.addEvent(new SharedObjectEvent(SO_CLIENT_DELETE_DATA, key, null));
		}
		
		this.data.removeAttributes();
		this.modified = true;
		this.notifyModified();
	}
	
	public void register(IConnection connection) {
		this.connections.add(connection);
		
		// prepare response for new client
		this.ownerMessage.addEvent(new SharedObjectEvent(SO_CLIENT_INITIAL_DATA, null, null));
		if (!this.data.getAttributeNames().isEmpty())
			this.ownerMessage.addEvent(new SharedObjectEvent(SO_CLIENT_UPDATE_DATA, null, this.getData()));
		
		// we call notifyModified here to send response if we're not in a beginUpdate block
		this.notifyModified();
	}
	
	public void unregister(IConnection connection) {
		this.connections.remove(connection);
		if (!this.persistent && this.connections.isEmpty()) {
			log.info("Deleting shared object " + this.name + " because all clients disconnected.");
			this.data.removeAttributes();
			try {
				this.storage.removeObject(this.getPersistentId());
			} catch (IOException e) {
				log.error("Could not remove shared object.", e);
			}
		}
	}
	
	public HashSet getConnections() {
		return this.connections;
	}
	
	public void beginUpdate() {
		this.updateCounter += 1;
	}
	
	public void endUpdate() {
		this.updateCounter -= 1;
		
		if (this.updateCounter == 0)
			this.notifyModified();
	}
	
	public String getPersistentId() {
		return PERSISTENT_ID_PREFIX + this.getName();
	}
	
	public void serialize(OutputStream output) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(1024);
		buf.setAutoExpand(true);
		serialize(buf);
		buf.flip();
		ServletUtils.copy(buf.asInputStream(), output);
	}
	
	public void serialize(ByteBuffer output) throws IOException {
		Output out = new Output(output);
		out.writeString(this.getName());
		data.serialize(output);
	}
	
	public void deserialize(InputStream input) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(1024);
		buf.setAutoExpand(true);
		ServletUtils.copy(input, buf.asOutputStream());
		buf.flip();
		deserialize(buf);
	}
	
	public void deserialize(ByteBuffer input) throws IOException {
		Input in = new Input(input);
		Deserializer deserializer = new Deserializer();
		name = (String) deserializer.deserialize(in);
		persistent = true;
		data.deserialize(input);
		this.ownerMessage.setName(name);
		this.ownerMessage.setType(2);
		this.syncMessage.setName(name);
		this.syncMessage.setType(2);
	}
	
	public IPersistentStorage getStorage() {
		return storage;
	}
	
	public void setStorage(IPersistentStorage storage) {
		this.storage = storage;
	}
}
