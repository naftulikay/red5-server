package org.red5.server.common.rtmp.impl;

import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.red5.server.common.BufferEx;
import org.red5.server.common.amf.AMFInputOutputException;
import org.red5.server.common.amf.AMFMode;
import org.red5.server.common.amf.AMFUtils;
import org.red5.server.common.rtmp.RTMPCodecException;
import org.red5.server.common.rtmp.RTMPUtils;
import org.red5.server.common.rtmp.packet.RTMPAudio;
import org.red5.server.common.rtmp.packet.RTMPBytesRead;
import org.red5.server.common.rtmp.packet.RTMPChunkSize;
import org.red5.server.common.rtmp.packet.RTMPClientBW;
import org.red5.server.common.rtmp.packet.RTMPFlexMessage;
import org.red5.server.common.rtmp.packet.RTMPFlexStreamSend;
import org.red5.server.common.rtmp.packet.RTMPHeader;
import org.red5.server.common.rtmp.packet.RTMPInvoke;
import org.red5.server.common.rtmp.packet.RTMPNotify;
import org.red5.server.common.rtmp.packet.RTMPPacket;
import org.red5.server.common.rtmp.packet.RTMPPing;
import org.red5.server.common.rtmp.packet.RTMPServerBW;
import org.red5.server.common.rtmp.packet.RTMPSharedObject;
import org.red5.server.common.rtmp.packet.RTMPSharedObjectMessage;
import org.red5.server.common.rtmp.packet.RTMPUnknown;
import org.red5.server.common.rtmp.packet.RTMPVideo;

/**
 * The default implementation of RTMPInput.
 * 
 * @author Steven Gong (steven.gong@gmail.com)
 */
public class DefaultRTMPInput extends BaseRTMPInput {

	public DefaultRTMPInput(boolean isServerMode) {
		super(isServerMode);
	}

	@Override
	protected RTMPPacket decodePacket(RTMPHeader header, BufferEx body,
			ClassLoader classLoader) throws RTMPCodecException {
		RTMPPacket result;
		try {
			switch (header.getType()) {
			case RTMPPacket.TYPE_RTMP_CHUNK_SIZE:
				result = decodeChunkSize(header, body);
				break;
			case RTMPPacket.TYPE_RTMP_BYTES_READ:
				result = decodeBytesRead(header, body);
				break;
			case RTMPPacket.TYPE_RTMP_PING:
				result = decodePing(header, body);
				break;
			case RTMPPacket.TYPE_RTMP_SERVER_BW:
				result = decodeServerBW(header, body);
				break;
			case RTMPPacket.TYPE_RTMP_CLIENT_BW:
				result = decodeClientBW(header, body);
				break;
			case RTMPPacket.TYPE_RTMP_AUDIO:
				result = decodeAudio(header, body);
				break;
			case RTMPPacket.TYPE_RTMP_VIDEO:
				result = decodeVideo(header, body);
				break;
			case RTMPPacket.TYPE_RTMP_FLEX_STREAM_SEND:
				result = decodeFlexStreamSend(header, body, classLoader);
				break;
			case RTMPPacket.TYPE_RTMP_FLEX_SHARED_OBJECT:
				result = decodeFlexSharedObject(header, body, classLoader);
				break;
			case RTMPPacket.TYPE_RTMP_FLEX_MESSAGE:
				result = decodeFlexMessage(header, body, classLoader);
				break;
			case RTMPPacket.TYPE_RTMP_NOTIFY:
				result = decodeNotify(header, body, classLoader);
				break;
			case RTMPPacket.TYPE_RTMP_SHARED_OBJECT:
				result = decodeSharedObject(header, body, classLoader);
				break;
			case RTMPPacket.TYPE_RTMP_INVOKE:
				result = decodeInvoke(header, body, classLoader);
				break;
			default:
				result = decodeUnknown(header, body);
			break;
			}
			RTMPUtils.copyHeader(result, header);
			return result;
		} catch (RTMPCodecException e) {
			throw e;
		} catch (BufferUnderflowException e) {
			throw new RTMPCodecException(e);
		} catch (AMFInputOutputException e) {
			throw new RTMPCodecException(e);
		}
	}
	
	protected RTMPPacket decodeFlexSharedObject(RTMPHeader header, BufferEx body,
			ClassLoader classLoader) {
		AMFMode amfMode;
		byte amfEncoding = body.get();
		if (amfEncoding == 0) {
			amfMode = AMFMode.AMF0;
		} else if (amfEncoding == 3) {
			amfMode = AMFMode.AMF3;
		} else {
			throw new RTMPCodecException("Invalid Flex SO AMF encoding byte " + amfEncoding);
		}
		RTMPSharedObjectMessage soMessage = new RTMPSharedObjectMessage();
		doDecodeSharedObject(soMessage, header, body, classLoader, amfMode);
		return soMessage;
	}

	protected RTMPPacket decodeSharedObject(RTMPHeader header, BufferEx body,
			ClassLoader classLoader) {
		RTMPSharedObjectMessage soMessage = new RTMPSharedObjectMessage();
		doDecodeSharedObject(soMessage, header, body, classLoader, AMFMode.AMF0);
		return soMessage;
	}

	protected RTMPPacket decodeInvoke(RTMPHeader header, BufferEx body,
			ClassLoader classLoader) {
		RTMPInvoke invoke = new RTMPInvoke();
		decodeNotifyOrInvoke(invoke, header, body, classLoader);
		return invoke;
	}

	protected RTMPPacket decodeNotify(RTMPHeader header, BufferEx body,
			ClassLoader classLoader) {
		RTMPNotify notify = new RTMPNotify();
		decodeNotifyOrInvoke(notify, header, body, classLoader);
		return notify;
	}

	protected RTMPPacket decodeFlexMessage(RTMPHeader header, BufferEx body,
			ClassLoader classLoader) {
		RTMPFlexMessage flexMessage = new RTMPFlexMessage();
		// XXX skip unknown byte
		body.get();
		decodeNotifyOrInvoke(flexMessage, header, body, classLoader);
		return flexMessage;
	}

	protected RTMPPacket decodeFlexStreamSend(RTMPHeader header, BufferEx body,
			ClassLoader classLoader) {
		RTMPFlexStreamSend flexStreamSend = new RTMPFlexStreamSend();
		// TODO check whether it is encoded the same as RTMPNotify
		// XXX should skip unknown bytes?
		// body.get();
		decodeNotifyOrInvoke(flexStreamSend, header, body, classLoader);
		return flexStreamSend;
	}

	protected RTMPPacket decodeVideo(RTMPHeader header, BufferEx body) {
		RTMPVideo video = new RTMPVideo();
		video.setVideoData(body);
		return video;
	}

	protected RTMPPacket decodeAudio(RTMPHeader header, BufferEx body) {
		RTMPAudio audio = new RTMPAudio();
		audio.setAudioData(body);
		return audio;
	}

	protected RTMPPacket decodeClientBW(RTMPHeader header, BufferEx body) {
		RTMPClientBW clientBW = new RTMPClientBW();
		clientBW.setBandwidth(body.getInt());
		clientBW.setValue2(body.get());
		return clientBW;
	}

	protected RTMPPacket decodeServerBW(RTMPHeader header, BufferEx body) {
		RTMPServerBW serverBW = new RTMPServerBW();
		serverBW.setBandwidth(body.getInt());
		return serverBW;
	}

	protected RTMPPacket decodePing(RTMPHeader header, BufferEx body) {
		RTMPPing ping = new RTMPPing();
		ping.setPingType(body.getShort());
		ping.setParam0(body.getInt());
		if (body.remaining() > 0) {
			ping.setParam1(body.getInt());
		}
		if (body.remaining() > 0) {
			ping.setParam2(body.getInt());
		}
		return ping;
	}

	protected RTMPPacket decodeChunkSize(RTMPHeader header, BufferEx body) {
		RTMPChunkSize chunkSize = new RTMPChunkSize();
		chunkSize.setChunkSize(body.getInt());
		return chunkSize;
	}
	
	protected RTMPPacket decodeBytesRead(RTMPHeader header, BufferEx body) {
		RTMPBytesRead bytesRead = new RTMPBytesRead();
		bytesRead.setBytesRead(body.getInt());
		return bytesRead;
	}

	protected RTMPPacket decodeUnknown(RTMPHeader header, BufferEx body) {
		RTMPUnknown unknown = new RTMPUnknown(header.getType());
		unknown.setUnknownBody(body);
		return unknown;
	}

	@SuppressWarnings("unchecked")
	private void decodeNotifyOrInvoke(RTMPNotify notify,
			RTMPHeader header, BufferEx body, ClassLoader classLoader) {
		amfInput.resetInput();
		boolean isInvoke = notify instanceof RTMPInvoke;
		notify.setAction(amfInput.read(body, String.class));
		if (isInvoke) {
			RTMPInvoke invoke = (RTMPInvoke) notify;
			invoke.setInvokeId(amfInput.read(body, Number.class).longValue());
		}
		if (body.remaining() > 0) {
			if (isInvoke) {
				Object connectionParamsObj =
					amfInput.read(body, Object.class);
				Map<String,Object> connectionParams;
				if (AMFUtils.isAMFNull(connectionParamsObj)) {
					connectionParams = null;
				} else {
					connectionParams = (Map<String,Object>) connectionParamsObj;
				}
				RTMPInvoke invoke = (RTMPInvoke) notify;
				invoke.setConnectionParams(connectionParams);
			}
			List<Object> arguments = new ArrayList<Object>();
			while (body.remaining() > 0) {
				arguments.add(AMFUtils.amfReadObject(amfInput, body));
			}
		}
	}
	
	private void doDecodeSharedObject(RTMPSharedObjectMessage soMessage,
			RTMPHeader header, BufferEx body, ClassLoader classLoader, AMFMode amfMode) {
		amfInput.resetInput(amfMode);
		soMessage.setName(amfInput.read(body, String.class));
		soMessage.setVersion(body.getInt());
		soMessage.setPersistent(body.getInt() == 2);
		// unknown 4 bytes
		body.getInt();
		List<RTMPSharedObject> sharedObjects =
			new ArrayList<RTMPSharedObject>();
		while (body.remaining() > 0) {
			RTMPSharedObject so = new RTMPSharedObject(body.getInt());
			so.setSoLength(body.getInt());
			switch (so.getSoType()) {
			case RTMPSharedObject.CLIENT_STATUS:
				so.setKey(amfInput.read(body, String.class));
				so.setValue(amfInput.read(body, String.class));
				break;
			case RTMPSharedObject.CLIENT_UPDATE_DATA:
				so.setKey(null);
				Map<String,Object> valueMap = new HashMap<String,Object>();
				int start = body.position();
				while (body.position() - start < so.getSoLength()) {
					valueMap.put(amfInput.read(body, String.class),
							AMFUtils.amfReadObject(amfInput, body));
				}
				so.setValue(valueMap);
				break;
			case RTMPSharedObject.SEND_MESSAGE:
				so.setKey(amfInput.read(body, String.class));
				List<Object> valueList = new ArrayList<Object>();
				start = body.position();
				while (body.position() - start < so.getSoLength()) {
					valueList.add(AMFUtils.amfReadObject(amfInput, body));
				}
				so.setValue(valueList);
				break;
			default:
				start = body.position();
				so.setKey(amfInput.read(body, String.class));
				if (body.position() - start < so.getSoLength()) {
					so.setValue(AMFUtils.amfReadObject(amfInput, body));
				}
				break;
			}
			sharedObjects.add(so);
		}
		soMessage.setSharedObjects(sharedObjects);
	}
}
