package org.red5.server.net.mrtmp;

import java.util.ArrayList;
import java.util.List;

import org.red5.server.net.rtmpt.EdgeRTMPTConnection;
import org.red5.server.net.rtmpt.RTMPTServlet;

public class SimpleMRTMPEdgeManager implements IMRTMPEdgeManager {
	private RTMPTServlet servlet;
	private List<IMRTMPConnection> connList = new ArrayList<IMRTMPConnection>();
	
	public IMRTMPConnection lookupMRTMPConnection(int clientId) {
		if (connList.size() > 0) {
			return connList.get(0);
		} else {
			return null;
		}
	}

	public void registerConnection(IMRTMPConnection conn) {
		connList.add(conn);
	}

	public void unregisterConnection(IMRTMPConnection conn) {
		connList.remove(conn);
	}

	public EdgeRTMPTConnection lookupRTMPTConnection(int clientId) {
		return (EdgeRTMPTConnection) servlet.lookupConnection(clientId);
	}
	
	public void setRTMPTServlet(RTMPTServlet servlet) {
		this.servlet = servlet;
	}
}
