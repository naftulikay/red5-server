package org.red5.server.common.rtmp;

import org.red5.server.common.rtmp.packet.RTMPPacket;

public interface RTMPHandler<T> {
	void onPacket(T source, RTMPPacket packet);
}
