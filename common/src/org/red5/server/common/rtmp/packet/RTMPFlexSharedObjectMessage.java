package org.red5.server.common.rtmp.packet;

public class RTMPFlexSharedObjectMessage extends RTMPSharedObjectMessage {

	public RTMPFlexSharedObjectMessage() {
		super();
		setType(RTMPPacket.TYPE_RTMP_FLEX_SHARED_OBJECT);
	}

}
