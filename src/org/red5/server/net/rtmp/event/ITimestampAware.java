package org.red5.server.net.rtmp.event;

public interface ITimestampAware {

	public int getTimestamp();
	
	public void setTimestamp(int timestamp);
}
