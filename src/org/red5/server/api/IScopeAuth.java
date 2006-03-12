package org.red5.server.api;

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
 * Scope Auth controls actions performed against a scope object
 * 
 * Gives fine grained control over what actions can be performed with the can*
 * methods.
 * 
 * The thread local connection is always available via the Red5 object within
 * these methods
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Luke Hubbard (luke@codegent.com)
 */
public interface IScopeAuth {

	/**
	 * Can a new scope be created for a given context path
	 * 
	 * @param contextPath
	 *            the context path, eg: /myapp/room
	 * @return true if the scope can be created, otherwise false
	 */
	boolean canCreateScope(String contextPath);

	/**
	 * Can a given client connect to a scope
	 * 
	 * @param conn
	 *            the connection object
	 * @return true if the client can connect, otherwise false
	 */
	boolean canConnect(IConnection conn, IScope scope);

	/**
	 * Can the service call proceed
	 * 
	 * @param call
	 *            call object holding service name, method, and arguments
	 * @return true if the client can call the service, otherwise false
	 */
	boolean canCallService(ICall call);
	
	/**
	 * Can an event be broadcast to all connected clients
	 * 
	 * @param event
	 *            the event object
	 * @return true if the broadcast can continue
	 */
	boolean canBroadcastEvent(Object event);

	/**
	 * Can a client record a stream with a given name
	 * 
	 * @param name
	 *            name of the stream to be recorded, usually the name of the FLV
	 * @return true if the record can continue, otherwise false
	 */
	boolean canRecordStream(String name);
	
	/**
	 * Can the stream be published
	 * 
	 * @param name
	 *            of the stream
	 * @return true if the client can publish the stream, otherwise false
	 */
	boolean canPublishStream(String name);

	/**
	 * Can a client subscribe to a broadcast stream
	 * 
	 * @param name
	 *            the name of the stream
	 * @return true if they can subscribe, otherwise false
	 */
	boolean canSubscribeToBroadcastStream(String name);

	/**
	 * Can a client connect to a shared object
	 * 
	 * @param soName
	 *            the name of the shared object, since it may not exist yet
	 * @return true if they can connect, otherwise false
	 */
	boolean canConnectSharedObject(String soName);

	/**
	 * Can a shared object attribute be updated
	 * 
	 * @param so
	 *            the shared object be updated
	 * @param key
	 *            the name of the attribute
	 * @param value
	 *            the value of the attribute
	 * @return true if the update can continue
	 */
	boolean canUpdateSharedObject(ISharedObject so, String key, Object value);

	/**
	 * Can the client delete a shared object attribute
	 * 
	 * @param so
	 *            the shared object
	 * @param key
	 *            the name of the attribute to be deleted
	 * @return true if the delete can continue, otherwise false
	 */
	boolean canDeleteSharedObject(ISharedObject so, String key);

	/**
	 * Can a shared object send continue
	 * 
	 * @param so
	 *            the shared object
	 * @param method
	 *            the method name
	 * @param params
	 *            the arguments
	 * @return true if the send can continue, otherwise false
	 */
	boolean canSendSharedObject(ISharedObject so, String method, Object[] params);

}
