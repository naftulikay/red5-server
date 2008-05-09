package org.red5.server.connector.mina;

import org.apache.mina.common.IoBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.red5.server.common.BufferEx;
import org.red5.server.common.rtmp.RTMPOutput;
import org.red5.server.common.rtmp.packet.RTMPPacket;

public class RTMPMinaProtocolEncoder implements ProtocolEncoder {

	@Override
	public void dispose(IoSession session) throws Exception {
		// do nothing
	}

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput output)
			throws Exception {
		RTMPMinaConnection connection =
			(RTMPMinaConnection) session.getAttribute(RTMPMinaIoHandler.CONNECTION_OBJ_KEY);
		RTMPPacket packet = (RTMPPacket) message;
		RTMPOutput rtmpOutput = connection.getRTMPOutput();
		BufferEx buf = rtmpOutput.write(packet);
		output.write(IoBuffer.wrap(buf.buf()));
		output.flush();
	}

}
