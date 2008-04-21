package org.red5.server.common.service;

@SuppressWarnings("serial")
public class ServiceInvocationException extends Exception {

	public ServiceInvocationException() {
		super();
	}

	public ServiceInvocationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceInvocationException(String message) {
		super(message);
	}

	public ServiceInvocationException(Throwable cause) {
		super(cause);
	}
	
}
