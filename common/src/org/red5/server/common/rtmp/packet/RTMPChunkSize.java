package org.red5.server.common.rtmp.packet;

public class RTMPChunkSize extends RTMPPacket {
	private int chunkSize;
	
	public RTMPChunkSize() {
		super(TYPE_RTMP_CHUNK_SIZE);
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	@Override
	public String toString() {
		return String.format("%1$s, ChunkSize=%2$d", super.toString(), chunkSize);
	}
}
