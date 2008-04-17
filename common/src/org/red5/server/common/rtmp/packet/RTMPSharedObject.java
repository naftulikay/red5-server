package org.red5.server.common.rtmp.packet;

public abstract class RTMPSharedObject {
	private int soType;
	private int soLength;
	
	protected RTMPSharedObject(int soType) {
		this.soType = soType;
	}
	
	public int getSoType() {
		return soType;
	}
	protected void setSoType(int soType) {
		this.soType = soType;
	}
	public int getSoLength() {
		return soLength;
	}
	public void setSoLength(int soLength) {
		this.soLength = soLength;
	}
	
}
