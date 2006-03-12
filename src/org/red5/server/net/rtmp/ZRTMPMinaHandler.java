package org.red5.server.net.rtmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.red5.server.context.GlobalContext;
import org.red5.server.net.protocol.ProtocolState;
import org.red5.server.net.rtmp.codec.RTMP;
import org.red5.server.net.rtmp.status.StatusObjectService;
import org.red5.server.service.ServiceInvoker;

/*
 * Mina implementation of the RTMP handler.
 * 
 */
public class ZRTMPMinaHandler extends IoHandlerAdapter {

	protected static Log log =
        LogFactory.getLog(ZRTMPMinaHandler.class.getName());
	
	protected ZRTMPHandler handler = new ZRTMPHandler();
	private ProtocolCodecFactory codecFactory = null;
	
	
	public void setGlobalContext(GlobalContext globalContext) {
		handler.setGlobalContext(globalContext);
	}
	
	public void setServiceInvoker(ServiceInvoker serviceInvoker) {
		handler.setServiceInvoker(serviceInvoker);
	}
	
	public void setCodecFactory(ProtocolCodecFactory codecFactory) {
		this.codecFactory = codecFactory;
	}
	
	public void setStatusObjectService(StatusObjectService statusObjectService) {
		handler.setStatusObjectService(statusObjectService);
	}
	
	//	 ------------------------------------------------------------------------------
	
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		log.debug("Exception caught", cause);
	}

	public void messageReceived(IoSession session, Object in) throws Exception {
		final RTMPMinaConnection conn = (RTMPMinaConnection) session.getAttachment();
		final ProtocolState state = (ProtocolState) session.getAttribute(RTMP.SESSION_KEY);
		
		handler.messageReceived(conn, state, in);
	}
	
	public void messageSent(IoSession session, Object message) throws Exception {
		final RTMPMinaConnection conn = (RTMPMinaConnection) session.getAttachment();

		handler.messageSent(conn, message);
	}

	public void sessionClosed(IoSession session) throws Exception {
		final RTMP rtmp = (RTMP) session.getAttribute(RTMP.SESSION_KEY);
		final RTMPMinaConnection conn = (RTMPMinaConnection) session.getAttachment();
		
		handler.connectionClosed(conn, rtmp);
	}

	public void sessionCreated(IoSession session) throws Exception {
		if(log.isDebugEnabled())
			log.debug("Session created");
		
		// moved protocol state from connection object to rtmp object
		session.setAttribute(RTMP.SESSION_KEY, new RTMP(RTMP.MODE_SERVER));
		
		session.getFilterChain().addFirst(
                "protocolFilter", new ProtocolCodecFilter(this.codecFactory) );
        session.getFilterChain().addLast(
                "logger", new LoggingFilter() );
        
		session.setAttachment(new RTMPMinaConnection(session));
		
	}
}
