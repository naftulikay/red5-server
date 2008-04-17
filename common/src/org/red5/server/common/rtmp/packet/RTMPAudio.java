package org.red5.server.common.rtmp.packet;

import org.red5.server.common.ExByteBuffer;

public class RTMPAudio extends RTMPPacket {
	private ExByteBuffer audioData;
	
	public RTMPAudio() {
		super(TYPE_RTMP_AUDIO);
	}

	public ExByteBuffer getAudioData() {
		return audioData;
	}

	public void setAudioData(ExByteBuffer audioData) {
		this.audioData = audioData;
	}
}
