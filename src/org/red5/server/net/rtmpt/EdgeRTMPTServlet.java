package org.red5.server.net.rtmpt;

public class EdgeRTMPTServlet extends RTMPTServlet {

	@Override
	protected RTMPTConnection createConnection() {
		return (EdgeRTMPTConnection) rtmpConnManager.createConnection(EdgeRTMPTConnection.class);
	}

}
