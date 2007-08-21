package org.red5.server.net.mrtmp;

public interface IMRTMPOriginManager extends IMRTMPManager {
	/**
	 * Set the afinity of a RTMPT client with a MRTMP connection
	 * so that the specified MRTMP connection will get higher
	 * priority for selection as the output for packets.
	 * Note it is implementation specific to choose which MRTMP
	 * connection for which RTMPT client.
	 * @param conn
	 * @param clientId
	 */
	void setAfinity(IMRTMPConnection conn, int clientId);
}
