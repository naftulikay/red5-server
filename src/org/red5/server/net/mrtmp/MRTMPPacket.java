package org.red5.server.net.mrtmp;

import org.apache.mina.common.ByteBuffer;
import org.red5.server.net.rtmp.message.Packet;

public class MRTMPPacket {
	public static final int CONNECT = 0;
	public static final int CLOSE = 1;
	public static final int RTMP = 2;
	
	public static final int COMMON_HEADER_LENGTH = 16;
	public static final int RTMP_HEADER_LENGTH = 20;
	
	private Header header;
	private Body body;
	
	static public class Header {
		private int type;
		private int clientId;
		private int headerLength;
		private int bodyLength;
		
		public int getBodyLength() {
			return bodyLength;
		}
		
		public void setBodyLength(int bodyLength) {
			this.bodyLength = bodyLength;
		}
		
		public int getClientId() {
			return clientId;
		}
		
		public void setClientId(int clientId) {
			this.clientId = clientId;
		}
		
		public int getHeaderLength() {
			return headerLength;
		}
		
		public void setHeaderLength(int headerLength) {
			this.headerLength = headerLength;
		}
		
		public int getType() {
			return type;
		}
		
		public void setType(int type) {
			this.type = type;
		}
	}
	
	static public class Body {
		private ByteBuffer rawBuf;

		public ByteBuffer getRawBuf() {
			return rawBuf;
		}

		public void setRawBuf(ByteBuffer rawBuf) {
			this.rawBuf = rawBuf;
		}
		
	}
	
	static public class RTMPHeader extends Header {
		private int rtmpType;

		public int getRtmpType() {
			return rtmpType;
		}

		public void setRtmpType(int rtmpType) {
			this.rtmpType = rtmpType;
		}
		
	}
	
	static public class RTMPBody extends Body {
		private Packet rtmpPacket;

		public Packet getRtmpPacket() {
			return rtmpPacket;
		}

		public void setRtmpPacket(Packet rtmpPacket) {
			this.rtmpPacket = rtmpPacket;
		}
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("MRTMPPakcet: type=");
		switch (header.getType()) {
			case CONNECT:
				buf.append("CONNECT");
				break;
			case CLOSE:
				buf.append("CLOSE");
				break;
			case RTMP:
				buf.append("RTMP");
				break;
			default:
				break;
		}
		buf.append(",clientId=" + header.getClientId());
		if (header.getType() == RTMP) {
			RTMPHeader rtmpHeader = (RTMPHeader) header;
			buf.append(",rtmpType=" + rtmpHeader.rtmpType);
			RTMPBody rtmpBody = (RTMPBody) body;
			buf.append(",rtmpBody=" + rtmpBody.rtmpPacket.getMessage());
		}

		return buf.toString();
	}
}
