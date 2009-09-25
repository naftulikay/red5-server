package org.red5.server.plugin.icy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.red5.server.plugin.icy.parser.NSVStreamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SHOUTcast / ICY protocol handler.
 * 
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class ICYSocketHandler extends IoHandlerAdapter {

	private static Logger log = LoggerFactory.getLogger(ICYSocketHandler.class);

	private static final byte[] OK_MESSAGE = "OK2\r\nicy-caps:11\r\n\r\n".getBytes();
	
	private static final byte[] BAD_PASSWD_MESSAGE = "invalid password\r\n".getBytes();
	
	private static final String userAgent = "Mozilla/4.0 (compatible; Red5 Server/NSV plugin)";
	
	private String host = "0.0.0.0";

	private int port = 8001;

	private int mode = 0;
	
	private SocketAcceptor acceptor;
	
	private IoBuffer outBuffer;
	
	private IICYHandler handler;
	
	public NSVStreamConfig config;
	
	private Map<String, Object> metaData = new HashMap<String, Object>();
	
	private boolean connected;

	private long lastDataTs;
	
	private String password;

	//thread sleep period
	private int waitTime = 50;
	
	//data timeout in milliseconds
	private long dataTimeout = 10000;

	//password has been accepted
	private boolean validated;
	
	//determines how to notify players that the video is upside down
	private boolean notifyFlipped;

	@SuppressWarnings("unused")
	private String audioType;
	
	public void start() {
		log.debug("Starting icy socket handler");
        switch (mode) {
			case 1: // client mode
				// create a singular HttpClient object
				HttpClient client = new HttpClient();

				// use proxy if specified
				if (System.getProperty("http.proxyHost") != null && System.getProperty("http.proxyPort") != null) {
					HostConfiguration config = client.getHostConfiguration();
					config.setProxy(System.getProperty("http.proxyHost").toString(), Integer.parseInt( System.getProperty("http.proxyPort")));
				}

				// establish a connection within 5 seconds
				client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
				//get the params for the client
				HttpClientParams params = client.getParams();
				params.setParameter(HttpMethodParams.USER_AGENT, userAgent);
				//get registry file
				HttpMethod method = new GetMethod(host);
				//follow any 302's although there should not be any
				method.setFollowRedirects(true);
				// execute the method
				try {
					int code = client.executeMethod(method);
					log.debug("HTTP response code: {}", code);
					String resp = method.getResponseBodyAsString();
					log.trace("Response: {}", resp);
				
					//TODO pipe input stream into mina
					//input = method.getResponseBodyAsStream();
					
				} catch (HttpException he) {
					log.error("Http error connecting to {}", host, he);
				} catch (IOException ioe) {
					log.error("Unable to connect to {}", host, ioe);
				} finally {
					//client mode is automatically validated
					validated = true;
					
					if (method != null) {
						method.releaseConnection();
					}
				}
				break;
			
			case 0: // server mode
				try {
		        	acceptor = new NioSocketAcceptor();
		        	acceptor.setReuseAddress(true);
		        	acceptor.setHandler(this);
		        	
		        	if (log.isDebugEnabled()) {
		        		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		        	}
		            acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
		            acceptor.getSessionConfig().setReadBufferSize(1024);
		            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
					
		        	if ("".equals(host)) {
		        		acceptor.setDefaultLocalAddress(new InetSocketAddress(port));
		        		acceptor.bind();
		        	} else {
    					Set<SocketAddress> addresses = new HashSet<SocketAddress>();			
    					addresses.add(new InetSocketAddress(host, port));	
    					acceptor.bind(addresses);
		        	}
					
					log.info("icy listening on port {}", port);	    					
					
					connected = true;
				} catch (IOException ioe) {
					log.debug("Unable to setup connector on host: {} port: {}", host, port);
					log.error("Unable to setup connector", ioe);
				}
				break;
				
			default:
				log.debug("Unhandled mode: {}", mode);
		}		
        
        outBuffer = IoBuffer.allocate(1024);
        outBuffer.setAutoExpand(true);
        
	}

	public void reset() {
		log.debug("Resetting icy socket");
    	connected = false;
    	validated = false;
    	lastDataTs = 0L;
    	metaData.clear();
    }
	
	public void stop() {
		log.debug("Stopping icy socket");
		reset();
		acceptor.unbind();
	}
	
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		log.info("Incomming: {}", session.getRemoteAddress().toString());
		log.debug("Message: {}", message.getClass().getName());
		
		if (lastDataTs > 0) {
        	long delta = System.currentTimeMillis() - lastDataTs;
        	if (delta > dataTimeout) {
        		log.debug("Data too late exit time: {} > timeout: {}", delta, dataTimeout);
        		//disconnect if late?
        		stop();
        	}	
		}
		lastDataTs = System.currentTimeMillis();
		log.debug("Data ts: {}", lastDataTs);
		
		//convert to string for comparisons
		String msg = null;
		if (message instanceof String) {
			msg = message.toString();
		}

		// check password?
		if (validated) {
			//after we stop getting text, we need to switch to binary protocol
			if (msg == null) {
				
	            //config.writeFrame(frame);

			} else if (msg.startsWith("icy") || msg.startsWith("content")) {
				String key = msg.substring(msg.indexOf('-') + 1, msg.indexOf(':'));
				String value = msg.substring(msg.indexOf(':') + 1);
				log.debug("Meta: {}={}", key, value);
				metaData.put(key, value);
				
			} else if (msg.length() == 0) {
				log.debug("End of header detected");
				//
				handler.onMetaData(metaData);
				//reset mode based on type
				String[] type = ((String) metaData.get("type")).split("/");
				if (mode == 3 || mode == 2) {
					if (type[0].equals("video")) {
						mode = (mode == 3) ? 0 : 1;
					} else {
						audioType = type[1];
					}
				} else {
					if (type[0].equals("audio")) {
						if (mode == 0) {
							mode = 3;
						} else {
							mode = 2;
						}
						audioType = type[1];
					}
				}
				//notify handler of mode change
				handler.reset(type[0], type[1]);
				
				//audio only
				//if (mode == 2 || mode == 3) {
					//handler.onAudioData(bits);
				//}
				
				//remove text protocol filter
	            acceptor.getFilterChain().remove("codec");
	            
	            //TODO create icy data protocol filter
	            //http://mina.apache.org/tutorial-on-protocolcodecfilter-for-mina-2x.html
	            //acceptor.getFilterChain().addLast("codec", new OurCoolIcyProtocolFilter());			
			}
		} else {
			log.debug("Not validated, check password");
			if (password.equals(msg)) {
				log.debug("Passwords match!");
				validated = true;
				outBuffer.put(OK_MESSAGE);
			} else {
				log.info("Invalid password {}, reset and close", msg);
				outBuffer.put(BAD_PASSWD_MESSAGE);
			}
			//flip it!
			outBuffer.flip();	
			//respond to the client
			session.write(outBuffer);
		}
		
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable ex) throws Exception {
		log.debug("Exception occurred {}", session.getRemoteAddress().toString());
		if (log.isDebugEnabled()) {
			//we want the stacktrace only if debugging
			log.warn("Exception: {}", ex);
		}
		session.close(true);
		//if we "stop" here then the port will need to be re-established
		reset();
	}	

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public IICYHandler getHandler() {
		return handler;
	}

	public void setHandler(IICYHandler handler) {
		this.handler = handler;
	}	
	
	public void setPassword(String password) {
		this.password = password;
	}

	public int getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	public long getDataTimeout() {
		return dataTimeout;
	}

	public void setDataTimeout(long dataTimeout) {
		this.dataTimeout = dataTimeout;
	}
	
	public boolean isNotifyFlipped() {
		return notifyFlipped;
	}

	public void setNotifyFlipped(boolean notifyFlipped) {
		this.notifyFlipped = notifyFlipped;
	}

	public boolean isConnected() {
		return connected;
	}
	
}
