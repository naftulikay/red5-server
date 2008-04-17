package org.red5.server.common.rtmp;

@SuppressWarnings("serial")
public class RTMPInvalidCodecStateException extends RTMPCodecException {

	public RTMPInvalidCodecStateException() {
		super();
	}

	public RTMPInvalidCodecStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public RTMPInvalidCodecStateException(String message) {
		super(message);
	}

	public RTMPInvalidCodecStateException(Throwable cause) {
		super(cause);
	}

}
