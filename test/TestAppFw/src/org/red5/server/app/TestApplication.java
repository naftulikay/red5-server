package org.red5.server.app;

import java.util.HashMap;
import java.util.Map;

import org.red5.server.common.service.ServiceRegistry;
import org.red5.server.core.rtmp.RTMPApplication;
import org.red5.server.core.rtmp.RTMPApplicationInstance;

public class TestApplication implements RTMPApplication {
	private Map<String,TestApplicationInstance> instanceMap =
		new HashMap<String,TestApplicationInstance>();
	private String name;
	
	public TestApplication(String name) {
		this.name = name;
	}

	@Override
	synchronized public RTMPApplicationInstance acquireInstance(String instanceName) {
		TestApplicationInstance instance = instanceMap.get(instanceName);
		if (instance == null) {
			instance = new TestApplicationInstance(this, instanceName);
			instanceMap.put(instanceName, instance);
		}
		instance.addRef();
		return instance;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ServiceRegistry getRegistry() {
		return null;
	}

	@Override
	synchronized public void releaseInstance(String instanceName) {
		TestApplicationInstance instance = instanceMap.get(instanceName);
		if (instance != null) {
			int refCount = instance.removeRef();
			if (refCount == 0) {
				instanceMap.remove(instanceName);
			}
		}
	}

}
