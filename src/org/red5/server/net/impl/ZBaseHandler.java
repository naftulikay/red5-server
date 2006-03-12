package org.red5.server.net.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.context.AppContext;
import org.red5.server.context.GlobalContext;
import org.red5.server.context.HostContext;
import org.red5.server.context.IClientRegistry;
import org.red5.server.net.AccessDeniedException;
import org.red5.server.net.ClientNotFoundException;
import org.red5.server.net.HostNotFoundException;
import org.red5.server.net.IConnectionHandler;
import org.red5.server.net.ScopeNotFoundException;
import org.red5.server.service.ServiceInvoker;

public class ZBaseHandler implements IConnectionHandler {

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

	protected String getHostname(String url) {
		return url.split("/")[2];
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
		final String hostname = getHostname((String) params.get("tcUrl"));
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
	
	public IClient newClient(String host) throws HostNotFoundException {
		// TODO Auto-generated method stub
		HostContext hostCtx = lookupHostContext(host);
		if (hostCtx == null)
			throw new HostNotFoundException();

		IClientRegistry registry;
		if (hostCtx.hasClientRegistry())
			registry = hostCtx.getClientRegistry();
		else
			registry = globalContext.getClientRegistry();
		
		return registry.newClient(host);
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
