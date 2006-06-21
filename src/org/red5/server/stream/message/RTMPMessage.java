package org.red5.server.stream.message;

import org.red5.server.messaging.AbstractMessage;
import org.red5.server.net.rtmp.event.BaseEvent;

public class RTMPMessage extends AbstractMessage {
	private BaseEvent body;

	public BaseEvent getBody() {
		return body;
	}

	public void setBody(BaseEvent body) {
		this.body = body;
	}
	
	public void acquire() {
		if (body != null) body.acquire();
	}
	
	public void release() {
		if (body != null) body.release();
	}
	
}
