package org.red5.server.common.rtmp;

import org.red5.server.common.ExByteBuffer;
import org.red5.server.common.rtmp.packet.RTMPPacket;

public interface RTMPInput {
	/**
	 * Decode one RTMP packet from the user-specified buffer.
	 * The input buffer will be set to the original state if any exception
	 * occurs, otherwise all bytes in the buffer will be consumed if
	 * it is not enough to generate a packet.
	 * 
	 * @param buf
	 * @return The decoded RTMP packet or <tt>null</tt> if the bytes
	 * in buffer are not enough for an RTMP packet.
	 * @throws RTMPCodecException Any problem found during decoding.
	 */
	RTMPPacket read(ExByteBuffer buf) throws RTMPCodecException;
	
	/**
	 * Decode as many RTMP packets as possible from the buffer.
	 * The input buffer will be set to the original state if any exception
	 * occurs, otherwise all bytes in the buffer will be consumed.
	 * 
	 * @param buf
	 * @return An array of decoded RTMP packets or an empty array if
	 * the bytes in buffer are not enough for an RTMP packet.
	 * @throws RTMPCodecException Any problem found during decoding.
	 */
	RTMPPacket[] readAll(ExByteBuffer buf) throws RTMPCodecException;
	
	RTMPCodecState getCodecState();
	
	RTMPMode getInputRTMPMode();
	
	void resetInput();
}
