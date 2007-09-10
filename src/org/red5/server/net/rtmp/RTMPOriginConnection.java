package org.red5.server.net.rtmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ByteBuffer;
import org.red5.server.api.scheduling.ISchedulingService;
import org.red5.server.net.mrtmp.IMRTMPConnection;
import org.red5.server.net.mrtmp.IMRTMPOriginManager;
import org.red5.server.net.mrtmp.OriginMRTMPHandler;
import org.red5.server.net.rtmp.codec.RTMP;
import org.red5.server.net.rtmp.message.Packet;

/**
 * A pseudo-connection on Origin that represents a client
 * on Edge.
 * The connection is created behind a MRTMP connection so
 * no handshake job or keep-alive job is necessary. No raw byte
 * data write is needed either.
 * @author Steven Gong (steven.gong@gmail.com)
 * @version $Id$
 */
public class RTMPOriginConnection extends RTMPConnection {
	private static final Log log = LogFactory.getLog(RTMPOriginConnection.class);
	
	private int clientId;
	private IMRTMPOriginManager mrtmpManager;
	private OriginMRTMPHandler handler;
	private RTMP state;

	public RTMPOriginConnection(int clientId) {
		super(PERSISTENT);
		this.clientId = clientId;
		state = new RTMP(RTMP.MODE_SERVER);
		state.setState(RTMP.STATE_CONNECTED);
	}

	public void setMrtmpManager(IMRTMPOriginManager mrtmpManager) {
		this.mrtmpManager = mrtmpManager;
	}

	public void setHandler(OriginMRTMPHandler handler) {
		this.handler = handler;
	}

	public RTMP getState() {
		return state;
	}

	@Override
	protected void onInactive() {
		// Edge already tracks the activity
		// no need to do again here.
	}

	@Override
	public void rawWrite(ByteBuffer out) {
		// won't write any raw data on the wire
		// XXX should we throw exception here
		// to indicate an abnormal state ?
		log.warn("Erhhh... Raw write. Shouldn't be in here!");
	}

	@Override
	public void write(Packet packet) {
		IMRTMPConnection conn = mrtmpManager.lookupMRTMPConnection(clientId);
		if (conn == null) {
			// the connect is gone
			log.debug("Client " + clientId + " is gone!");
			return;
		}
		mrtmpManager.setAfinity(conn, clientId);
		log.debug("Origin writing packet to client " + clientId + ":" + packet.getMessage());
		conn.write(clientId, packet);
	}

	@Override
	public void startRoundTripMeasurement() {
		// Edge already tracks the RTT
		// no need to track RTT here.
	}

	@Override
	protected void startWaitForHandshake(ISchedulingService service) {
		// no handshake in MRTMP, simply ignore
	}

	@Override
	public void close() {
		handler.closeConnection(clientId);
	}
	
	synchronized public void realClose() {
		if (state.getState() != RTMP.STATE_DISCONNECTED) {
			state.setState(RTMP.STATE_DISCONNECTED);
			super.close();
		}
	}
}
