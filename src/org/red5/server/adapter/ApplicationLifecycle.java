/**
 * 
 */
package org.red5.server.adapter;

import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;

/**
 * @author dominickaccattato
 *
 */
public class ApplicationLifecycle implements IApplication {
	
	/* (non-Javadoc)
	 * @see org.red5.server.adapter.IApplication#appConnect(org.red5.server.api.IConnection, java.lang.Object[])
	 */
	public boolean appConnect(IConnection conn, Object[] params) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.red5.server.adapter.IApplication#appDisconnect(org.red5.server.api.IConnection)
	 */
	public void appDisconnect(IConnection conn) {
	}

	/* (non-Javadoc)
	 * @see org.red5.server.adapter.IApplication#appJoin(org.red5.server.api.IClient, org.red5.server.api.IScope)
	 */
	public boolean appJoin(IClient client, IScope app) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.red5.server.adapter.IApplication#appLeave(org.red5.server.api.IClient, org.red5.server.api.IScope)
	 */
	public void appLeave(IClient client, IScope app) {
	}

	/* (non-Javadoc)
	 * @see org.red5.server.adapter.IApplication#appStart(org.red5.server.api.IScope)
	 */
	public boolean appStart(IScope app) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.red5.server.adapter.IApplication#appStop(org.red5.server.api.IScope)
	 */
	public void appStop(IScope app) {
	}

	/* (non-Javadoc)
	 * @see org.red5.server.adapter.IApplication#roomConnect(org.red5.server.api.IConnection, java.lang.Object[])
	 */
	public boolean roomConnect(IConnection conn, Object[] params) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.red5.server.adapter.IApplication#roomDisconnect(org.red5.server.api.IConnection)
	 */
	public void roomDisconnect(IConnection conn) {

	}

	/* (non-Javadoc)
	 * @see org.red5.server.adapter.IApplication#roomJoin(org.red5.server.api.IClient, org.red5.server.api.IScope)
	 */
	public boolean roomJoin(IClient client, IScope room) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.red5.server.adapter.IApplication#roomLeave(org.red5.server.api.IClient, org.red5.server.api.IScope)
	 */
	public void roomLeave(IClient client, IScope room) {
	}

	/* (non-Javadoc)
	 * @see org.red5.server.adapter.IApplication#roomStart(org.red5.server.api.IScope)
	 */
	public boolean roomStart(IScope room) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.red5.server.adapter.IApplication#roomStop(org.red5.server.api.IScope)
	 */
	public void roomStop(IScope room) {
	}

}
