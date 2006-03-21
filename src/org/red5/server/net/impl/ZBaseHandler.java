package org.red5.server.net.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.api.IClient;
import org.red5.server.api.IClientRegistry;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.ex.AccessDeniedException;
import org.red5.server.ex.ClientNotFoundException;
import org.red5.server.ex.ScopeNotFoundException;
import org.red5.server.service.ServiceInvoker;
import org.red5.server.zcontext.AppContext;
import org.red5.server.zcontext.GlobalContext;
import org.red5.server.zcontext.HostContext;

public class ZBaseHandler {

	protected static Log log =
        LogFactory.getLog(ZBaseHandler.class.getName());
	
	public GlobalContext globalContext = null;
	public ServiceInvoker serviceInvoker = null;
	
	public void setGlobalContext(GlobalContext globalContext) {
		this.globalContext = globalContext;
	}
	
	public void setServiceInvoker(ServiceInvoker serviceInvoker) {
		this.serviceInvoker = serviceInvoker;
	}



	protected HostContext lookupHostContext(String hostname) {
		log.debug("Hostname: "+hostname);

		if (globalContext.hasHostContext(hostname))
			return globalContext.getHostContext(hostname);
		else
			return globalContext.getDefaultHost();
	}
	
	protected HostContext lookupHostContext(IConnection conn) {
		final Map params = conn.getParams();
		final String hostname = "" ; //getHostname((String) params.get("tcUrl"));
		return lookupHostContext(hostname);
	}
	
	protected AppContext lookupAppContext(IConnection conn){
		final HostContext host = lookupHostContext(conn);
		final Map params = conn.getParams();
		final String app = (String) params.get("app");
		log.debug("App: "+app);
		
		if(!host.hasAppContext(app)){
			log.warn("Application \"" + app + "\" not found");
			return null; // todo close connection etc, send status etc
		}
		
		return host.getAppContext(app);
	}
	
	public IClient newClient(String host)  {
		// TODO Auto-generated method stub
		HostContext hostCtx = lookupHostContext(host);
		
		IClientRegistry registry;
		if (hostCtx.hasClientRegistry())
			registry = hostCtx.getClientRegistry();
		else
			registry = globalContext.getClientRegistry();
		
		return registry.newClient();
	}

	public IClient lookupClient(String sessionId)
			throws ClientNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public IScope lookupScope(IClient client, String contextPath)
			throws ScopeNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean connect(IConnection conn) throws AccessDeniedException {
		// TODO Auto-generated method stub
		return false;
	}

	public void disconnect(IConnection conn) {
		// TODO Auto-generated method stub

	}

}
