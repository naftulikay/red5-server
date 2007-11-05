package org.red5.server.net.mrtmp;

import java.util.ArrayList;
import java.util.List;

import org.red5.server.net.rtmp.RTMPConnection;

public class SimpleMRTMPEdgeManager implements IMRTMPEdgeManager {
	private List<IMRTMPConnection> connList = new ArrayList<IMRTMPConnection>();
	
	public boolean registerConnection(IMRTMPConnection conn) {
		return connList.add(conn);
	}

	public boolean unregisterConnection(IMRTMPConnection conn) {
		return connList.remove(conn);
	}

	public IMRTMPConnection lookupMRTMPConnection(RTMPConnection conn) {
		if (connList.size() > 0) {
			return connList.get(0);
		} else {
			return null;
		}
	}
	
}
