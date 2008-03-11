/**
 * 
 */
package org.red5.myapp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.server.net.rtmp.EmbeddedRTMPClient;
import org.red5.server.net.rtmp.RTMPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ptamilarasan
 * 
 */
public class Application extends MultiThreadedApplicationAdapter {

	private EmbeddedRTMPClient client = new EmbeddedRTMPClient();
	private static final Logger log = LoggerFactory
			.getLogger(Application.class);

	private String remoteIP;
	private Integer remotePort;
	
	public Application() {
		log.info("Started Omnovia application v0.1 build 03102008");
	}

	@Override
	public boolean connect(IConnection conn, IScope scope, Object[] params) {
		log.debug("Connect called");
		// Check if the user passed valid parameters
		if (params == null || params.length == 0) {
			log.debug("No parameters passed");
		} else {
			log.debug("Params length: {}", params.length);
		}
		// Call original method of parent class
		if (!super.connect(conn, scope, params)) {
			log.warn("Super class connect returned false");
			return false;
		}

		String clientId = conn.getClient().getId();
		log.debug("Client id: {}", clientId);
    	
		return true;
	}	
	
	@Override	
    public boolean appConnect(IConnection conn, Object[] params) {
        log.debug("appConnect - client id: {}", conn.getClient().getId());
        if (log.isDebugEnabled()) {
	        // getting client parameters
	        Map<String, Object> properties = conn.getConnectParams();
	        for(Map.Entry<String, Object> e : properties.entrySet()) {
	            log.debug("{} = {}", e.getKey(), e.getValue());
	        }        
        }
        return true;
    }	
    
	@Override
	public boolean appStart(IScope app) {
		log.info("application started...");
		return true;
	}

	@Override
	public boolean roomConnect(IConnection conn, Object[] params) {
		log.info("Client {} has connected to room.", conn.getClient().getId());
		return true;
	}

	@Override
	public void disconnect(IConnection conn, IScope scope) {
		log.debug("Disconnect called");	
		super.disconnect(conn, scope);
	}		
	
	public void createClient() {
		log.debug("Creating client - ip: {} port: {}", remoteIP, remotePort);
		client.connect(remoteIP, remotePort, "myapp", new ConnectCallback());
	}

	// Functions called by client 1 that then invoke a function on the server
	// through client 2 (RTMPClient)
	public void testInvokeNoParams() {
		log.debug("Test invokeNoParams");		
		client.invoke("invokeNoParams", new InvokeCallback());
	}

	public void testInvokeParams() {
		log.debug("Test invokeParams");
		client.invoke("invokeParams", new Object[] { 1, 2, 3 },
				new InvokeCallback());
	}

	// Functions invoked from RTMPClient
	public void invokeNoParams() {
		log.info("invokeNoParams() called.");
	}

	public void invokeParams(int a, int b, int c) {
		log.info("Client passed args: {}, {}, {}.", new Object[] { a, b, c });
	}

	public String getRemoteIP() {
		return remoteIP;
	}

	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}

	public Integer getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(Integer remotePort) {
		this.remotePort = remotePort;
	}	
	
	// Callback classes
	public class ConnectCallback implements IPendingServiceCallback {
		public void resultReceived(IPendingServiceCall call) {
			log.info("The connection completed: {}", call.getResult());
		}
	}

	public class InvokeCallback implements IPendingServiceCallback {
		public void resultReceived(IPendingServiceCall call) {
			log.info("The call was completed: {}", call.getResult());
		}
	}

}
