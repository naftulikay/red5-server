package org.red5.server.common.rtmp;


public interface RTMPCodecFactory {	
	RTMPInput newRTMPInput();
	RTMPOutput newRTMPOutput();
}
