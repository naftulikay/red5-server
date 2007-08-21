package org.red5.server.net.mrtmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.red5.server.net.mrtmp.MRTMPPacket.RTMPBody;
import org.red5.server.net.mrtmp.MRTMPPacket.RTMPHeader;
import org.red5.server.net.rtmp.event.Invoke;
import org.red5.server.net.rtmp.message.Constants;
import org.red5.server.net.rtmpt.EdgeRTMPTConnection;
import org.red5.server.net.rtmpt.codec.EdgeRTMP;
import org.red5.server.service.Call;

public class EdgeMRTMPHandler extends IoHandlerAdapter
implements Constants {
	private static final Log log = LogFactory.getLog(EdgeMRTMPHandler.class);

	private IMRTMPEdgeManager mrtmpManager;
	private ProtocolCodecFactory codecFactory;
	
	public void setCodecFactory(ProtocolCodecFactory codecFactory) {
		this.codecFactory = codecFactory;
	}

	public void setMrtmpMananger(IMRTMPEdgeManager mrtmpMananger) {
		this.mrtmpManager = mrtmpMananger;
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		MRTMPPacket mrtmpPacket = (MRTMPPacket) message;
		int clientId = mrtmpPacket.getHeader().getClientId();
		EdgeRTMPTConnection conn = mrtmpManager.lookupRTMPTConnection(clientId);
		if (conn == null) {
			log.debug("Client " + clientId + " is already closed.");
			return;
		}
		EdgeRTMP rtmpState = (EdgeRTMP) conn.getState();
		switch (mrtmpPacket.getHeader().getType()) {
			case MRTMPPacket.CLOSE:
				// TODO current impl uses a private field
				// closing to indicate the "closing" state
				// we need to use EdgeRTMP state to track
				// the connection state consistently.
				conn.close();
				break;
			case MRTMPPacket.RTMP:
				RTMPHeader rtmpHeader = (RTMPHeader) mrtmpPacket.getHeader();
				RTMPBody rtmpBody = (RTMPBody) mrtmpPacket.getBody();
				boolean toDisconnect = false;
				synchronized (rtmpState) {
					if (rtmpState.getState() == EdgeRTMP.ORIGIN_CONNECT_FORWARDED &&
							rtmpHeader.getRtmpType() == TYPE_INVOKE) {
						// we got the connect invocation result from Origin
						// parse the result
						Invoke invoke = (Invoke) rtmpBody.getRtmpPacket().getMessage();
						if ("connect".equals(invoke.getCall().getServiceMethodName())) {
							if (invoke.getCall().getStatus() == Call.STATUS_SUCCESS_RESULT) {
								rtmpState.setState(EdgeRTMP.STATE_CONNECTED);
							} else {
								// TODO set EdgeRTMP state to closing ?
								toDisconnect = true;
							}
						}
					}
				}
				log.debug("Forward packet to client: " + rtmpBody.getRtmpPacket().getMessage());
				// send the packet back to client
				conn.write(rtmpBody.getRtmpPacket());
				if (toDisconnect) {
					conn.close();
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// do nothing
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		MRTMPEdgeConnection conn = (MRTMPEdgeConnection) session.getAttachment();
		// TODO we need to notify all the Edge RTMPT connections that
		// are assigned to this Origin so that the RTMPT connections
		// (1) Be closed, or
		// (2) Reassigned to another Origin
		mrtmpManager.unregisterConnection(conn);
		conn.close();
		log.debug("Closed MRTMP Edge Connection " + conn);
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		MRTMPEdgeConnection conn = new MRTMPEdgeConnection();
		conn.setIoSession(session);
		mrtmpManager.registerConnection(conn);
		session.setAttachment(conn);
		session.getFilterChain().addFirst("protocolFilter",
				new ProtocolCodecFilter(this.codecFactory));
		if (log.isDebugEnabled()) {
			session.getFilterChain().addLast("logger", new LoggingFilter());
		}
		log.debug("Created MRTMP Edge Connection " + conn);
	}

}
