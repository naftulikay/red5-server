package org.red5.server;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.red5.server.api.IBasicScope;
import org.red5.server.api.IScope;
import org.red5.server.api.event.IEvent;
import org.red5.server.api.event.IEventDispatcher;
import org.red5.server.api.event.IEventListener;

public class BasicScope extends AttributeStore implements IBasicScope {

	protected IScope parent;
	protected String name;
	protected Set<IEventListener> listeners;
	protected boolean persistent = false; 
	protected String type;
	
	public BasicScope(IScope parent, String type,  String name, boolean persistent){
		this.parent = parent;
		this.type=type;
		this.name = name;
		this.persistent = persistent;
		this.listeners = new HashSet<IEventListener>();
	}
	
	public String getType(){
		return type;
	}
	
	public boolean hasParent() {
		return true;
	}

	public IScope getParent() {
		return parent;
	}

	public int getDepth() {
		return parent.getDepth() + 1;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return parent.getPath() + "/" + name;
	}

	public void addEventListener(IEventListener listener) {
		listeners.add(listener);
	}

	public void removeEventListener(IEventListener listener) {
		listeners.remove(listener);
	}

	public Iterator<IEventListener> getEventListeners() {
		return listeners.iterator();
	}

	public boolean isPersistent() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setPersistent(boolean persistent) {
		// TODO Auto-generated method stub
		
	}

	public boolean handleEvent(IEvent event) {
		// do nothing.
		return false;
	}

	public void notifyEvent(IEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void dispatchEvent(Object event) {
		// send out an event, with this object as the source
	}
	
	public void dispatchEvent(IEvent event){
		for(IEventListener listener : listeners){
			if(event.getSource()==null || 
					event.getSource() != listener )
				listener.notifyEvent(event);
		}
	}
	
	
}