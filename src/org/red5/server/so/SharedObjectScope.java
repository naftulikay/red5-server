package org.red5.server.so;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.red5.server.BasicScope;
import org.red5.server.api.IAttributeStore;
import org.red5.server.api.IScope;
import org.red5.server.api.event.IEvent;
import org.red5.server.api.event.IEventListener;
import org.red5.server.api.so.ISharedObject;

public class SharedObjectScope extends BasicScope 
	implements ISharedObject {

	protected int updateCounter = 0;
	protected boolean modified = false;
	protected int version = 0;
	private final ReentrantLock lock = new ReentrantLock();
	private final LinkedList updates = new LinkedList();
	protected boolean persistent = false;
	protected ISharedObjectMessage syncMessage;
	protected ISharedObjectMessage sourceMessage;
	protected IEventListener source;
	
	public SharedObjectScope(IScope parent, String name, boolean persistent){
		super(parent,name, persistent);
	}
	
	public void beginUpdate(IEventListener source){
		beginUpdate();
		this.source = source;
	}
	
	public void beginUpdate() {
		if(!lock.isHeldByCurrentThread()) lock.lock();
		updateCounter++;
	}

	public void endUpdate() {
		updateCounter--;
		if(updateCounter == 0){
			// send messages
			if(source != null) 
				source.notifyEvent(sourceMessage);
			source = null;
			if(!syncMessage.isEmpty()) 
				dispatchEvent(syncMessage);
			lock.unlock();
		}
	}

	public int getVersion() {
		return version;
	}

	public void sendMessage(String handler, List arguments) {
		beginUpdate();
		syncMessage.addEvent(ISharedObjectEvent.Type.SEND_MESSAGE,handler,arguments);
		sourceMessage.addEvent(ISharedObjectEvent.Type.SEND_MESSAGE,handler,arguments);
		endUpdate();
	}
	
	@Override
	public synchronized boolean removeAttribute(String name) {
		beginUpdate();
		sourceMessage.addEvent(ISharedObjectEvent.Type.CLIENT_DELETE_ATTRIBUTE,name,null);
		final boolean success = super.removeAttribute(name);
		if(success) {
			syncMessage.addEvent(ISharedObjectEvent.Type.CLIENT_DELETE_ATTRIBUTE,name,null);
			version++;
		}
		endUpdate();
		return success;
	}
	
	@Override
	public void addEventListener(IEventListener listener) {
		// TODO Auto-generated method stub
		// prepare response for new client
		
		super.addEventListener(listener);
		
		sourceMessage.addEvent(ISharedObjectEvent.Type.CLIENT_INITIAL_DATA, null, null);
		if (!getAttributeNames().isEmpty())
			syncMessage.addEvent(ISharedObjectEvent.Type.CLIENT_UPDATE_DATA, null, this);

	}

	public boolean handleEvent(IEvent e){
		if(! (e instanceof ISharedObjectEvent)) return false;
		ISharedObjectMessage msg = (ISharedObjectMessage) e;
		if(msg.hasSource()) beginUpdate(msg.getSource());
		else beginUpdate();
		for(ISharedObjectEvent event : msg.getEvents()){
			switch(event.getType()){
			case CONNECT:
				if(msg.hasSource()) 
					addEventListener(msg.getSource());
				break;
			case SET_ATTRIBUTE:
				setAttribute(event.getKey(), event.getValue());
				break;
			case DELETE_ATTRIBUTE:
				removeAttribute(event.getKey());
				break;
			case SEND_MESSAGE:
				sendMessage(event.getKey(), (List) event.getValue());
				break;
			case CLEAR:
				removeAttributes();
				break;		
			}
		}
		endUpdate();
		return true;
	}
	

	@Override
	public synchronized boolean setAttribute(String name, Object value) {
		beginUpdate();
		final boolean success = super.setAttribute(name, value);
		if(success) {
			version++;
		}
		endUpdate();
		return success;
	}

	@Override
	public synchronized void setAttributes(IAttributeStore values) {
		beginUpdate();
		super.setAttributes(values);
		endUpdate();
	}

	@Override
	public synchronized void setAttributes(Map<String, Object> values) {
		beginUpdate();
		super.setAttributes(values);
		endUpdate();
	}
	
}