package org.red5.server.common.rtmp.packet;

import org.red5.server.common.ExByteBuffer;

public class RTMPHandshake extends RTMPPacket {
	public static final int HANDSHAKE_SIZE = 1536;
	
	private ExByteBuffer handshakeData;

	public RTMPHandshake() {
		super(TYPE_RTMP_HANDSHAKE);
	}

	public ExByteBuffer getHandshakeData() {
		return handshakeData;
	}

	public void setHandshakeData(ExByteBuffer handshakeData) {
		this.handshakeData = handshakeData;
	}

}
