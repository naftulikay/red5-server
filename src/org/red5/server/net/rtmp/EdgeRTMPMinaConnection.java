package org.red5.server.net.rtmp;

import org.red5.server.api.scheduling.ISchedulingService;

public class EdgeRTMPMinaConnection extends RTMPMinaConnection {

	@Override
	protected void startWaitForHandshake(ISchedulingService service) {
		// FIXME do nothing to avoid disconnect.
	}
}
