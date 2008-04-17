package org.red5.server.common.rtmp;

@SuppressWarnings("serial")
public class RTMPCodecException extends RuntimeException {

	public RTMPCodecException() {
		super();
	}

	public RTMPCodecException(String message, Throwable cause) {
		super(message, cause);
	}

	public RTMPCodecException(String message) {
		super(message);
	}

	public RTMPCodecException(Throwable cause) {
		super(cause);
	}
}
