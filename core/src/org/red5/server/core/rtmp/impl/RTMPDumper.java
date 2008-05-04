package org.red5.server.core.rtmp.impl;

import org.red5.server.common.rtmp.RTMPHandler;
import org.red5.server.common.rtmp.packet.RTMPPacket;

public class RTMPDumper implements RTMPHandler<Object> {

	@Override
	public void onPacket(Object source, RTMPPacket packet) {
		System.out.println(packet);
	}

}
