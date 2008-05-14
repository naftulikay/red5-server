package org.red5.server.common.rtmp;

import org.red5.server.common.rtmp.packet.RTMPPacket;

public class RTMPUtils {
	public static void copyHeader(RTMPPacket dest, RTMPPacket src) {
		dest.setChannel(src.getChannel());
		dest.setTimestamp(src.getTimestamp());
		dest.setSize(src.getSize());
		dest.setStreamId(src.getStreamId());
	}
}
