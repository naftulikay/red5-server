package org.red5.server.common.rtmp.packet;

public class RTMPClientBW extends RTMPPacket {
	private int bandwidth;
	private byte value2;
	
	public RTMPClientBW() {
		super(TYPE_RTMP_CLIENT_BW);
	}

	public int getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}

	public byte getValue2() {
		return value2;
	}

	public void setValue2(byte value2) {
		this.value2 = value2;
	}

}
