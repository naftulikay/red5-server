/**
 * 
 */
package org.red5.myapp;

import java.util.HashMap;
import java.util.Map;

import org.red5.io.amf3.ByteArray;
import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.Red5;
import org.red5.server.api.service.IPendingServiceCall;
import org.red5.server.api.service.IPendingServiceCallback;
import org.red5.server.api.service.IServiceCapableConnection;
import org.red5.server.api.service.ServiceUtils;
import org.red5.server.net.rtmp.EmbeddedRTMPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test application for the RTMPClient
 * 
 * @author ptamilarasan (Original author)
 * @author Paul Gregoire 
 */
public class Application extends MultiThreadedApplicationAdapter {

	private EmbeddedRTMPClient client = null;
	private static final Logger log = LoggerFactory
			.getLogger(Application.class);

	private String remoteIP;
	private Integer remotePort;
	
	public Application() {
		log.info("Started application build 03112008");
	}

	@Override
	public boolean connect(IConnection conn, IScope scope, Object[] params) {
		log.info("connect called");
		// Check if the user passed valid parameters
		if (params == null || params.length == 0) {
			log.info("No parameters passed");
		} else {
			log.info("Params length: {}", params.length);
	        // getting client parameters
	        Map<String, Object> properties = conn.getConnectParams();
	        for(Map.Entry<String, Object> e : properties.entrySet()) {
	            log.info("Connection parameter: {} = {}", e.getKey(), e.getValue());
	        } 			
	        for(Object p : params) {
	        	log.info("Parameter class: {}", p.getClass().getName());
	            log.info("Parameter: {}", p.toString());
	        } 			
		}
		// Call original method of parent class
		if (!super.connect(conn, scope, params)) {
			log.warn("Super class connect returned false");
			return false;
		}

		String clientId = conn.getClient().getId();
		log.info("Client id: {}", clientId);
    	
		return true;
	}	
	
	@Override	
    public boolean appConnect(IConnection conn, Object[] params) {
        log.info("appConnect - client id: {}", conn.getClient().getId());
        if (log.isDebugEnabled()) {
	        // getting client parameters
	        Map<String, Object> properties = conn.getConnectParams();
	        for(Map.Entry<String, Object> e : properties.entrySet()) {
	            log.info("Parameter: {} = {}", e.getKey(), e.getValue());
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
		log.info("Disconnect called");	
		//if its a due to a browser close or error make sure the client is disconnected as well
		if (client != null) {
			client.disconnect();
		}
		super.disconnect(conn, scope);
	}		
	
	public void createClient(Integer amfVersion) {
		log.info("Creating client - ip: {} port: {} amf: {}", new Object[]{remoteIP, remotePort, amfVersion});
		
		client = new EmbeddedRTMPClient();
		client.setObjectEncoding(amfVersion);
		client.connect(remoteIP, remotePort, "myapp", new ConnectCallback());
	}

	public void createClientWithConnectParams(Integer amfVersion) {
		log.info("Creating client - ip: {} port: {}", remoteIP, remotePort);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("app", "myapp");
		params.put("tcUrl", "rtmp://"+remoteIP+':'+remotePort+"/myapp");
		params.put("objectEncoding", amfVersion);
		params.put("fpad", Boolean.FALSE);
		params.put("flashVer", "WIN 9,0,115,0");
		params.put("audioCodecs", Integer.valueOf(1639)); 
		params.put("videoFunction", Integer.valueOf(1)); 
		params.put("pageUrl", null);
		params.put("path", "myapp");
		params.put("capabilities", Integer.valueOf(15)); 
		params.put("swfUrl", null);
		params.put("videoCodecs", Integer.valueOf(252)); 		
		// extra parameters
		params.put("var1", "A string");
		params.put("var2", Integer.valueOf(33));
		params.put("var3", Boolean.TRUE);
		
		client = new EmbeddedRTMPClient();		
		client.setObjectEncoding(amfVersion);	
		client.connect(remoteIP, remotePort, params, new ConnectCallback());
	}	
	
	public void destroyClient() {
		log.info("Destroy client");
		client.disconnect();
	}	
	
	public void testEcho(String message) {
		log.info("Test echo");
		IConnection conn = Red5.getConnectionLocal();
		log.info("Connection: {}", conn);
		log.info("ServiceCapable? {}", (conn instanceof IServiceCapableConnection));
		ServiceUtils.invokeOnConnection(conn, "onResponse", new Object[]{message});		
	}
	
	// Functions called by client 1 that then invoke a function on the server
	// through client 2 (RTMPClient)
	public void testInvokeNoParams() {
		log.info("Test invokeNoParams");		
		client.invoke("invokeNoParams", new InvokeCallback());
	}

	public void testInvokeParams() {
		log.info("Test invokeParams");
		client.invoke("invokeParams", new Object[] { 1, 2, 3 },	new InvokeCallback());
	}

	public void testInvokeNoParamsWithResult() {
		log.info("Test invokeNoParams with Result");		
		client.invoke("invokeNoParamsWithResult", new InvokeCallback(Red5.getConnectionLocal(), "onResponse"));
	}
	
	public void testInvokeAMF3Params() {
		log.info("Test invokeAMF3Params");
		ByteArray b = new ByteArray();
		b.writeByte((byte)1);
		b.position(0);
		client.invoke("invokeAMF3Params", new Object[] { b },	new InvokeCallback());
	}

	// Functions invoked from RTMPClient
	public void invokeNoParams() {
		log.info("invokeNoParams called");
	}

	public void invokeParams(int a, int b, int c) {
		log.info("invokeParams called - client passed args: {}, {}, {}.", new Object[] { a, b, c });
	}

	public String invokeNoParamsWithResult() {
		log.info("invokeNoParamsWithResult called");
		return "Ok";
	}	
	
	public void invokeAMF3Params(ByteArray b) {
		log.info("InvokeAMF3Params");
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
		
		private IConnection connection;
		private String methodName;
		
		public InvokeCallback() {			
			log.info("Creating invoke callback without a method name or connection");
		}

		public InvokeCallback(IConnection connection, String methodName) {	
			this.connection = connection;
			this.methodName = methodName;
			log.info("Creating invoke callback with - method name: {}, connection: {}", methodName, connection);
		}
		
		public void resultReceived(IPendingServiceCall call) {
			log.info("The call: {} was completed with result: {}", call.getResult());
			//if connection is not null then communicate back to the caller
			if (connection != null) {
				//send a result back to the original caller
				if (ServiceUtils.invokeOnConnection(connection, methodName, new Object[]{call.getResult()})) {
					log.info("Method call to flex/flash client suceeded");
				} else {
					log.info("Method call to flex/flash client failed");					
				}
			}
		}
	}

}
