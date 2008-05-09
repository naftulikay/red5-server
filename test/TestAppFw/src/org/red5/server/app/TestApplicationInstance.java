package org.red5.server.app;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.red5.server.common.service.ServiceRegistry;
import org.red5.server.core.rtmp.RTMPApplication;
import org.red5.server.core.rtmp.RTMPApplicationInstance;
import org.red5.server.core.rtmp.RTMPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestApplicationInstance implements RTMPApplicationInstance {
	private static final Logger log = LoggerFactory.getLogger(TestApplicationInstance.class);
	
	private RTMPApplication application;
	private String instanceName;
	private int refCount;

	public TestApplicationInstance(RTMPApplication application,
			String instanceName) {
		this.application = application;
		this.instanceName = instanceName;
		this.refCount = 0;
	}

	@Override
	public void acquire() {
		this.application.acquireInstance(instanceName);
	}
	
	@Override
	public void release() {
		this.application.releaseInstance(instanceName);
	}

	@Override
	public Object connect(RTMPConnection connection,
			Map<String, Object> connectionParams, List<Object> args) {
		log.info("connect to instance {}", instanceName);
		return null;
	}

	@Override
	public RTMPApplication getApplication() {
		return application;
	}

	@Override
	public Collection<RTMPConnection> getConnections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInstanceName() {
		return instanceName;
	}

	@Override
	public ServiceRegistry getInstanceRegistry() {
		return null;
	}

	public int addRef() {
		refCount++;
		return refCount;
	}

	public int removeRef() {
		refCount--;
		return refCount;
	}
}
