package org.red5.server.net.rtmpt;

import org.apache.mina.common.ByteBuffer;
import org.red5.server.api.scheduling.ISchedulingService;
import org.red5.server.net.rtmp.codec.RTMP;
import org.red5.server.net.rtmpt.codec.EdgeRTMP;

public class EdgeRTMPTConnection extends RTMPTConnection {

	@Override
	void setRTMPTHandle(RTMPTHandler handler) {
    	synchronized (this) {
    		this.state = new EdgeRTMP(RTMP.MODE_SERVER);
    		// FIXME use cluster-wide unique method to generate client id
    		clientId = hashCode();
    	}
		this.buffer = ByteBuffer.allocate(2048);
		this.buffer.setAutoExpand(true);
		this.handler = handler;
		this.decoder = handler.getCodecFactory().getSimpleDecoder();
		this.encoder = handler.getCodecFactory().getSimpleEncoder();
	}

	@Override
	protected void startWaitForHandshake(ISchedulingService service) {
		// FIXME do nothing to avoid disconnect.
	}

}
