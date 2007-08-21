package org.red5.server.net.mrtmp;

public interface IMRTMPManager {
	/**
	 * Map a client to an Origin MRTMP connection.
	 * @param clientId
	 * @return
	 */
	IMRTMPConnection lookupMRTMPConnection(int clientId);
	
	/**
	 * Register a MRTMP connection so that it can be later
	 * been looked up.
	 * @param conn
	 */
	void registerConnection(IMRTMPConnection conn);
	
	/**
	 * Unregister a MRTMP connection.
	 * @param conn
	 */
	void unregisterConnection(IMRTMPConnection conn);
}
