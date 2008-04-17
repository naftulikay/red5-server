package org.red5.server.common.rtmp.packet;

public class RTMPBytesRead extends RTMPPacket {
	private int bytesRead;
	
	public RTMPBytesRead() {
		super(TYPE_RTMP_BYTES_READ);
	}

	public int getBytesRead() {
		return bytesRead;
	}

	public void setBytesRead(int bytesRead) {
		this.bytesRead = bytesRead;
	}

}
