package org.red5.server.connector.mina;

import java.util.Collections;
import java.util.Map;

import org.apache.mina.common.IoSession;
import org.red5.server.common.rtmp.RTMPInput;
import org.red5.server.common.rtmp.RTMPOutput;
import org.red5.server.common.rtmp.packet.RTMPHandshake;
import org.red5.server.common.rtmp.packet.RTMPPacket;
import org.red5.server.common.service.ServiceCall;
import org.red5.server.common.service.ServiceCallback;
import org.red5.server.core.rtmp.RTMPApplicationInstance;
import org.red5.server.core.rtmp.RTMPConnection;

public class RTMPMinaConnection implements RTMPConnection {
	private IoSession session;
	private int state;
	private int connectionId;
	private RTMPApplicationInstance appInstance;
	private Map<String,Object> connectionParams;
	
	public RTMPMinaConnection(IoSession session) {
		this.connectionId = hashCode();
		this.state = RTMP_CONN_STATE_INIT;
		this.session = session;
	}

	@Override
	public long call(ServiceCall<Object> call, int channel,
			ServiceCallback callback) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void close() {
		if (this.appInstance != null) {
			this.appInstance.release();
			this.appInstance = null;
		}
		this.state = RTMP_CONN_STATE_DISCONNECTED;
	}

	@Override
	public boolean connectToAppInstance(RTMPApplicationInstance appInstance,
			Map<String, Object> connectionParams) {
		if (state != RTMP_CONN_STATE_CONNECTING) {
			return false;
		}
		this.appInstance = appInstance;
		this.connectionParams = connectionParams;
		this.state = RTMP_CONN_STATE_CONNECTED;
		this.appInstance.acquire();
		return true;
	}

	@Override
	public RTMPApplicationInstance getApplicationInstance() {
		return appInstance;
	}

	@Override
	public Map<String, Object> getConnectionParams() {
		return Collections.unmodifiableMap(connectionParams);
	}

	@Override
	public int getConnectionId() {
		return connectionId;
	}

	@Override
	public int getState() {
		return state;
	}

	@Override
	public void onError(long invokeId, Object error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResult(long invokeId, Object result) {
		// TODO Auto-generated method stub

	}

	@Override
	public int onReceiveHandshake(RTMPHandshake handshake) {
		// TODO decide state change by handshake size
		if (state == RTMP_CONN_STATE_INIT) {
			state = RTMP_CONN_STATE_HANDSHAKE;
		} else if (state == RTMP_CONN_STATE_HANDSHAKE) {
			state = RTMP_CONN_STATE_CONNECTING;
		}
		return state;
	}
	
	@Override
	public void writePacket(RTMPPacket packet) {
		session.write(packet);
	}
	
	public RTMPInput getRTMPInput() {
		// TODO
		return null;
	}
	
	public RTMPOutput getRTMPOutput() {
		// TODO
		return null;
	}
}
