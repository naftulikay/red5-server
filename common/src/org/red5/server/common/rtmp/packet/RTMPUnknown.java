package org.red5.server.common.rtmp.packet;

import org.red5.server.common.BufferEx;

public class RTMPUnknown extends RTMPPacket {
	private BufferEx unknownBody;

	public RTMPUnknown(int type) {
		super(type);
	}

	public BufferEx getUnknownBody() {
		return unknownBody;
	}

	public void setUnknownBody(BufferEx unknownBody) {
		this.unknownBody = unknownBody;
	}
	
}
