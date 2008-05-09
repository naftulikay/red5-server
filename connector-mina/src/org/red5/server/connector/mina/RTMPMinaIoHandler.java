package org.red5.server.connector.mina;

import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.red5.server.common.rtmp.packet.RTMPPacket;
import org.red5.server.core.rtmp.RTMPConnectorHandler;

public class RTMPMinaIoHandler extends IoHandlerAdapter {
	public static final String CONNECTION_OBJ_KEY = "connection";
	
	private RTMPConnectorHandler rtmpConnectorHandler;
	private ProtocolCodecFactory codecFactory;

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		RTMPMinaConnection connection =
			(RTMPMinaConnection) session.getAttribute(CONNECTION_OBJ_KEY);
		rtmpConnectorHandler.onPacket(connection, (RTMPPacket) message);
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		RTMPMinaConnection connection =
			(RTMPMinaConnection) session.getAttribute(CONNECTION_OBJ_KEY);
		rtmpConnectorHandler.packetSent(connection, (RTMPPacket) message);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		RTMPMinaConnection connection =
			(RTMPMinaConnection) session.getAttribute(CONNECTION_OBJ_KEY);
		rtmpConnectorHandler.sessionClosed(connection);
		session.setAttribute(CONNECTION_OBJ_KEY, null);
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		IoFilterChain sessionFilterChain = session.getFilterChain();
		sessionFilterChain.addFirst("codecFactory",
				new ProtocolCodecFilter(codecFactory));
		sessionFilterChain.addLast("logging", new LoggingFilter());
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		RTMPMinaConnection connection = new RTMPMinaConnection(session);
		session.setAttribute(CONNECTION_OBJ_KEY, connection);
		rtmpConnectorHandler.sessionOpened(connection);
	}

	public void setRtmpConnectorHandler(RTMPConnectorHandler rtmpConnectorHandler) {
		this.rtmpConnectorHandler = rtmpConnectorHandler;
	}

	public void setCodecFactory(ProtocolCodecFactory codecFactory) {
		this.codecFactory = codecFactory;
	}
	
}
