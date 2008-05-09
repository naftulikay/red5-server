package org.red5.server.common.rtmp.packet;

import org.red5.server.common.BufferEx;

public class RTMPVideo extends RTMPPacket {
	private BufferEx videoData;
	
	public RTMPVideo() {
		super(TYPE_RTMP_VIDEO);
	}

	public BufferEx getVideoData() {
		return videoData;
	}

	public void setVideoData(BufferEx videoData) {
		this.videoData = videoData;
	}

}
