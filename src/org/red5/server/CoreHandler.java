package org.red5.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.api.IClient;
import org.red5.server.api.IClientRegistry;
import org.red5.server.api.IConnection;
import org.red5.server.api.IContext;
import org.red5.server.api.IScope;
import org.red5.server.api.IScopeHandler;
import org.red5.server.api.service.IServiceCall;

public class CoreHandler implements IScopeHandler {

	protected static Log log =
        LogFactory.getLog(CoreHandler.class.getName());
	
	protected IClientRegistry clientRegistry;
	
	public boolean addChildScope(IScope scope) {
		return true;
	}

	public boolean connect(IConnection conn) {
		
		String id = conn.getSessionId();
		
		IClient client = clientRegistry.hasClient(id) ? 
				clientRegistry.lookupClient(id) : clientRegistry.newClient();
		
		// We have a context, and a client object.. time to init the conneciton.
		conn.initialize(client);
		
		// we could checked for banned clients here 
		return true;
	}

	public void disconnect(IConnection conn) {
		
	}

	public boolean handleEvent(Object event) {
		
		return false;
	}

	public boolean join(IClient client, IScope scope) {
		// TODO Auto-generated method stub
		return false;
	}

	public void leave(IClient client, IScope scope) {
		// TODO Auto-generated method stub
		
	}

	public IServiceCall postProcessServiceCall(IConnection conn, IServiceCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	public IServiceCall preProcessServiceCall(IConnection conn, IServiceCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeChildScope(IScope scope) {
		// TODO Auto-generated method stub
		
	}

	public boolean serviceCall(IConnection conn, IServiceCall call) {
		final IContext context = conn.getScope().getContext();
		if(call.getServiceName() != null){
			context.getServiceInvoker().invoke(call, context);
		} else {
			context.getServiceInvoker().invoke(call, conn.getScope().getHandler());
		}
		return true;
	}

	public boolean start(IScope scope) {
		return true;
	}

	public void stop(IScope scope) {
		// TODO Auto-generated method stub
	}

	public boolean handleEvent(IConnection conn, Object event) {
		// TODO Auto-generated method stub
		return true;
	}

	
}
