package org.red5.server.connector.mina;

import org.apache.mina.common.IoBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.red5.server.common.BufferEx;
import org.red5.server.common.rtmp.RTMPInput;
import org.red5.server.common.rtmp.packet.RTMPPacket;

public class RTMPMinaProtocolDecoder implements ProtocolDecoder {

	@Override
	public void decode(IoSession session, IoBuffer ioBuffer, ProtocolDecoderOutput output)
			throws Exception {
		RTMPMinaConnection connection =
			(RTMPMinaConnection) session.getAttribute(RTMPMinaIoHandler.CONNECTION_OBJ_KEY);
		RTMPInput rtmpInput = (RTMPInput) connection.getRTMPInput();
		RTMPPacket[] packets = rtmpInput.readAll(BufferEx.wrap(ioBuffer.buf()));
		for (RTMPPacket packet : packets) {
			output.write(packet);
		}
		output.flush();
	}

	@Override
	public void dispose(IoSession session) throws Exception {
		// do nothing
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput output)
			throws Exception {
		// do nothing
	}

}
