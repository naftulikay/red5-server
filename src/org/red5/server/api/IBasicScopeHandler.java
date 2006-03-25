package org.red5.server.api;

public interface IBasicScopeHandler {

	/**
	 * Called when a scope is created for the first time
	 * 
	 * @param scope
	 *            the new scope object
	 */
	boolean start(IScope scope);

	/**
	 * Called just before a scope is disposed
	 */
	void stop(IScope scope);
	
	/**
	 * Called just before every connection to a scope
	 * 
	 * @param conn
	 *            connection object
	 */
	boolean connect(IConnection conn);

	/**
	 * Called just after the a connection is disconnected
	 * 
	 * @param conn
	 *            connection object
	 */
	void disconnect(IConnection conn);
	
	/**
	 * Called when an event is broadcast
	 * 
	 * @param event
	 *            the event object
	 */
	boolean handleEvent(IConnection conn, Object event);
	
}
