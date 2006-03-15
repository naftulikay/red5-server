package org.red5.server.api.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.red5.server.api.IBroadcastStream;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IContext;
import org.red5.server.api.IScope;
import org.red5.server.api.IScopeAuth;
import org.red5.server.api.IScopeHandler;
import org.red5.server.api.ISharedObject;
import org.springframework.core.io.Resource;

public class Scope extends AttributeStore implements IScope {
		
	private Scope parent;
	private String name = "";
	private IScopeHandler handler;
	private IScopeAuth auth; 
	private IContext context;
	private int depth;
	
	private HashMap childScopes = new HashMap();

	private HashMap broadcastStreams = new HashMap();
	private HashMap sharedObjects = new HashMap();
	private HashMap clients = new HashMap();
	
	public Scope(Scope parent, String name, IContext context){
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
		return clients.keySet();
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
		if(hasParent() && !parent.connect(conn)) return false;
		if(!auth.canConnect(conn, this)) return false;
		final IClient client = conn.getClient();
		if(!clients.containsKey(client)){
			//handler.onClientConnect(Client client);
			final Set conns = new HashSet();
			conns.add(conn);
			clients.put(conn.getClient(), conns);
		} else {
			final Set conns = (Set) clients.get(client);
			conns.add(conn);
		}
		if(handler != null) 
			handler.onConnect(conn);
		return true;
	}
	
	void disconnect(IConnection conn){
		if(hasParent()) parent.disconnect(conn);
		final IClient client = conn.getClient();
		if(clients.containsKey(client)){
			final Set conns = (Set) clients.get(client);
			conns.remove(conn);
			if(handler != null) 
				handler.onDisconnect(conn);
			if(conns.isEmpty()) {
				clients.remove(clients);
				//handler.onClientDisconnect(client);
			}
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
	
	public Iterator getConnections() {
		return new ConnectionIterator();
	}

	public Set lookupConnections(IClient client) {
		return (Set) clients.get(client);
	}

	class ConnectionIterator implements Iterator {

		private Iterator setIterator; 
		private Iterator connIterator;
		private IConnection current;
		
		public ConnectionIterator(){
			setIterator = clients.values().iterator();
		}
		
		public boolean hasNext() {
			return connIterator.hasNext() || setIterator.hasNext();
		}

		public Object next() {
			if(!connIterator.hasNext()){
				if(!setIterator.hasNext()) return null;
				connIterator = ((Set) setIterator.next()).iterator();
			}
			current = (IConnection) connIterator.next();
			return current;
		}

		public void remove() {
			if(current!=null){
				disconnect(current);
			}
		}
		
	}
	
}