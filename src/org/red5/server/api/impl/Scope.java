package org.red5.server.api.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.set.UnmodifiableSet;
import org.red5.server.api.IBroadcastStream;
import org.red5.server.api.IConnection;
import org.red5.server.api.IContext;
import org.red5.server.api.IScope;
import org.red5.server.api.IScopeAuth;
import org.red5.server.api.IScopeHandler;
import org.red5.server.api.ISharedObject;
import org.springframework.core.io.Resource;

public class Scope extends AttributeStore implements IScope {
		
	private IScope parent;
	private String name = "";
	private IScopeHandler handler;
	private IScopeAuth auth; 
	private IContext context;
	private int depth;
	
	private HashMap childScopes = new HashMap();

	private HashMap broadcastStreams = new HashMap();
	private HashMap sharedObjects = new HashMap();
	private HashSet clients = new HashSet();
	
	public Scope(IScope parent, String name, IContext context){
		this.parent = parent;
		this.name = name;
		this.context = context;
		handler = context.lookupScopeHandler(getPath());
		auth = handler.getScopeAuth(this);
		if(parent == null) depth = 0;
		else depth = parent.getDepth() + 1;
	}
	
	public void dispatchEvent(Object event) {
		// TODO: Should this be in a util class ?
	}
	
	public IBroadcastStream getBroadcastStream(String name) {
		return (IBroadcastStream) broadcastStreams.get(name);
	}

	public Set getBroadcastStreamNames() {
		return broadcastStreams.keySet();
	}

	public boolean hasChildScope(String name){
		return childScopes.containsKey(name);
	}
	
	public IScope getChildScope(String name) {
		return (IScope) childScopes.get(name);
	}

	public Set getChildScopeNames() {
		return childScopes.keySet();
	}

	public Set getClients() {
		return UnmodifiableSet.decorate(clients);
	}

	public IContext getContext() {
		return context;
	}

	public String getName() {
		return name;
	}
	
	public String getPath() {
		if(hasParent()) return parent.getPath() + "/" + name;
		else return "";
	}

	public IScopeAuth getAuth() {
		return auth;
	}
	
	public IScopeHandler getHandler() {
		return handler;
	}

	public IScope getParent() {
		return parent;
	}
	
	public boolean createSharedObject(String name, boolean persistent){
		// TODO:
		return false;
	}
	
	public ISharedObject getSharedObject(String name) {
		return (ISharedObject) sharedObjects.get(name);
	}

	public Set getSharedObjectNames() {
		return sharedObjects.keySet();
	}

	public boolean hasBroadcastStream(String name) {
		return broadcastStreams.containsKey(name);
	}

	public boolean hasParent() {
		return (parent != null);
	}

	boolean connect(IConnection conn) {
		if(!auth.canConnect(conn, this)) return false;
		if(!clients.contains(conn.getClient())){
			clients.add(conn.getClient());
			handler.onConnect(conn);		
		}
		return true;
	}
	
	void disconnect(IConnection conn){
		if(clients.contains(conn.getClient())){
			clients.remove(conn.getClient());
			handler.onDisconnect(conn);
		}
	}

	public int getDepth() {
		return depth;
	}

	public Resource[] getResources(String path) throws IOException {
		return context.getResources(path);
	}

	public Resource getResource(String path) {
		return context.getResource(path);
	}

}