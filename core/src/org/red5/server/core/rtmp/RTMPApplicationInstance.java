package org.red5.server.core.rtmp;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.red5.server.common.service.ServiceRegistry;

public interface RTMPApplicationInstance {
	public static final String DEFAULT_INSTANCE_NAME = "_definst_";
	
	String getInstanceName();
	RTMPApplication getApplication();
	ServiceRegistry getInstanceRegistry();
	Collection<RTMPConnection> getConnections();
	/**
	 * Handles a connect from RTMPConnection to this instance.
	 * @param connection
	 * @param connectionParams
	 * @param args
	 * @return Reject object. <tt>null</tt> if the connection is accepted.
	 */
	Object connect(RTMPConnection connection, Map<String,Object> connectionParams, List<Object> args);
}
