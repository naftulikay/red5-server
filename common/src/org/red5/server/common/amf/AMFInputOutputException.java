package org.red5.server.common.amf;

@SuppressWarnings("serial")
public class AMFInputOutputException extends RuntimeException {

	public AMFInputOutputException() {
		super();
	}

	public AMFInputOutputException(String message, Throwable cause) {
		super(message, cause);
	}

	public AMFInputOutputException(String message) {
		super(message);
	}

	public AMFInputOutputException(Throwable cause) {
		super(cause);
	}

}
