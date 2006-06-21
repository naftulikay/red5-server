package org.red5.server.net.rtmp.event;

import org.red5.server.net.rtmp.message.Header;

public interface IHeaderAware {

	public void setHeader(Header header);
	
}
