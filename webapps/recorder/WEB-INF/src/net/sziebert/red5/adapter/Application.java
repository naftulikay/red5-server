package net.sziebert.red5.adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IConnection;

/**
 * <code>Application</code> is a simple <code>ApplicationAdapter</code>
 * delegate which gets a reference to the publishing stream, plays it into a
 * server stream and records it.
 */
public class Application extends ApplicationAdapter
{
	private static final Log log = LogFactory.getLog(Application.class);

	/* ----- ApplicationAdapter delegate methods ----- */

	/**
	 * Delegate method used to accept/reject incoming connection requests.
	 * 
	 * @param conn
	 * @param params
	 * @return true/false
	 */
	@Override
	public boolean roomConnect(IConnection conn, Object[] params)
	{
		log.debug("New connection attempt from " + conn.getRemoteAddress() + "...");
		// Insure that the listeners are properly attached.
		return super.roomConnect(conn, params);
	}

	/**
	 * Delegate method which logs connection/client/user disconnections.
	 * 
	 * @param conn
	 */
	@Override
	public void roomDisconnect(IConnection conn)
	{
		log.debug("Connection closed by " + conn.getRemoteAddress() + "...");
		// Call the super class to insure that all listeners are properly
		// dismissed.
		super.roomDisconnect(conn);
	}

	/* ----- Application utility methods ----- */
}
