package org.red5.server.common.rtmp.packet;

import org.red5.server.common.ExByteBuffer;

public class RTMPVideo extends RTMPPacket {
	private ExByteBuffer videoData;
	
	public RTMPVideo() {
		super(TYPE_RTMP_VIDEO);
	}

	public ExByteBuffer getVideoData() {
		return videoData;
	}

	public void setVideoData(ExByteBuffer videoData) {
		this.videoData = videoData;
	}

}
