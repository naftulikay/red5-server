package org.red5.server.common.rtmp.packet;

public class RTMPServerBW extends RTMPPacket {
	private int bandwidth;
	
	public RTMPServerBW() {
		super(TYPE_RTMP_SERVER_BW);
	}

	public int getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}

}
