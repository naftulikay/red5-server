package org.red5.server.net.rtmp.event;

import org.red5.server.api.event.IEventListener;
import org.red5.server.net.rtmp.message.Constants;

public abstract class BaseEvent implements Constants, IRTMPEvent {

	// override this
	protected byte EVENT_DATATYPE = 0x00; 
	
	private Type type;
	protected Object object;
	protected IEventListener source;
	protected int timestamp;
	
	public BaseEvent(Type type) {
		this(type, null);
	}
	
	public BaseEvent(Type type, IEventListener source) {
		this.type = type;
		this.source = source;
	}
	
	public Type getType() {
		return type;
	}
	
	public Object getObject() {
		return object;
	}
	
	public boolean hasSource() {
		return source != null;
	}
	
	public IEventListener getSource() {
		return source;
	}

	public byte getDataType() {
		return EVENT_DATATYPE;
	}
	
	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}
	
}
