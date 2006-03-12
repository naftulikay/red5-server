package org.red5.server.net.rtmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.IoSession;

public class RTMPMinaConnection extends RTMPConnection {

	protected static Log log =
        LogFactory.getLog(RTMPMinaConnection.class.getName());

	private IoSession ioSession;
	
	public RTMPMinaConnection(IoSession protocolSession) {
		super();
		this.ioSession = protocolSession;
	}
	
	public IoSession getIoSession() {
		return ioSession;
	}

	public void dispatchEvent(Object packet){
		ioSession.write(packet);
	}
	
	public boolean isConnected() {
		return this.ioSession.isConnected();
	}
}
