package org.red5.server.so;

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
import org.red5.server.AttributeStore;
import org.red5.server.api.Red5;
import org.red5.server.api.IConnection;
import org.red5.server.api.so.ISharedObject;
import org.red5.server.net.rtmp.Channel;
import org.red5.server.net.rtmp.RTMPConnection;
import org.red5.server.net.rtmp.message.Constants;
import org.red5.server.net.rtmp.message.SharedObjectEvent;
import org.red5.server.net.servlet.ServletUtils;
import org.red5.server.persistence.IPersistable;
import org.red5.server.persistence.IPersistentStorage;

public class SharedObject implements IPersistable, Constants {

	protected static Log log = LogFactory.getLog(SharedObject.class.getName());

	public final static String PERSISTENT_ID_PREFIX = "_RED5_SO_";

	protected String name = "";

	protected IPersistentStorage storage = null;

	protected int version = 0;

	protected boolean persistent = false;

	protected AttributeStore data = new AttributeStore();

	protected HashSet<IConnection> connections = new HashSet<IConnection>();

	protected int updateCounter = 0;

	protected boolean modified = false;

	private org.red5.server.net.rtmp.message.SharedObject ownerMessage;

	private org.red5.server.net.rtmp.message.SharedObject syncMessage;

	public SharedObject() {
		ownerMessage = new org.red5.server.net.rtmp.message.SharedObject();
		ownerMessage.setName(name);
		ownerMessage.setTimestamp(0);
		ownerMessage.setType(persistent ? 2 : 0);

		syncMessage = new org.red5.server.net.rtmp.message.SharedObject();
		syncMessage.setName(name);
		syncMessage.setTimestamp(0);
		syncMessage.setType(persistent ? 2 : 0);
	}

	public SharedObject(String name, boolean persistent,
			IPersistentStorage storage) {
		name = name;
		persistent = persistent;
		storage = storage;

		ownerMessage = new org.red5.server.net.rtmp.message.SharedObject();
		ownerMessage.setName(name);
		ownerMessage.setTimestamp(0);
		ownerMessage.setType(persistent ? 2 : 0);

		syncMessage = new org.red5.server.net.rtmp.message.SharedObject();
		syncMessage.setName(name);
		syncMessage.setTimestamp(0);
		syncMessage.setType(persistent ? 2 : 0);
	}

	public String getName() {
		return name;
	}

	public boolean isPersistent() {
		return persistent;
	}

	private void sendUpdates() {
		if (!ownerMessage.getEvents().isEmpty()) {
			// Send update to "owner" of this update request
			ownerMessage.setSoId(version);
			ownerMessage.setSealed(false);

			IConnection conn = Red5.getConnectionLocal();
			Channel channel = ((RTMPConnection) conn).getChannel((byte) 3);
			
			if (channel != null) {
				channel.write(ownerMessage);
				log.debug("Owner: " + channel);
			} else
				log.warn("No channel found for owner changes!?");
			ownerMessage.getEvents().clear();
		}

		if (!syncMessage.getEvents().isEmpty()) {
			// Synchronize updates with all registered clients of this shared
			// object
			IConnection conn = Red5.getConnectionLocal();
			syncMessage.setSoId(version);
			syncMessage.setSealed(false);
			// Acquire the packet, this will stop the data inside being released
			syncMessage.acquire();
			
			for(IConnection connection : connections) {
				
				if (connection == conn) {
					// Don't re-send update to active client
					log.debug("Skipped " + connection);
					continue;
				}

				if (!(connection instanceof RTMPConnection)) {
					log.warn("Can't send sync message to unknown connection "
							+ connection);
					continue;
				}

				Channel c = ((RTMPConnection) connection).getChannel((byte) 3);
				log.debug("Send to " + c);
				c.write(syncMessage);
				syncMessage.setSealed(false);
			}
			// After sending the packet down all the channels we can release the
			// packet,
			// which in turn will allow the data buffer to be released
			syncMessage.release();
			syncMessage.getEvents().clear();
		}
	}

	private void notifyModified() {
		if (updateCounter > 0)
			// we're inside a beginUpdate...endUpdate block
			return;

		if (modified)
			// The client sent at least one update -> increase version of SO
			updateVersion();

		if (modified && storage != null) {
			try {
				storage.storeObject(this);
			} catch (IOException e) {
				log.error("Could not store shared object.", e);
			}
		}

		sendUpdates();
	}

	public boolean hasAttribute(String name) {
		return data.hasAttribute(name);
	}

	public Set getAttributeNames() {
		return data.getAttributeNames();
	}

	public Object getAttribute(String name) {
		return data.getAttribute(name);
	}

	public boolean setAttribute(String name, Object value) {
		ownerMessage.addEvent(new SharedObjectEvent(
				SO_CLIENT_UPDATE_ATTRIBUTE, name, null));
		if (data.setAttribute(name, value)) {
			modified = true;
			// only sync if the attribute changed
			syncMessage.addEvent(new SharedObjectEvent(
					SO_CLIENT_UPDATE_DATA, name, value));
			notifyModified();
			return true;
		} else {
			notifyModified();
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
		boolean result = data.removeAttribute(name);
		// Send confirmation to client
		ownerMessage.addEvent(new SharedObjectEvent(SO_CLIENT_DELETE_DATA,
				name, null));
		if (result) {
			modified = true;
			syncMessage.addEvent(new SharedObjectEvent(
					SO_CLIENT_DELETE_DATA, name, null));
		}
		notifyModified();
		return result;
	}

	public void sendMessage(String handler, List arguments) {
		ownerMessage.addEvent(new SharedObjectEvent(
				SO_CLIENT_SEND_MESSAGE, handler, arguments));
		syncMessage.addEvent(new SharedObjectEvent(SO_CLIENT_SEND_MESSAGE,
				handler, arguments));
	}

	public void setData(Map data) {
		this.data.removeAttributes();
		this.data.setAttributes(data);
		modified = false;
	}

	public Map<String, Object> getData() {
		Map<String, Object> result = new HashMap<String, Object>();
		Iterator<String> it = data.getAttributeNames().iterator();
		while (it.hasNext()) {
			String name = it.next();
			Object value = data.getAttribute(name);
			result.put(name, value);
		}
		return result;
	}

	public int getVersion() {
		return version;
	}

	private void updateVersion() {
		version += 1;
	}

	public void removeAttributes() {
		// TODO: there must be a direct way to clear the SO on the client
		// side...
		Iterator keys = data.getAttributeNames().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			ownerMessage.addEvent(new SharedObjectEvent(
					SO_CLIENT_DELETE_DATA, key, null));
			syncMessage.addEvent(new SharedObjectEvent(
					SO_CLIENT_DELETE_DATA, key, null));
		}

		data.removeAttributes();
		modified = true;
		notifyModified();
	}

	public void register(IConnection connection) {
		connections.add(connection);

		// prepare response for new client
		ownerMessage.addEvent(new SharedObjectEvent(
				SO_CLIENT_INITIAL_DATA, null, null));
		if (!data.getAttributeNames().isEmpty())
			ownerMessage.addEvent(new SharedObjectEvent(
					SO_CLIENT_UPDATE_DATA, null, getData()));

		// we call notifyModified here to send response if we're not in a
		// beginUpdate block
		notifyModified();
	}

	public void unregister(IConnection connection) {
		connections.remove(connection);
		if (!persistent && connections.isEmpty()) {
			log.info("Deleting shared object " + name
					+ " because all clients disconnected.");
			data.removeAttributes();
			try {
				storage.removeObject(getPersistentId());
			} catch (IOException e) {
				log.error("Could not remove shared object.", e);
			}
		}
	}

	public HashSet getConnections() {
		return connections;
	}

	public void beginUpdate() {
		updateCounter += 1;
	}

	public void endUpdate() {
		updateCounter -= 1;

		if (updateCounter == 0)
			notifyModified();
	}

	public String getPersistentId() {
		return PERSISTENT_ID_PREFIX + getName();
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
		out.writeString(getName());
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
		ownerMessage.setName(name);
		ownerMessage.setType(2);
		syncMessage.setName(name);
		syncMessage.setType(2);
	}

	public IPersistentStorage getStorage() {
		return storage;
	}

	public void setStorage(IPersistentStorage storage) {
		storage = storage;
	}
}
