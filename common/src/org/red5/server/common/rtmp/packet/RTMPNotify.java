package org.red5.server.common.rtmp.packet;

import java.util.List;

public class RTMPNotify extends RTMPPacket {
	private String action;
	private List<Object> arguments;

	public RTMPNotify() {
		super(TYPE_RTMP_NOTIFY);
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<Object> getArguments() {
		return arguments;
	}

	public void setArguments(List<Object> arguments) {
		this.arguments = arguments;
	}
}
