package org.red5.server.common.service;

import java.util.Map;

/**
 * The call object for a service invocation.
 * A service call is composed of
 * (1) service name
 * Each service object is registered in the service registry by a name.
 * (2) service method
 * A service has a number of methods.
 * (3) arguments
 * Arguments for the method.
 * (4) context map
 * Extra parameters that facilitate the matching and invocation.
 * An example is the connection object that might be used by a service
 * method.
 * 
 * @author Steven Gong (steven.gong@gmail.com)
 */
public class ServiceCall {
	private String serviceName;
	private String serviceMethod;
	private Object[] arguments;
	private Map<String,Object> callContext;
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServiceMethod() {
		return serviceMethod;
	}
	public void setServiceMethod(String serviceMethod) {
		this.serviceMethod = serviceMethod;
	}
	public Object[] getArguments() {
		return arguments;
	}
	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}
	public Map<String, Object> getCallContext() {
		return callContext;
	}
	public void setCallContext(Map<String, Object> callContext) {
		this.callContext = callContext;
	}
	
}
