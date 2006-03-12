package org.red5.server.net.rtmp;

import org.red5.server.api.IConnection;
import org.red5.server.net.AccessDeniedException;
import org.red5.server.net.IServiceHandler;
import org.red5.server.net.ISharedObjectHandler;
import org.red5.server.net.IStreamHandler;
import org.red5.server.net.ServiceNotFoundException;
import org.red5.server.net.SharedObjectException;
import org.red5.server.net.StreamControlException;
import org.red5.server.net.StreamDataException;
import org.red5.server.net.impl.BaseHandler;
import org.red5.server.net.message.IServiceCall;
import org.red5.server.net.message.ISharedObject;
import org.red5.server.net.message.IStreamControl;
import org.red5.server.net.message.IStreamData;

public class RTMPHandler extends BaseHandler implements 
	IServiceHandler, ISharedObjectHandler, IStreamHandler {
	
	protected IStreamHandler streamHandler;
	protected ISharedObjectHandler sharedObjectHandler;
	protected IServiceHandler serviceHandler;
	
	public void setServiceHandler(IServiceHandler serviceHandler) {
		this.serviceHandler = serviceHandler;
	}

	public void setSharedObjectHandler(ISharedObjectHandler sharedObjectHandler) {
		this.sharedObjectHandler = sharedObjectHandler;
	}

	public void setStreamHandler(IStreamHandler streamHandler) {
		this.streamHandler = streamHandler;
	}

	public void handleServiceCall(IConnection conn, IServiceCall serviceCall) throws ServiceNotFoundException, AccessDeniedException {
		serviceHandler.handleServiceCall(conn, serviceCall);
	}
	
	public void handleSharedObject(IConnection conn, ISharedObject so) throws AccessDeniedException, SharedObjectException {
		sharedObjectHandler.handleSharedObject(conn,so);
	}
	
	public void handleControl(IStreamControl control) throws StreamControlException {
		streamHandler.handleControl(control);
	}
	
	public void handleData(IStreamData data) throws StreamDataException {
		streamHandler.handleData(data);
	}

}