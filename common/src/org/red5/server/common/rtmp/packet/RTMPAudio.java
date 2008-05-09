package org.red5.server.common.rtmp.packet;

import org.red5.server.common.BufferEx;

public class RTMPAudio extends RTMPPacket {
	private BufferEx audioData;
	
	public RTMPAudio() {
		super(TYPE_RTMP_AUDIO);
	}

	public BufferEx getAudioData() {
		return audioData;
	}

	public void setAudioData(BufferEx audioData) {
		this.audioData = audioData;
	}
}
