package org.red5.server.connector.mina;

import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class RTMPMinaCodecFactory implements ProtocolCodecFactory {
	private RTMPMinaProtocolDecoder decoder = new RTMPMinaProtocolDecoder();
	private RTMPMinaProtocolEncoder encoder = new RTMPMinaProtocolEncoder();

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return encoder;
	}

}
