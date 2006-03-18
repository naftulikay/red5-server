package org.red5.server.api.impl;

import java.util.Map;

import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IContext;
import org.red5.server.api.IScope;
import org.red5.server.api.ScopeUtils;

public class BaseConnection extends AttributeStore 
	implements IConnection {
	
	protected String type;
	protected String host;
	protected String path;
	protected String sessionId;
	protected Map<String,String> params = null;
	
	protected IClient client = null;
	protected IContext context = null;
	protected Scope scope = null;
	
	public BaseConnection(String type, String host, String path, String sessionId, Map<String,String> params){
		this.type = type;
		this.host = host;
		this.path = path;
		this.sessionId = sessionId;
		this.params = params;
	}
	
	public void initialize(IClient client, IContext context){
		this.client = client;
		this.context = context;
	}
	
	public String getType(){
		return type;
	}

	public String getHost() {
		return host;
	}

	public String getPath(){
		return path;
	}

	public String getSessionId() {
		return sessionId;
	}

	public Map<String,String> getParams(){
		return params;
	}

	public IClient getClient() {
		return client;
	}

	public IContext getContext() {
		return null;
	}

	public boolean isConnected(){
		return scope == null;
	}

	public boolean connect(IScope newIScope) {
		Scope newScope = (Scope) newIScope;
		if(newScope.connect(this)){
			if(isConnected()) scope.disconnect((IConnection) this);
			scope = newScope;
			return true;
		} else 	return false;
	}

	public IScope getScope() {
		return scope;
	}
	
	public void close(){
		if(isConnected()) scope.disconnect(this);
	}

	/* This is really a utility
	public boolean switchScope(String contextPath) {
		// At the moment this method is not dealing with tree schematics
		Scope newScope = (Scope) ScopeUtils.resolveScope(scope, contextPath);
		if(newScope == null) return false;
		return connect(scope);
	}
	*/
	
}