package org.red5.server.common.rtmp.packet;

public class RTMPHeader extends RTMPPacket {
	private int relativeTS;

	public RTMPHeader() {
		super(-1);
	}

	public int getRelativeTS() {
		return relativeTS;
	}

	public void setRelativeTS(int relativeTS) {
		this.relativeTS = relativeTS;
	}

	public void setType(int type) {
		super.setType(type);
	}
}
