package org.red5.server.net.mrtmp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.red5.server.net.rtmp.IRTMPHandler;
import org.red5.server.net.rtmp.RTMPOriginConnection;

public class OriginMRTMPHandler extends IoHandlerAdapter {
	private Log log = LogFactory.getLog(OriginMRTMPHandler.class);
	
	private IMRTMPOriginManager mrtmpManager;
	private ProtocolCodecFactory codecFactory;
	private IRTMPHandler handler;
	private Map<Integer, RTMPOriginConnection> connMap =
		new HashMap<Integer, RTMPOriginConnection>();
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	
	
	public void setMrtmpManager(IMRTMPOriginManager mrtmpManager) {
		this.mrtmpManager = mrtmpManager;
	}

	public void setHandler(IRTMPHandler handler) {
		this.handler = handler;
	}

	public void setCodecFactory(ProtocolCodecFactory codecFactory) {
		this.codecFactory = codecFactory;
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		MRTMPPacket packet = (MRTMPPacket) message;
		MRTMPPacket.Header header = packet.getHeader();
		MRTMPPacket.Body body = packet.getBody();
		if (log.isDebugEnabled()) {
			log.debug(packet);
		}
		int clientId = header.getClientId();
		MRTMPOriginConnection mrtmpConn = (MRTMPOriginConnection) session.getAttachment();
		// set the afinity so that the follow-up packets can be sent
		// via this mrtmp connection.
		mrtmpManager.setAfinity(mrtmpConn, clientId);
		switch (packet.getHeader().getType()) {
			case MRTMPPacket.CONNECT:
				lock.writeLock().lock();
				try {
					if (!connMap.containsKey(clientId)) {
						RTMPOriginConnection conn = new RTMPOriginConnection(header.getClientId());
						conn.setMrtmpManager(mrtmpManager);
						conn.setHandler(this);
						connMap.put(clientId, conn);
					} else {
						log.warn("Open an already existing origin connection!");
					}
				} finally {
					lock.writeLock().unlock();
				}
				break;
			case MRTMPPacket.CLOSE:
				closeConnection(clientId);
				break;
			case MRTMPPacket.RTMP:
				lock.readLock().lock();
				try {
					if (connMap.containsKey(clientId)) {
						RTMPOriginConnection conn = connMap.get(clientId);
						MRTMPPacket.RTMPBody rtmpBody = (MRTMPPacket.RTMPBody) body;
						handler.messageReceived(conn, conn.getState(), rtmpBody.getRtmpPacket());
					} else {
						log.warn("Handle on a non-existent origin connection!");
					}
				} finally {
					lock.readLock().unlock();
				}
				break;
			default:
				log.warn("Unknown mrtmp packet received!");
				break;
		}
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// do nothing
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		MRTMPOriginConnection conn = (MRTMPOriginConnection) session.getAttachment();
		// TODO we need to handle the case when all MRTMP connection
		// is broken.
		mrtmpManager.unregisterConnection(conn);
		conn.close();
		log.debug("Closed MRTMP Origin Connection " + conn);
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		MRTMPOriginConnection conn = new MRTMPOriginConnection();
		conn.setIoSession(session);
		mrtmpManager.registerConnection(conn);
		session.setAttachment(conn);
		session.getFilterChain().addFirst("protocolFilter",
				new ProtocolCodecFilter(this.codecFactory));
		if (log.isDebugEnabled()) {
			session.getFilterChain().addLast("logger", new LoggingFilter());
		}
		log.debug("Created MRTMP Origin Connection " + conn);
	}

	public void closeConnection(int clientId) {
		lock.writeLock().lock();
		try {
			if (connMap.containsKey(clientId)) {
				RTMPOriginConnection conn = connMap.get(clientId);
				connMap.remove(clientId);
				conn.realClose();
			} else {
				log.warn("Close a non-existent origin connection!");
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
}
