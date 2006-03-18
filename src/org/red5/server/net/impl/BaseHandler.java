package org.red5.server.net.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.api.IClient;
import org.red5.server.api.IClientRegistry;
import org.red5.server.api.IConnection;
import org.red5.server.api.IContext;
import org.red5.server.api.IScope;
import org.red5.server.api.IScopeResolver;
import org.red5.server.api.impl.BaseConnection;
import org.red5.server.net.AccessDeniedException;
import org.red5.server.net.IConnectionHandler;

public class BaseHandler implements IConnectionHandler {

	protected static Log log =
        LogFactory.getLog(BaseHandler.class.getName());
	
	public IContext globalContext = null;
	
	public void setGlobalContext(IContext context) {
		this.globalContext = context;
	}

	public boolean connect(IConnection conn) throws AccessDeniedException {
		
		// We start with the global context
		IContext context = globalContext;
		
		// First thing to do is lookup the scope.. 
		IScope scope = context.resolveScope(conn.getHost(), conn.getPath());
		
		// Right we can switch context now, to the context associated with our new scope
		context = scope.getContext();
		
		// Now we need a client object, lets get the registry 
		String id = conn.getSessionId();
		IClientRegistry reg = context.getClientRegistry();
		IClient client = reg.hasClient(id) ? reg.lookupClient(id) : reg.newClient();
		
		// We have a context, and a client object.. time to init the conneciton.
		conn.initialize(client, context);
		
		// Ok, now we can try to connect, connecting means joining the scope.
		return conn.connect(scope);
	}

	public void disconnect(IConnection conn) {
		conn.close();
	}

}