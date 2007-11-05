package org.red5.server.net.rtmp;

import org.red5.server.net.rtmp.codec.RTMP;
import org.red5.server.net.rtmpt.codec.EdgeRTMP;

public class EdgeRTMPMinaIoHandler extends RTMPMinaIoHandler {
	private IRTMPConnManager rtmpConnManager;

	@Override
	protected RTMPMinaConnection createRTMPMinaConnection() {
		return (RTMPMinaConnection) rtmpConnManager.createConnection(EdgeRTMPMinaConnection.class);
	}

	public void setRtmpConnManager(IRTMPConnManager rtmpConnManager) {
		this.rtmpConnManager = rtmpConnManager;
	}

	@Override
	protected RTMP createRTMP(boolean mode) {
		// TODO Auto-generated method stub
		return new EdgeRTMP(mode);
	}
}
