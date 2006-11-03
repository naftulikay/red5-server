package org.red5.demos.security;

import org.red5.server.api.IConnection;

public interface App {

	public boolean appConnect(IConnection conn, Object[] obj);
}
