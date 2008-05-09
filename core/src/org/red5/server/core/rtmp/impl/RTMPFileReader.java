package org.red5.server.core.rtmp.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.red5.server.common.BufferEx;
import org.red5.server.common.rtmp.RTMPCodecFactory;
import org.red5.server.common.rtmp.RTMPHandler;
import org.red5.server.common.rtmp.RTMPInput;
import org.red5.server.common.rtmp.packet.RTMPPacket;

public class RTMPFileReader {
	private RTMPHandler<Object> rtmpHandler;
	private RTMPInput rtmpInput;
	
	public void setRtmpHandler(RTMPHandler<Object> rtmpHandler) {
		this.rtmpHandler = rtmpHandler;
	}
	
	public void setRtmpInput(RTMPInput rtmpInput) {
		this.rtmpInput = rtmpInput;
	}
	
	public void read(File file) throws IOException {
		byte[] buf = new byte[1024];
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(file);
			int bytesRead;
			while ((bytesRead = fin.read(buf)) >= 0) {
				if (bytesRead > 0) {
					BufferEx byteBuffer =
						BufferEx.wrap(buf, 0, bytesRead);
					RTMPPacket packet = rtmpInput.read(byteBuffer);
					if (packet != null) {
						rtmpHandler.onPacket(this, packet);
					}
				}
			}
		} finally {
			if (fin != null) {
				fin.close();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		RTMPFileReader reader = new RTMPFileReader();
		reader.setRtmpHandler(new RTMPDumper());
		// TODO get rtmp codec factory
		RTMPCodecFactory factory = null;
		reader.setRtmpInput(factory.newRTMPInput());
		reader.read(new File("e:\\tmp\\h264_test.raw"));
	}
}
