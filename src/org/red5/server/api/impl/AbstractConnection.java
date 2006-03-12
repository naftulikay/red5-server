package org.red5.server.api.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.set.UnmodifiableSet;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.IStream;
import org.red5.server.api.ScopeUtils;

public abstract class AbstractConnection extends AttributeStore 
	implements IConnection {
	
	protected IClient client = null;
	protected String contextPath = null;
	protected Scope scope = null;
	private HashSet streams = new HashSet();

	public AbstractConnection(IClient client, String contextPath){
		this.client = client;
		this.contextPath = contextPath;
	}
	
	public AbstractConnection(IClient client, Scope scope){
		this.client = client;
		setScope(scope);
	}
	
	public abstract void close();

	public abstract void dispatchEvent(Object object);

	public abstract Map getParams();
	
	public IClient getClient() {
		return client;
	}
	
	public String getContextPath(){
		return contextPath;
	}

	public IScope getScope() {
		return scope;
	}

	public Set getStreams() {
		return UnmodifiableSet.decorate(streams);
	}

	public abstract String getType();

	public abstract boolean isConnected();

	public void setScope(Scope scope){
		this.scope = scope;
		contextPath = scope.getContextPath();
	}
	
	public boolean switchScope(String contextPath) {
		// At the moment this method is not dealing with tree schematics
		Scope newScope = (Scope) ScopeUtils.resolveScope(scope, contextPath);
		if(newScope == null) return false;
		if(newScope.connect(this)){
			scope.disconnect(this);
			setScope(newScope);
			return true;
		} else 	return false;
	}
	
	void register(IStream stream){
		streams.add(stream);
	}
	
	void unregister(IStream stream){
		if(streams.contains(stream))
			streams.remove(stream);
	}

}