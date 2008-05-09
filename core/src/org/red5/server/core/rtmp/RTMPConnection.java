package org.red5.server.core.rtmp;

import java.util.Map;

import org.red5.server.common.rtmp.packet.RTMPHandshake;
import org.red5.server.common.rtmp.packet.RTMPPacket;
import org.red5.server.common.service.ServiceCall;
import org.red5.server.common.service.ServiceCallback;

public interface RTMPConnection {
	public static final int RTMP_CONN_STATE_DISCONNECTED   = -1;
	public static final int RTMP_CONN_STATE_INIT           = 0;
	public static final int RTMP_CONN_STATE_HANDSHAKE      = 1;
	public static final int RTMP_CONN_STATE_CONNECTING     = 2;
	public static final int RTMP_CONN_STATE_CONNECTED      = 3;
	
	// Official connect parameter keys
	public static final String CONNECT_PARAM_KEY_APP       = "app";
	public static final String CONNECT_PARAM_KEY_TCURL     = "tcUrl";
	public static final String CONNECT_PARAM_KEY_SWFURL    = "swfUrl";
	public static final String CONNECT_PARAM_KEY_ENCODING  = "objectEncoding";
	public static final String CONNECT_PARAM_KEY_FLASHVER  = "flashVer";
	// Red5 specific connect parameter keys
	public static final String CONNECT_PARAM_KEY_PROTOCOL    = "red5.protocol";
	public static final String CONNECT_PARAM_KEY_HOST        = "red5.host";
	public static final String CONNECT_PARAM_KEY_PORT        = "red5.port";
	public static final String CONNECT_PARAM_KEY_APP_NAME    = "red5.app";
	public static final String CONNECT_PARAM_KEY_APP_INST    = "red5.appInst";
	public static final String CONNECT_PARAM_KEY_QUERYSTRING = "red5.queryString";
	
	int getState();
	int getConnectionId();
	void writePacket(RTMPPacket packet);
	
	/**
	 * Called when handshake is received. The method should
	 * not change the internal state of handshake buffer.
	 * 
	 * @param handshake
	 * @return The state after receiving this handshake.
	 */
	int onReceiveHandshake(RTMPHandshake handshake);
	
	/**
	 * Associate the connection to the application instance.
	 * Switch the connection state to CONNECTED mode. The implementation
	 * should acquire the instance from application object to avoid
	 * it from being released.
	 * 
	 * @param appInstance
	 * @param connectionParams The connection parameters.
	 * @return <tt>true</tt> successfully connect to app instance,
	 * <tt>false</tt> otherwise.
	 */
	boolean connectToAppInstance(RTMPApplicationInstance appInstance, Map<String,Object> connectionParams);
	RTMPApplicationInstance getApplicationInstance();
	Map<String,Object> getConnectionParams();
	
	long call(ServiceCall<Object> call, int channel, ServiceCallback callback);
	
	void close();
	
	// TODO used by core handler only, need more modularized?
	void onResult(long invokeId, Object result);
	void onError(long invokeId, Object error);
}
