package org.red5.server.common.rtmp.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.red5.server.common.BufferEx;
import org.red5.server.common.BufferExUtils;
import org.red5.server.common.amf.AMFInput;
import org.red5.server.common.rtmp.RTMPCodecException;
import org.red5.server.common.rtmp.RTMPCodecState;
import org.red5.server.common.rtmp.RTMPInput;
import org.red5.server.common.rtmp.packet.RTMPChunkSize;
import org.red5.server.common.rtmp.packet.RTMPHandshake;
import org.red5.server.common.rtmp.packet.RTMPHeader;
import org.red5.server.common.rtmp.packet.RTMPPacket;

public abstract class BaseRTMPInput implements RTMPInput {
	private static final int INTERNAL_BUF_CAPACITY = 1024;
	private static final int SUB_HEADER_LENGTH[] = { 11, 7, 3, 0 }; // without channel id
	
	protected ClassLoader defaultClassLoader;
	protected RTMPCodecState codecState;
	protected AMFInput amfInput;
	protected boolean isServerMode;
	
	protected BufferEx internalBuf;
	protected int chunkSize = 128;
	
	protected Map<Integer,RTMPHeader> lastHeaderMap =
		new HashMap<Integer,RTMPHeader>();
	
	private Map<Integer,RTMPPacketObject> decodingPacketMap =
		new HashMap<Integer,RTMPPacketObject>();
	
	private byte[] workingBuf = new byte[14];
	
	public BaseRTMPInput(boolean isServerMode) {
		codecState = RTMPCodecState.HANDSHAKE_1;
		this.isServerMode = isServerMode;
		this.internalBuf = BufferEx.allocate(INTERNAL_BUF_CAPACITY);
		// TODO initialize AMF input
		// this.amfInput = null;
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
	public RTMPPacket read(BufferEx buf, ClassLoader classLoader)
			throws RTMPCodecException {
		int originPos = buf.position();
		try {
			if (codecState == RTMPCodecState.HANDSHAKE_1 ||
					codecState == RTMPCodecState.HANDSHAKE_2) {
				return readHandshake(buf);
			} else {
				RTMPPacket packet = readPacket(buf, classLoader);
				if (packet instanceof RTMPChunkSize) {
					RTMPChunkSize chunkSizePacket = (RTMPChunkSize) packet;
					chunkSize = chunkSizePacket.getChunkSize();
				}
				return packet;
			}
		} catch (RTMPCodecException e) {
			buf.position(originPos);
			throw e;
		}
	}

	@Override
	public RTMPPacket read(BufferEx buf) throws RTMPCodecException {
		return read(buf, getClassLoader());
	}

	@Override
	public RTMPPacket[] readAll(BufferEx buf, ClassLoader classLoader)
			throws RTMPCodecException {
		int originPos = buf.position();
		try {
			ArrayList<RTMPPacket> packets = new ArrayList<RTMPPacket>();
			RTMPPacket packet = read(buf, classLoader);
			while (packet != null) {
				packets.add(packet);
				packet = read(buf, classLoader);
			}
			return (RTMPPacket[]) packets.toArray();
		} catch (RTMPCodecException e) {
			buf.position(originPos);
			throw e;
		}
	}

	@Override
	public RTMPPacket[] readAll(BufferEx buf) throws RTMPCodecException {
		return readAll(buf, getClassLoader());
	}

	@Override
	public void resetInput() {
		internalBuf.clear();
		amfInput.resetInput();
		lastHeaderMap.clear();
		decodingPacketMap.clear();
	}

	@Override
	public void setDefaultClassLoader(ClassLoader defaultClassLoader) {
		this.defaultClassLoader = defaultClassLoader;
	}
	
	protected RTMPHandshake readHandshake(BufferEx buf) throws RTMPCodecException {
		RTMPCodecState nextState;
		int handshakeSize;
		if (codecState == RTMPCodecState.HANDSHAKE_1) {
			nextState = RTMPCodecState.HANDSHAKE_2;
			handshakeSize = RTMPHandshake.HANDSHAKE_SIZE+1;
		} else if (codecState == RTMPCodecState.HANDSHAKE_2) {
			nextState = RTMPCodecState.GENERIC_RTMP;
			handshakeSize = RTMPHandshake.HANDSHAKE_SIZE;
		} else {
			throw new RTMPCodecException("Illegal state reading handshake");
		}
		RTMPHandshake handshake = null;
		int bytesRemaining = internalBuf.position() + buf.remaining();
		if (bytesRemaining >= handshakeSize) {
			byte[] handshakeBuf = new byte[handshakeSize];
			int internalBufLength = internalBuf.position();
			internalBuf.flip();
			internalBuf.get(handshakeBuf, 0, internalBufLength);
			internalBuf.clear();
			buf.get(handshakeBuf, internalBufLength, handshakeSize-internalBufLength);
			handshake = new RTMPHandshake();
			handshake.setHandshakeData(BufferEx.wrap(handshakeBuf));
			codecState = nextState;
			return handshake;
		} else {
			internalBuf.put(buf);
		}
		return handshake;
	}
	
	protected RTMPPacket readPacket(BufferEx buf, ClassLoader classLoader)
	throws RTMPCodecException {
		if (codecState != RTMPCodecState.GENERIC_RTMP) {
			throw new RTMPCodecException("Illegal state reading generic packet");
		}
		RTMPPacketObject packetObject = readChunk(buf);
		while (packetObject != null) {
			if (packetObject.isCompleteDecoding()) {
				packetObject.body.flip();
				return decodePacket(packetObject.header, packetObject.body, classLoader);
			} else {
				packetObject = readChunk(buf);
			}
		}
		return null;
	}
	
	protected abstract RTMPPacket decodePacket(
			RTMPHeader header, BufferEx body, ClassLoader classLoader)
	throws RTMPCodecException;
	
	private RTMPPacketObject readChunk(BufferEx buf) {
		int originInternalBufPos = internalBuf.position();
		int originBufPos = buf.position();
		internalBuf.flip();
		int bytesRemaining = internalBuf.remaining() + buf.remaining();
		if (bytesRemaining < 1) {
			internalBuf.limit(internalBuf.capacity());
			internalBuf.position(originInternalBufPos);
			return null;
		}
		byte channelByte0;
		if (internalBuf.remaining() >= 1) {
			channelByte0 = internalBuf.get();
		} else {
			channelByte0 = buf.get();
		}
		int bytesChannelId = 1;
		if ((channelByte0 & 0x3f) == 0) {
			bytesChannelId = 2;
		} else if ((channelByte0 & 0x3f) == 1) {
			bytesChannelId = 3;
		}
		int headerType = (channelByte0 & 0x3) >> 6;
		int bytesHeader = bytesChannelId + SUB_HEADER_LENGTH[headerType];
		if (bytesRemaining < bytesHeader) {
			internalBuf.limit(internalBuf.capacity());
			internalBuf.position(originInternalBufPos);
			buf.position(originBufPos);
			internalBuf.put(buf);
			return null;
		}
		internalBuf.position(originInternalBufPos);
		buf.position(originBufPos);
		if (internalBuf.remaining() >= bytesHeader) {
			internalBuf.put(workingBuf, 0, bytesHeader);
		} else {
			int internalRemaining = internalBuf.remaining();
			internalBuf.put(workingBuf, 0, internalRemaining);
			buf.put(workingBuf, internalRemaining, bytesHeader - internalRemaining);
		}
		int channelId;
		switch (bytesChannelId) {
		case 1:
			channelId = workingBuf[0] & 0x3f;
			break;
		case 2:
			channelId = 64 + (workingBuf[1] & 0x0ff);
			break;
		case 3:
		default:
			channelId = 64 + (workingBuf[1] & 0x0ff) + ((workingBuf[2] & 0x0ff) << 8);
			break;
		}
		BufferEx subHeaderBuf = BufferEx.wrap(workingBuf, bytesChannelId, SUB_HEADER_LENGTH[headerType]);
		RTMPPacketObject decodingPacket = decodingPacketMap.get(channelId);
		if (decodingPacket == null) {
			long timestamp;
			int relativeTS = 0;
			int size;
			int type;
			int streamId;
			RTMPHeader lastHeader = lastHeaderMap.get(channelId);
			if (headerType != 0 && lastHeader == null) {
				throw new RTMPCodecException("Last header not found parsing headerType " + headerType);
			}
			switch (headerType) {
			case 0:
				timestamp = BufferExUtils.readMediumIntBE(subHeaderBuf);
				size = BufferExUtils.readMediumIntBE(subHeaderBuf);
				type = subHeaderBuf.get() & 0x0ff;
				streamId = BufferExUtils.readMediumIntLE(subHeaderBuf);
				break;
			case 1:
				relativeTS = BufferExUtils.readMediumIntBE(subHeaderBuf);
				timestamp = lastHeader.getTimestamp() + relativeTS;
				size = BufferExUtils.readMediumIntBE(subHeaderBuf);
				type = subHeaderBuf.get() & 0x0ff;
				streamId = lastHeader.getStreamId();
				break;
			case 2:
				relativeTS = BufferExUtils.readMediumIntBE(subHeaderBuf);
				timestamp = lastHeader.getTimestamp() + relativeTS;
				size = lastHeader.getSize();
				type = lastHeader.getSize();
				streamId = lastHeader.getStreamId();
				break;
			case 3:
				relativeTS = lastHeader.getRelativeTS();
				timestamp = lastHeader.getTimestamp();
				size = lastHeader.getSize();
				type = lastHeader.getSize();
				streamId = lastHeader.getStreamId();
				break;
			default:
				// impossible value
				throw new RTMPCodecException("Impossible code path");
			}
			RTMPHeader currentHeader = new RTMPHeader();
			currentHeader.setChannel(channelId);
			currentHeader.setRelativeTS(relativeTS);
			currentHeader.setTimestamp(timestamp);
			currentHeader.setSize(size);
			currentHeader.setType(type);
			currentHeader.setStreamId(streamId);
			decodingPacket = new RTMPPacketObject(currentHeader);
			decodingPacketMap.put(channelId, decodingPacket);
		}
		// try to read remaining bytes in the chunk
		int bytesToRead =
			decodingPacket.remaining() > this.chunkSize ?
					this.chunkSize : decodingPacket.remaining();
		bytesToRead -= internalBuf.remaining();
		if (buf.remaining() < bytesToRead) {
			internalBuf.limit(internalBuf.capacity());
			internalBuf.position(originInternalBufPos);
			buf.position(originBufPos);
			internalBuf.put(buf);
			return null;
		}
		decodingPacket.body.put(internalBuf);
		BufferExUtils.getBufferByLength(decodingPacket.body, buf, bytesToRead);
		if (decodingPacket.remaining() == 0) {
			decodingPacketMap.remove(channelId);
			lastHeaderMap.put(channelId, decodingPacket.header);
			return decodingPacket;
		} else {
			internalBuf.clear();
			return null;
		}
	}
	
	private ClassLoader getClassLoader() {
		if (defaultClassLoader != null) {
			return defaultClassLoader;
		} else {
			return Thread.currentThread().getContextClassLoader();
		}
	}
	
	private class RTMPPacketObject {
		public RTMPHeader header;
		public BufferEx body;
		
		public RTMPPacketObject(RTMPHeader header) {
			this.header = header;
			body = BufferEx.allocate(header.getSize());
			body.setAutoExpand(false);
		}
		
		public boolean isCompleteDecoding() {
			if (body.position() == header.getSize()) {
				return true;
			} else {
				return false;
			}
		}
		
		public int remaining() {
			int bodyPos = body.position();
			return header.getSize() - bodyPos;
		}
	}
}
