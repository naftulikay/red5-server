package org.red5.server.common.rtmp.packet;

public class RTMPFlexMessage extends RTMPInvoke {

	public RTMPFlexMessage() {
		super();
		setType(TYPE_RTMP_FLEX_MESSAGE);
	}

}
