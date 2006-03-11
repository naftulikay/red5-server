package org.red5.server.net.message;

/**
 * One update event for a shared object received through a connection.
 */
public interface ISharedObjectEvent {

	/**
	 * Connect the current client to the shared object.
	 */
	final public static int CONNECT				= 1;
	/**
	 * Clear the data of the shared object.
	 */
	final public static int CLEAR				= 2;
	/**
	 * Change an attribute of the shared object.
	 */
	final public static int SET_ATTRIBUTE		= 3;
	/**
	 * Delete an attribute of the shared object
	 */
	final public static int DELETE_ATTRIBUTE	= 4;
	/**
	 * Send a message to all connected clients of the shared object.
	 */
	final public static int SEND_MESSAGE		= 5; 
	
	/**
	 * Returns the type of the event.
	 * 
	 * @return the type of the event.
	 */
	public int getType();
	
	/**
	 * Returns the key of the event.
	 * 
	 * Depending on the type this contains:
	 * <ul>
	 * <li>the attribute name to set for SET_ATTRIBUTE</li>
	 * <li>the attribute name to delete for DELETE_ATTRIBUTE</li>
	 * <li>the handler name to call for SEND_MESSAGE</li>
	 * </ul>
	 * In all other cases the key is <code>null</code>.
	 * 
	 * @return the key of the event
	 */
	public String getKey();
	
	/**
	 * Returns the value of the event.
	 * 
	 * Depending on the type this contains:
	 * <ul>
	 * <li>the attribute value to set for SET_ATTRIBUTE</li>
	 * <li>a list of parameters to pass to the handler for SEND_MESSAGE</li>
	 * </ul>
	 * In all other cases the value is <code>null</code>.
	 * 
	 * @return the value of the event
	 */
	public Object getValue();
}
