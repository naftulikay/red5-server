package org.red5.server.core.rtmp.impl;

import java.util.List;
import java.util.Map;

import org.red5.server.core.rtmp.RTMPConnection;
import org.red5.server.core.rtmp.RTMPStatus;

public class RTMPConnectHandler {
	public RTMPStatus connect(RTMPConnection connection,
			Map<String,Object> connectionParams,
			List<Object> args) {
		// TODO
		return null;
	}
	
	public void disconnect(RTMPConnection connection) {
		connection.close();
	}
}
