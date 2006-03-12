package org.red5.server.net.rtmp;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

public class RTMPMinaIoHandler extends IoHandlerAdapter {

	protected RTMPHandler handler;

	public void setHandler(RTMPHandler handler) {
		this.handler = handler;
	}

	public void exceptionCaught(IoSession arg0, Throwable arg1) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(arg0, arg1);
	}

	public void messageReceived(IoSession arg0, Object arg1) throws Exception {
		// TODO Auto-generated method stub
		super.messageReceived(arg0, arg1);
	}

	public void messageSent(IoSession arg0, Object arg1) throws Exception {
		// TODO Auto-generated method stub
		super.messageSent(arg0, arg1);
	}

	public void sessionClosed(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub
		super.sessionClosed(arg0);
	}

	public void sessionCreated(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub
		super.sessionCreated(arg0);
	}

	public void sessionIdle(IoSession arg0, IdleStatus arg1) throws Exception {
		// TODO Auto-generated method stub
		super.sessionIdle(arg0, arg1);
	}

	public void sessionOpened(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub
		super.sessionOpened(arg0);
	}
		
}