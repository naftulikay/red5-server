package org.red5.server.plugin.icy.parser;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright (c) 2006-2009 by respective authors (see below). All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 2.1 of the License, or (at your option) any later 
 * version. 
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this library; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

/** 
 * Represents a single frame of NSV data.
 *  
 * @author Andy Shaules (bowljoman@hotmail.com)
 */
public class NSVFrame {

	public long frameType = 0x8080;//empty	

	public long frameNumber = 0;

	public int parser_info = 0;

	public String vid_type;

	public String aud_type;

	public int width = 0;

	public int height = 0;

	public double frameRate = 0;

	public int frameRateEncoded = 0x0;

	public int offsetCurrent = 0;

	public int[] vid_data;

	public int[] aud_data;

	public int vid_len = 0;

	public int aud_len = 0;

	public int num_aux = 0;

	public long timeStamp = 0;

	public NSVFrame(NSVStreamConfig id, long type) {
		frameType = type;
		aud_type = id.audioFormat;
		vid_type = id.videoFormat;
		width = id.videoWidth;
		height = id.videoHeight;
		frameRate = id.frameRate;
		frameRateEncoded = id.frameRateEncoded;
	}

	/**
	 * For output back to shoutcast server. 
	 */
	public int[] toBitStream() {
		int[] ret = new int[1];
		ret[0] = 0;
		int length = 0;
		NSVBitStream bs = new NSVBitStream();
		switch ((frameType == NSVStream.NSV_SYNC_DWORD) ? 1 : 2) {
			case 1:
				length = (24) + (vid_len) + (aud_len);
				ret = new int[length];
				ret[0] = 'N';
				ret[1] = 'S';
				ret[2] = 'V';
				ret[3] = 's';
				ret[4] = (byte) vid_type.charAt(0);
				ret[5] = (byte) vid_type.charAt(1);
				ret[6] = (byte) vid_type.charAt(2);
				ret[7] = (byte) vid_type.charAt(3);
				ret[8] = (byte) aud_type.charAt(0);
				ret[9] = (byte) aud_type.charAt(1);
				ret[10] = (byte) aud_type.charAt(2);
				ret[11] = (byte) aud_type.charAt(3);
				ret[12] = ((width << 8) >> 8);
				ret[13] = ((width) >> 8);
				ret[14] = ((height << 8) >> 8);
				ret[15] = ((height) >> 8);
				ret[16] = frameRateEncoded;//frame rate
				ret[17] = ((offsetCurrent << 8) >> 8);
				ret[18] = ((offsetCurrent) >> 8);
				bs = new NSVBitStream();
				bs.putBits(4, num_aux);
				bs.putBits(20, vid_len);
				bs.putBits(16, aud_len);

				for (int i = 0; i < 5; i++) {
					ret[19 + i] = bs.getbits(8);
				}
				for (int i = 0; i < vid_len; i++) {
					ret[24 + i] = vid_data[i];
				}
				for (int i = 0; i < aud_len; i++) {
					ret[(24 + vid_len + i)] = aud_data[i];
				}

				break;
			case 2:
				length = (7) + (vid_len) + (aud_len);
				ret = new int[length];

				ret[0] = 0xef;
				ret[1] = 0xbe;
				bs = new NSVBitStream();

				bs.putBits(4, num_aux);
				bs.putBits(20, vid_len);
				bs.putBits(16, aud_len);

				for (int i = 0; i < 5; i++) {
					ret[2 + i] = bs.getbits(8);
				}
				for (int i = 0; i < vid_len; i++) {
					ret[7 + i] = vid_data[i];
				}
				for (int i = 0; i < aud_len; i++) {
					ret[(7 + vid_len + i)] = aud_data[i];
				}
				break;
		}
		return ret;
	}

	public long getFrameNumber() {
		return frameNumber;
	}

	public void setFrameNumber(long frameNumber) {
		this.frameNumber = frameNumber;
	}

}
