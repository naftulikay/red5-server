package org.red5.server.common.rtmp.packet;

public class RTMPPacket {
	public static final int TYPE_RTMP_CHUNK_SIZE          = 0x01;
	// unknown 0x02
	public static final int TYPE_RTMP_BYTES_READ          = 0x03;
	public static final int TYPE_RTMP_PING                = 0x04;
	public static final int TYPE_RTMP_SERVER_BW           = 0x05;
	public static final int TYPE_RTMP_CLIENT_BW           = 0x06;
	// unknown 0x07
	public static final int TYPE_RTMP_AUDIO               = 0x08;
	public static final int TYPE_RTMP_VIDEO               = 0x09;
	// unknown 0x0a - 0x0e
	public static final int TYPE_RTMP_FLEX_STREAM_SEND    = 0x0f;
	public static final int TYPE_RTMP_FLEX_SHARED_OBJECT  = 0x10;
	public static final int TYPE_RTMP_FLEX_MESSAGE        = 0x11;
	public static final int TYPE_RTMP_NOTIFY              = 0x12;
	public static final int TYPE_RTMP_SHARED_OBJECT       = 0x13;
	public static final int TYPE_RTMP_INVOKE              = 0x14;
	// handshake is not an official RTMP packet
	public static final int TYPE_RTMP_HANDSHAKE           = 0x100;
	
	private int channel;
	private long timestamp;
	private int size;
	private int type;
	private int streamId;
	
	protected RTMPPacket(int type) {
		this.type = type;
	}
	
	public int getChannel() {
		return channel;
	}
	public void setChannel(int channel) {
		this.channel = channel;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getType() {
		return type;
	}
	protected void setType(int type) {
		this.type = type;
	}
	public int getStreamId() {
		return streamId;
	}
	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}
}
