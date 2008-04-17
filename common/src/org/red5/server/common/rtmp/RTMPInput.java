package org.red5.server.common.rtmp;

import org.red5.server.common.ExByteBuffer;
import org.red5.server.common.rtmp.packet.RTMPPacket;

public interface RTMPInput {
	RTMPPacket read(ExByteBuffer buf) throws RTMPCodecException;
	RTMPCodecState getCodecState();
}
