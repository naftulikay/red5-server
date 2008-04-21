package org.red5.server.common.rtmp;

import java.nio.BufferOverflowException;

import org.red5.server.common.ExByteBuffer;
import org.red5.server.common.rtmp.packet.RTMPPacket;

public interface RTMPOutput {
	/**
	 * Encode an RTMP packet into the user-specified buffer.
	 * 
	 * @param buf
	 * @param packet
	 * @throws RTMPCodecException
	 * @throws BufferOverflowException
	 */
	void write(ExByteBuffer buf, RTMPPacket packet)
	throws RTMPCodecException, BufferOverflowException;
	
	/**
	 * Encode an array of RTMP packets into the buffer.
	 * 
	 * @param buf
	 * @param packets
	 * @throws RTMPCodecException
	 * @throws BufferOverflowException
	 */
	void write(ExByteBuffer buf, RTMPPacket[] packets)
	throws RTMPCodecException, BufferOverflowException;
	
	RTMPCodecState getCodecState();
	
	RTMPMode getOutputRTMPMode();
	
	void resetOutputChannel(int channel);
	
	void resetOutput();
}
