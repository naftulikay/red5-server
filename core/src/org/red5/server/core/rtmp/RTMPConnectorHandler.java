package org.red5.server.core.rtmp;

import org.red5.server.common.rtmp.RTMPHandler;
import org.red5.server.common.rtmp.packet.RTMPPacket;

public interface RTMPConnectorHandler extends RTMPHandler<RTMPConnection> {
	void packetSent(RTMPConnection connection, RTMPPacket packet);
	void sessionOpened(RTMPConnection connection);
	void sessionClosed(RTMPConnection connection);
}
