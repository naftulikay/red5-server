package org.red5.server.api;

import java.util.Iterator;
import java.util.Set;

import org.springframework.core.io.support.ResourcePatternResolver;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright © 2006 by respective authors (see below). All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 2.1 of the License, or (at your option) any later 
 * version. 
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this library; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

/**
 * The Scope Object.
 * 
 * A statefull object shared between a group of clients connected to the same
 * context path. Scopes are arranged in a hierarchical way, so its possible for
 * a scope to have a parent. If a client is connect to a scope then they are also
 * connected to its parent scope. The scope object is used to access resources,
 * shared object, streams, etc.
 * 
 * The following are all names for scopes: application, room, place, lobby.
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Luke Hubbard (luke@codegent.com)
 */
public interface IScope extends IAttributeStore, ResourcePatternResolver {

	/**
	 * Does this scope have a parent
	 * 
	 * @return true if this scope has a parent
	 */
	public boolean hasParent();

	/**
	 * Get this scopes parent
	 * 
	 * @return parent scope, or null if this scope doesn't have a parent
	 */
	public IScope getParent();

	/**
	 * Get the full absolute path. eg. host/myapp/someroom
	 * 
	 * @return path
	 */
	public String getPath();
	
	/**
	 * Get the name of this scope. eg. someroom
	 * 
	 * @return name
	 */
	public String getName();
	
	/**
	 * Get the context
	 * 
	 * @return context object
	 */
	public IContext getContext();
	
	/**
	 * Check to see if this scope has a child scope matching a given name
	 * 
	 * @param name
	 *            the name of the child scope
	 * @return true if a child scope exists, otherwise false
	 */
	public boolean hasChildScope(String name);

	/**
	 * Get a set of the child scope names
	 * 
	 * @return set containing child scope names
	 */
	public Set getChildScopeNames();

	/**
	 * Get a child scope by name
	 * 
	 * @param name
	 *            name of the child scope
	 * @return the child scope, or null if no scope is found
	 */
	public IScope getChildScope(String name);

	/**
	 * Get a set of the shared object names
	 * 
	 * @return set containing the shared object names
	 */
	public Set getSharedObjectNames();

	/**
	 * Create a new shared object
	 * 
	 * @param name
	 *            the name of the shared object
	 * @param persistent
	 *            will the shared object be persistent
	 * @return true if the shared object was created, otherwise false
	 */
	public boolean createSharedObject(String name, boolean persistent);

	/**
	 * Get a shared object by name
	 * 
	 * @param name
	 *            the name of the shared object
	 * @return shared object, or null if not found
	 */
	public ISharedObject getSharedObject(String name);

	/**
	 * Get a set of connected clients You can get the connections by passing the
	 * scope to the clients lookupConnection method
	 * 
	 * @return set containing all connected clients
	 */
	public Set getClients();
	
	/**
	 * Dispatch an event to all connected clients
	 * 
	 * @param event
	 *            any simple object, which can be serialized and sent to clients
	 */
	public void dispatchEvent(Object event);

	/**
	 * Get the scope handler
	 * 
	 * @return scope handler
	 */
	public IScopeHandler getHandler();

	/**
	 * Get the scope auth
	 * 
	 * @return scope auth
	 */
	public IScopeAuth getAuth();
	
	/**
	 * Does the scope have a broadcast stream registered with a given name
	 * 
	 * @param name
	 *            name of the broadcast
	 * @return true is a stream exists, otherwise false
	 */
	public boolean hasBroadcastStream(String name);

	/**
	 * Get a broadcast stream by name
	 * 
	 * @param name
	 *            the name of the broadcast
	 * @return broadcast stream object
	 */
	public IBroadcastStream getBroadcastStream(String name);

	/**
	 * Get a set containing the names of all the broadcasts
	 * 
	 * @return set containing all broadcast names
	 */
	public Set getBroadcastStreamNames();
	
	/**
	 * Get the scopes depth, how far down the scope tree is it
	 * 
	 * @return depth
	 */
	public int getDepth();
	
	/**
	 * Lookup connections
	 * 
	 * @param	client object
	 * @return set of connection objects (readonly)
	 */
	public Set lookupConnections(IClient client);
	
	/**
	 * Get a connection iterator, you can call remove, and the connection will be closed.
	 * @return iterator holding all connections
	 */
	public Iterator getConnections();
	
}