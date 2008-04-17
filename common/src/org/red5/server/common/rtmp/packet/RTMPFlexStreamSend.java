package org.red5.server.common.rtmp.packet;

public class RTMPFlexStreamSend extends RTMPNotify {

	public RTMPFlexStreamSend() {
		super();
		setType(TYPE_RTMP_FLEX_STREAM_SEND);
	}

}
