package org.red5.server.net.rtmp.event;

public class StreamBytesRead extends BaseEvent {
	
	private static final int INITIAL_CAPACITY = 4;
	
	private int bytesRead = 0;

	public StreamBytesRead(int bytesRead){
		super();
		this.bytesRead = bytesRead;
	}
	
	public int getBytesRead(){
		return bytesRead;
	}

	public void setBytesRead(int bytesRead) {
		this.bytesRead = bytesRead;
	}

	protected void doRelease() {
		bytesRead = 0;
	}
	
	public String toString(){
		return "StreamBytesRead: "+bytesRead;
	}
	
}