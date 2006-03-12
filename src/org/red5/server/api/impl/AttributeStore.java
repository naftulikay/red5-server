package org.red5.server.api.impl;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.mina.common.ByteBuffer;
import org.red5.io.amf.Input;
import org.red5.io.amf.Output;
import org.red5.io.object.Deserializer;
import org.red5.io.object.Serializer;

import org.red5.server.api.IAttributeStore;

import org.red5.server.net.servlet.ServletUtils;
import org.red5.server.persistence.IPersistable;
import org.red5.server.persistence.IPersistentStorage;

public class AttributeStore implements IAttributeStore, IPersistable {

	private HashMap attributes = new HashMap();
	private IPersistentStorage storage = null;
	private String persistentId = null;
	
	public AttributeStore() {
		// Object is not associated with a persistence storage
	}
	
	public AttributeStore(IPersistentStorage storage) {
		this.storage = storage;
	}
	
	public void setStorage(IPersistentStorage storage) {
		this.storage = storage;
	}
	
	public Set getAttributeNames(){
		return attributes.keySet();
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}
	
	synchronized public boolean setAttribute(String name, Object value) {
		if (name == null)
			return false;
		
		Object old = attributes.get(name);
		if ((old == null && value != null) || !old.equals(value)) {
			// Attribute value changed
			attributes.put(name,value);
			return true;
		} else
			return false;
	}

	synchronized public void setAttributes(Map values) {
		attributes.putAll(values);
	}
	
	synchronized public void setAttributes(org.red5.server.api.IAttributeStore values) {
		Iterator it = values.getAttributeNames().iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			Object value = values.getAttribute(name);
			setAttribute(name, value);
		}
	}
	
	synchronized public boolean removeAttribute(String name) {
		if (name == null)
			return false;
		
		boolean result = hasAttribute(name);
		attributes.remove(name);
		return result;
	}
	
	synchronized public void removeAttributes() {
		attributes.clear();
	}	
	
	public String getPersistentId() {
		if (persistentId == null && storage != null) {
			persistentId = storage.newPersistentId(); 
		}
		
		return persistentId;
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
		Serializer serializer = new Serializer();
		serializer.writeMap(out, attributes);
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
		Map data = (Map) deserializer.deserialize(in);
		attributes.clear();
		attributes.putAll(data);
	}
	
	public IPersistentStorage getStorage() {
		return storage;
	}
	
}
