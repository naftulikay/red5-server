package org.red5.server.core.rtmp;

import org.red5.server.common.rtmp.packet.RTMPPacket;
import org.red5.server.common.service.ServiceCall;
import org.red5.server.common.service.ServiceCallback;

public interface RTMPConnection {
	public static final int RTMP_CONN_STATE_DISCONNECTED   = -1;
	public static final int RTMP_CONN_STATE_INIT           = 0;
	public static final int RTMP_CONN_STATE_HANDSHAKE      = 1;
	public static final int RTMP_CONN_STATE_CONNECTING     = 2;
	public static final int RTMP_CONN_STATE_CONNECTED      = 3;
	
	int getState();
	void setState(int state);
	int getConnectionId();
	void writePacket(RTMPPacket packet);
	
	RTMPApplicationInstance getApplicationInstance();
	
	long call(ServiceCall<Object> call, int channel, ServiceCallback callback);
	
	void close();
	
	// TODO used by core handler only, need more modularized?
	void onResult(long invokeId, Object result, int channel);
	void onError(long invokeId, Object error, int channel);
}
