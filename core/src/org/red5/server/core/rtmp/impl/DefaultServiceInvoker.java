package org.red5.server.core.rtmp.impl;

import org.red5.server.common.service.ServiceCall;
import org.red5.server.common.service.ServiceCallback;
import org.red5.server.common.service.ServiceInvocationException;
import org.red5.server.common.service.ServiceInvoker;
import org.red5.server.common.service.ServiceNotFoundException;
import org.red5.server.common.service.ServiceRegistry;
import org.red5.server.core.rtmp.RTMPConnection;

public class DefaultServiceInvoker implements ServiceInvoker<RTMPConnection> {

	@Override
	public Object asyncInvoke(Object service, ServiceCall<RTMPConnection> call,
			ServiceCallback callback) throws ServiceNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object asyncInvoke(ServiceRegistry registry,
			ServiceCall<RTMPConnection> call, ServiceCallback callback)
			throws ServiceNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object syncInvoke(Object service, ServiceCall<RTMPConnection> call)
			throws ServiceNotFoundException, ServiceInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object syncInvoke(ServiceRegistry registry,
			ServiceCall<RTMPConnection> call) throws ServiceNotFoundException,
			ServiceInvocationException {
		// TODO Auto-generated method stub
		return null;
	}

}
