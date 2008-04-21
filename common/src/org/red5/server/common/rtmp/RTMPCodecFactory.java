package org.red5.server.common.rtmp;


public interface RTMPCodecFactory {	
	public abstract RTMPInput newRTMPInput();
	public abstract RTMPOutput newRTMPOutput();
}
