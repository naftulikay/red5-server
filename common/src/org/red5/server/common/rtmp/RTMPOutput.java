package org.red5.server.common.rtmp;

import java.nio.BufferOverflowException;

import org.red5.server.common.BufferEx;
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
	void write(BufferEx buf, RTMPPacket packet)
	throws RTMPCodecException, BufferOverflowException;
	
	/**
	 * Encode an array of RTMP packets into the buffer.
	 * 
	 * @param buf
	 * @param packets
	 * @throws RTMPCodecException
	 * @throws BufferOverflowException
	 */
	void write(BufferEx buf, RTMPPacket[] packets)
	throws RTMPCodecException, BufferOverflowException;
	
	/**
	 * Write the packet to a newly-created buffer
	 * 
	 * @param packet
	 * @return Newly-created buffer
	 * @throws RTMPCodecException
	 */
	BufferEx write(RTMPPacket packet) throws RTMPCodecException;
	
	/**
	 * Write an array of packets to a newly-created buffer
	 * 
	 * @param packets
	 * @return Newly-created buffer
	 * @throws RTMPCodecException
	 */
	BufferEx write(RTMPPacket[] packets) throws RTMPCodecException;
	
	RTMPCodecState getCodecState();
	
	boolean isServerMode();
	
	/**
	 * RTMP uses short headers with relative timestamps. But
	 * sometimes it is mandatory to send a standard header with
	 * absolute timestamp (eg. the stream packet after a stream
	 * reset ping). Use this method to reset the output channel
	 * and make sure the next packet uses standard header.
	 * 
	 * @param channel
	 */
	void resetOutputChannel(int channel);
	
	/**
	 * Reset the output to the original state, free
	 * all resources.
	 */
	void resetOutput();
}
