package org.red5.server.net.mrtmp;

import org.red5.server.net.rtmpt.EdgeRTMPTConnection;

public interface IMRTMPEdgeManager extends IMRTMPManager {
	/**
	 * Look up the RTMPT connection of Edge.
	 * @param clientId
	 * @return
	 */
	EdgeRTMPTConnection lookupRTMPTConnection(int clientId);
}
