package org.red5.server.common.rtmp.impl;

import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.Map;

import org.red5.server.common.BufferEx;
import org.red5.server.common.BufferExUtils;
import org.red5.server.common.rtmp.RTMPCodecException;
import org.red5.server.common.rtmp.RTMPCodecState;
import org.red5.server.common.rtmp.RTMPConstants;
import org.red5.server.common.rtmp.RTMPOutput;
import org.red5.server.common.rtmp.RTMPUtils;
import org.red5.server.common.rtmp.packet.RTMPChunkSize;
import org.red5.server.common.rtmp.packet.RTMPHeader;
import org.red5.server.common.rtmp.packet.RTMPPacket;

public abstract class BaseRTMPOutput
implements RTMPOutput, RTMPConstants {
	protected boolean isServerMode;
	protected RTMPCodecState codecState;
	protected int chunkSize = 128;
	
	private Map<Integer, RTMPHeader> lastHeaderMap =
		new HashMap<Integer, RTMPHeader>();
	private BufferEx headerBuf = BufferEx.allocate(14);
	private BufferEx bodyBuf = BufferEx.allocate(2048);

	public BaseRTMPOutput(boolean isServerMode) {
		this.isServerMode = isServerMode;
	}
	
	@Override
	public RTMPCodecState getCodecState() {
		return codecState;
	}

	@Override
	public boolean isServerMode() {
		return isServerMode;
	}

	@Override
	public void resetOutput() {
		lastHeaderMap.clear();
	}

	@Override
	public void resetOutputChannel(int channel) {
		lastHeaderMap.remove(channel);
	}

	@Override
	public void write(BufferEx buf, RTMPPacket packet)
			throws RTMPCodecException, BufferOverflowException {
		// encode body so that we can get the size of the packet
		bodyBuf.clear();
		encodePacketBody(bodyBuf, packet);
		bodyBuf.flip();
		
		// write header
		headerBuf.clear();
		RTMPHeader currentHeader = encodeHeader(headerBuf, packet);
		headerBuf.flip();
		
		outputBuffer(buf, currentHeader);
		
		lastHeaderMap.put(packet.getChannel(), currentHeader);
		
		if (packet instanceof RTMPChunkSize) {
			RTMPChunkSize chunkSizePacket = (RTMPChunkSize) packet;
			chunkSize = chunkSizePacket.getChunkSize();
		}
	}

	@Override
	public void write(BufferEx buf, RTMPPacket[] packets)
			throws RTMPCodecException, BufferOverflowException {
		for (RTMPPacket packet : packets) {
			write(buf, packet);
		}
	}

	@Override
	public BufferEx write(RTMPPacket packet) throws RTMPCodecException {
		// encode body so that we can get the size of the packet
		bodyBuf.clear();
		encodePacketBody(bodyBuf, packet);
		bodyBuf.flip();
		
		// write header
		headerBuf.clear();
		RTMPHeader currentHeader = encodeHeader(headerBuf, packet);
		headerBuf.flip();
		
		// create output buffer and put encoded packet
		int headerSize = headerBuf.remaining();
		int channelIdSize = headerSize -
			SUB_HEADER_SIZE[currentHeader.getHeaderType()];
		int bodySize = bodyBuf.remaining();
		int numChunks = bodySize / chunkSize;
		BufferEx buf = BufferEx.allocate(
				headerSize + bodySize +
				(numChunks-1)*channelIdSize);
		
		outputBuffer(buf, currentHeader);

		lastHeaderMap.put(packet.getChannel(), currentHeader);
		
		if (packet instanceof RTMPChunkSize) {
			RTMPChunkSize chunkSizePacket = (RTMPChunkSize) packet;
			chunkSize = chunkSizePacket.getChunkSize();
		}
		return buf;
	}

	/**
	 * Put header and body buffer to the output buffer.
	 * The header and body buffer should be ready.
	 * @param buf
	 * @param packet
	 * @param currentHeader
	 */
	private void outputBuffer(BufferEx buf, RTMPHeader currentHeader) {
		int numChunks = bodyBuf.remaining() / chunkSize;
		buf.put(headerBuf);
		// write the encoded body by chunks into
		// output buffer.
		for (int i = 0; i < numChunks; i++) {
			int bytesRemaining = bodyBuf.remaining();
			int bytesToWrite = bytesRemaining < chunkSize ?
					bytesRemaining : chunkSize;
			BufferExUtils.putBufferByLength(buf, bodyBuf, bytesToWrite);
			if (i < numChunks - 1) {
				writeChannelBytes(buf, currentHeader.getHeaderType(),
						currentHeader.getChannel());
			}
		}
	}

	@Override
	public BufferEx write(RTMPPacket[] packets) throws RTMPCodecException {
		if (packets == null || packets.length == 0) {
			throw new IllegalArgumentException("packets should not be null or empty");
		}
		BufferEx result = write(packets[0]);
		for (int i = 1; i < packets.length; i++) {
			write(result, packets[i]);
		}
		return result;
	}
	
	/**
	 * Encode packet body and fill the header size into packet.
	 * @param buf
	 * @param packet
	 */
	protected abstract void encodePacketBody(BufferEx buf, RTMPPacket packet);

	protected int writeChannelBytes(BufferEx buf, int headerType, int channelId)
	throws RTMPCodecException {
		if (channelId < 2 || channelId >= 65856 /* = 320+65536 */) {
			throw new RTMPCodecException("Invalid channel id " + channelId);
		}
		int headerPrefix = (headerType << 6) & 0x0ff;
		int originPos = buf.position();
		if (2 <= channelId &&  channelId < 64) {
			buf.put((byte) (headerPrefix | channelId));
		} else if (64 <= channelId && channelId < 320) {
			buf.put((byte) (headerPrefix | 0));
			buf.put((byte) (channelId - 64));
		} else {
			buf.put((byte) (headerPrefix | 1));
			int bytes = channelId - 320;
			buf.put((byte) (bytes & 0x0ff));
			buf.put((byte) ((bytes >> 8) & 0x0ff));
		}
		return buf.position() - originPos;
	}
	
	private RTMPHeader encodeHeader(BufferEx buf, RTMPPacket packet) {
		RTMPHeader lastHeader = lastHeaderMap.get(packet.getChannel());
		int headerType = 0;
		int relativeTS = 0;
		if (lastHeader != null && lastHeader.getStreamId() == packet.getStreamId()) {
			relativeTS = (int) (packet.getTimestamp() - lastHeader.getTimestamp());
			headerType++;
			if (lastHeader.getSize() == packet.getSize()) {
				headerType++;
				if (lastHeader.getRelativeTS() == relativeTS) {
					headerType++;
				}
			}
		}
		writeChannelBytes(buf, headerType, packet.getChannel());
		switch (headerType) {
		case HEADER_STANDARD:
			BufferExUtils.writeMediumIntBE(buf, (int) packet.getTimestamp());
			BufferExUtils.writeMediumIntBE(buf, packet.getSize());
			buf.put((byte) packet.getType());
			BufferExUtils.writeIntLE(buf, packet.getStreamId());
			break;
		case HEADER_SAME_TARGET:
			BufferExUtils.writeMediumIntBE(buf, relativeTS);
			BufferExUtils.writeMediumIntBE(buf, packet.getSize());
			buf.put((byte) packet.getType());
			break;
		case HEADER_SAME_SIZE:
			BufferExUtils.writeMediumIntBE(buf, relativeTS);
			break;
		case HEADER_CONTINUE:
			// write nothing
			break;
		default:
			throw new RTMPCodecException("Impossible header type " + headerType);
		}
		RTMPHeader currentHeader = new RTMPHeader();
		RTMPUtils.copyHeader(currentHeader, packet);
		currentHeader.setHeaderType(headerType);
		currentHeader.setRelativeTS(relativeTS);
		return currentHeader;
	}
}
