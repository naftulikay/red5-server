package org.red5.server.common.service;

/**
 * The call object for a service invocation.
 * A service call is composed of
 * (1) service name
 * Each service object is registered in the service registry by a name.
 * (2) service method
 * A service has a number of methods.
 * (3) arguments
 * Arguments for the method.
 * (4) context object
 * Extra parameters that facilitate the matching and invocation.
 * An example is the connection object that might be used by a service
 * method.
 * 
 * @author Steven Gong (steven.gong@gmail.com)
 */
public class ServiceCall<T> {
	private String serviceName;
	private String methodName;
	private Object[] arguments;
	private T callContext;
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Object[] getArguments() {
		return arguments;
	}
	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}
	public T getCallContext() {
		return callContext;
	}
	public void setCallContext(T callContext) {
		this.callContext = callContext;
	}
	
}
