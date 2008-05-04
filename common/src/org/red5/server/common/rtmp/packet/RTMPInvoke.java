package org.red5.server.common.rtmp.packet;

import java.util.Map;

public class RTMPInvoke extends RTMPNotify {
	private long invokeId;
	private Map<String,Object> connectionParams;
	
	public RTMPInvoke() {
		super();
		setType(TYPE_RTMP_INVOKE);
	}

	public long getInvokeId() {
		return invokeId;
	}

	public void setInvokeId(long invokeId) {
		this.invokeId = invokeId;
	}

	public Map<String, Object> getConnectionParams() {
		return connectionParams;
	}

	public void setConnectionParams(Map<String, Object> connectionParams) {
		this.connectionParams = connectionParams;
	}

}
