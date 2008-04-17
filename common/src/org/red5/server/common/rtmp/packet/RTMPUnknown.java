package org.red5.server.common.rtmp.packet;

import org.red5.server.common.ExByteBuffer;

public class RTMPUnknown extends RTMPPacket {
	private ExByteBuffer unknownBody;

	public RTMPUnknown(int type) {
		super(type);
	}

	public ExByteBuffer getUnknownBody() {
		return unknownBody;
	}

	public void setUnknownBody(ExByteBuffer unknownBody) {
		this.unknownBody = unknownBody;
	}
	
}
