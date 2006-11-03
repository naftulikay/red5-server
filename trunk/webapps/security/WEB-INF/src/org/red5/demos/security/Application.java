package org.red5.demos.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.service.IServiceCapableConnection;

public class Application extends ApplicationAdapter implements App {

	private static final Log log = LogFactory.getLog(Application.class);

	@Override
	public boolean appStart(IScope scope) {
		// init your handler here
		return true;
	}

	@Override
	public boolean appConnect(IConnection conn, Object[] params) {
		IServiceCapableConnection service = (IServiceCapableConnection) conn;
		log.info("Client connected " + conn.getClient().getId() + " conn "
				+ conn);
		log.info("Setting stream id: " + getClients().size()); // just a unique number
		return true;
	}

}