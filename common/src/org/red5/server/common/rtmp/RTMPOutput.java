package org.red5.server.common.rtmp;

import java.nio.BufferOverflowException;

import org.red5.server.common.ExByteBuffer;
import org.red5.server.common.rtmp.packet.RTMPPacket;

public interface RTMPOutput {
	void write(ExByteBuffer buf, RTMPPacket packet)
	throws RTMPCodecException, BufferOverflowException;
	RTMPCodecState getCodecState();
}
