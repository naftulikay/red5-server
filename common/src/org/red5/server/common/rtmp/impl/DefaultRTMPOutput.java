package org.red5.server.common.rtmp.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.red5.server.common.BufferEx;
import org.red5.server.common.amf.AMFConstants;
import org.red5.server.common.amf.AMFOutput;
import org.red5.server.common.amf.AMFType;
import org.red5.server.common.rtmp.packet.RTMPAudio;
import org.red5.server.common.rtmp.packet.RTMPBytesRead;
import org.red5.server.common.rtmp.packet.RTMPChunkSize;
import org.red5.server.common.rtmp.packet.RTMPClientBW;
import org.red5.server.common.rtmp.packet.RTMPFlexMessage;
import org.red5.server.common.rtmp.packet.RTMPFlexSharedObjectMessage;
import org.red5.server.common.rtmp.packet.RTMPFlexStreamSend;
import org.red5.server.common.rtmp.packet.RTMPInvoke;
import org.red5.server.common.rtmp.packet.RTMPNotify;
import org.red5.server.common.rtmp.packet.RTMPPacket;
import org.red5.server.common.rtmp.packet.RTMPPing;
import org.red5.server.common.rtmp.packet.RTMPServerBW;
import org.red5.server.common.rtmp.packet.RTMPSharedObject;
import org.red5.server.common.rtmp.packet.RTMPSharedObjectMessage;
import org.red5.server.common.rtmp.packet.RTMPUnknown;
import org.red5.server.common.rtmp.packet.RTMPVideo;

public class DefaultRTMPOutput
extends BaseRTMPOutput implements AMFConstants {
	protected AMFOutput amfOutput;
	private int objectEncoding;

	public DefaultRTMPOutput(boolean isServerMode) {
		super(isServerMode);
		// TODO initialize amf output
		// amfOutput = null;
	}

	public int getObjectEncoding() {
		return objectEncoding;
	}

	public void setObjectEncoding(int objectEncoding) {
		this.objectEncoding = objectEncoding;
	}

	@Override
	protected void encodePacketBody(BufferEx buf, RTMPPacket packet) {
		int originPos = buf.position();
		switch (packet.getType()) {
		case RTMPPacket.TYPE_RTMP_CHUNK_SIZE:
			encodeChunkSize(buf, (RTMPChunkSize) packet);
			break;
		case RTMPPacket.TYPE_RTMP_BYTES_READ:
			encodeBytesRead(buf, (RTMPBytesRead) packet);
			break;
		case RTMPPacket.TYPE_RTMP_PING:
			encodePing(buf, (RTMPPing) packet);
			break;
		case RTMPPacket.TYPE_RTMP_SERVER_BW:
			encodeServerBW(buf, (RTMPServerBW) packet);
			break;
		case RTMPPacket.TYPE_RTMP_CLIENT_BW:
			encodeClientBW(buf, (RTMPClientBW) packet);
			break;
		case RTMPPacket.TYPE_RTMP_AUDIO:
			encodeAudio(buf, (RTMPAudio) packet);
			break;
		case RTMPPacket.TYPE_RTMP_VIDEO:
			encodeVideo(buf, (RTMPVideo) packet);
			break;
		case RTMPPacket.TYPE_RTMP_FLEX_STREAM_SEND:
			encodeFlexStreamSend(buf, (RTMPFlexStreamSend) packet);
			break;
		case RTMPPacket.TYPE_RTMP_FLEX_SHARED_OBJECT:
			encodeFlexSharedObject(buf, (RTMPFlexSharedObjectMessage) packet);
			break;
		case RTMPPacket.TYPE_RTMP_FLEX_MESSAGE:
			encodeFlexMessage(buf, (RTMPFlexMessage) packet);
			break;
		case RTMPPacket.TYPE_RTMP_NOTIFY:
			encodeNotify(buf, (RTMPNotify) packet);
			break;
		case RTMPPacket.TYPE_RTMP_SHARED_OBJECT:
			encodeSharedObject(buf, (RTMPSharedObjectMessage) packet);
			break;
		case RTMPPacket.TYPE_RTMP_INVOKE:
			encodeInvoke(buf, (RTMPInvoke) packet);
			break;
		default:
			encodeUnknown(buf, (RTMPUnknown) packet);
			break;
		}
		packet.setSize(buf.position() - originPos);
	}

	protected void encodeChunkSize(BufferEx buf, RTMPChunkSize packet) {
		buf.putInt(packet.getChunkSize());
	}

	protected void encodeBytesRead(BufferEx buf, RTMPBytesRead packet) {
		buf.putInt(packet.getBytesRead());
	}

	protected void encodePing(BufferEx buf, RTMPPing packet) {
		buf.putShort(packet.getPingType());
		buf.putInt(packet.getParam0());
		if (packet.getParam1() != RTMPPing.PING_PARAM_UNDEFINED) {
			buf.putInt(packet.getParam1());
			if (packet.getParam2() != RTMPPing.PING_PARAM_UNDEFINED) {
				buf.putInt(packet.getParam2());
			}
		}
	}

	protected void encodeServerBW(BufferEx buf, RTMPServerBW packet) {
		buf.putInt(packet.getBandwidth());
	}

	protected void encodeClientBW(BufferEx buf, RTMPClientBW packet) {
		buf.putInt(packet.getBandwidth());
		buf.put(packet.getValue2());
	}

	protected void encodeAudio(BufferEx buf, RTMPAudio packet) {
		buf.put(packet.getAudioData());
	}

	protected void encodeVideo(BufferEx buf, RTMPVideo packet) {
		buf.put(packet.getVideoData());
	}

	protected void encodeFlexStreamSend(BufferEx buf, RTMPFlexStreamSend packet) {
		encodeNotifyOrInvoke(buf, packet);
	}

	protected void encodeFlexSharedObject(BufferEx buf,
			RTMPFlexSharedObjectMessage packet) {
		if (objectEncoding == AMF_MODE_3) {
			buf.put((byte)3);
			amfOutput.setOutputMode(AMF_MODE_3);
		} else {
			buf.put((byte)0);
			amfOutput.setOutputMode(AMF_MODE_0);
		}
		doEncodeSharedObject(buf, packet);
	}

	protected void encodeFlexMessage(BufferEx buf, RTMPFlexMessage packet) {
		encodeNotifyOrInvoke(buf, packet);
	}

	protected void encodeNotify(BufferEx buf, RTMPNotify packet) {
		encodeNotifyOrInvoke(buf, packet);
	}

	protected void encodeSharedObject(BufferEx buf, RTMPSharedObjectMessage packet) {
		amfOutput.setOutputMode(AMF_MODE_0);
		doEncodeSharedObject(buf, packet);
	}

	protected void encodeInvoke(BufferEx buf, RTMPInvoke packet) {
		encodeNotifyOrInvoke(buf, packet);
	}

	protected void encodeUnknown(BufferEx buf, RTMPUnknown packet) {
		buf.put(packet.getUnknownBody());
	}

	private void encodeNotifyOrInvoke(BufferEx buf, RTMPNotify notify) {
		boolean isInvoke = notify instanceof RTMPInvoke;
		int outputEncoding = objectEncoding;
		amfOutput.setOutputMode(AMF_MODE_0);
		amfOutput.write(buf, notify.getAction(), AMFType.AMF_STRING);
		if (isInvoke) {
			RTMPInvoke invoke = (RTMPInvoke) notify;
			amfOutput.write(buf, invoke.getInvokeId(), AMFType.AMF_NUMBER);
			Map<String,Object> connectionParams =
				invoke.getConnectionParams();
			if (connectionParams != null && connectionParams.size() != 0) {
				amfOutput.write(buf, connectionParams, AMFType.AMF_OBJECT);
			} else {
				amfOutput.write(buf, null, AMFType.AMF_NULL);
			}
			if (invoke.getReplyTo() != null && "connect".equals(invoke.getReplyTo().getAction())) {
				outputEncoding = AMF_MODE_0;
			}
		}
		amfOutput.setOutputMode(outputEncoding);
		for (Object arg : notify.getArguments()) {
			amfOutput.write(buf, arg, null); // use auto java->amf mapping
		}
	}
	
	@SuppressWarnings("unchecked")
	private void doEncodeSharedObject(BufferEx buf,
			RTMPSharedObjectMessage soMessage) {
		amfOutput.writeString(buf, soMessage.getName());
		buf.putInt(soMessage.getVersion());
		buf.putInt(soMessage.isPersistent() ? 2 : 0);
		buf.putInt(0); // XXX unknown 4 bytes
		List<RTMPSharedObject> sharedObjects = soMessage.getSharedObjects();
		for (RTMPSharedObject so : sharedObjects) {
			buf.putInt(so.getSoType());
			int lengthPos = buf.position();
			buf.putInt(0); // place-holder, will be back later
			switch (so.getSoType()) {
			case RTMPSharedObject.SERVER_CONNECT:
			case RTMPSharedObject.CLIENT_INITIAL_DATA:
			case RTMPSharedObject.CLIENT_CLEAR_DATA:
				break;
			case RTMPSharedObject.SERVER_DELETE_ATTRIBUTE:
			case RTMPSharedObject.CLIENT_DELETE_DATA:
			case RTMPSharedObject.CLIENT_UPDATE_ATTRIBUTE:
				amfOutput.writeString(buf, so.getKey());
				break;
			case RTMPSharedObject.CLIENT_STATUS:
				amfOutput.writeString(buf, so.getKey());
				amfOutput.writeString(buf, (String) so.getValue());
				break;
			case RTMPSharedObject.SERVER_SET_ATTRIBUTE:
			case RTMPSharedObject.CLIENT_UPDATE_DATA:
				// XXX should so.getKey() == null ?
				if (so.getKey() == null) {
					Map<String,Object> valueMap =
						(Map<String,Object>) so.getValue();
					for (Entry<String, Object> entry : valueMap.entrySet()) {
						amfOutput.writeString(buf, entry.getKey());
						amfOutput.write(buf, entry.getValue(), null);
					}
				} else {
					amfOutput.writeString(buf, so.getKey());
					amfOutput.write(buf, so.getValue(), null);
				}
				break;
			case RTMPSharedObject.SEND_MESSAGE:
				amfOutput.write(buf, so.getKey(), AMFType.AMF_STRING);
				List<Object> valueList = (List<Object>) so.getValue();
				for (Object value : valueList) {
					amfOutput.write(buf, value, null);
				}
				break;
			default:
				amfOutput.writeString(buf, so.getKey());
				amfOutput.write(buf, so.getValue(), null);
				break;
			}
			int soEndPos = buf.position();
			int soLength = soEndPos - lengthPos - 4;
			buf.putInt(lengthPos, soLength);
			buf.position(soEndPos);
		}
	}
}
