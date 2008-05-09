package org.red5.server.common.rtmp.packet;

import org.red5.server.common.BufferEx;

public class RTMPHandshake extends RTMPPacket {
	public static final int HANDSHAKE_SIZE = 1536;
	
	private BufferEx handshakeData;

	public RTMPHandshake() {
		super(TYPE_RTMP_HANDSHAKE);
	}

	public BufferEx getHandshakeData() {
		return handshakeData;
	}

	public void setHandshakeData(BufferEx handshakeData) {
		this.handshakeData = handshakeData;
	}

}
