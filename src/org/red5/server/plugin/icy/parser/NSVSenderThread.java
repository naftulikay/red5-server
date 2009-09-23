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

import org.red5.server.plugin.icy.IICYMarshal;

/**
 * Pushes data out at specified intervals.
 * 
 * @author Paul Gregoire (mondain@gmail.com)
 * @author Andy Shaules (bowljoman@hotmail.com)
 */
public class NSVSenderThread implements Runnable {

	public IICYMarshal reader;

	public NSVStreamConfig config = new NSVStreamConfig();

	public int time = 0;

	public NSVSenderThread(IICYMarshal reader) {
		this.reader = reader;
	}

	@Override
	public void run() {
		while (config.hasFrames()) {
			NSVFrame frame = config.readFrame();
			if (reader.equals(null)) {
				continue;
			}
			reader.onAudioData(frame.aud_data);
			if (config.videoFormat == null) {
				continue;
			}
			reader.onVideoData(frame.vid_data);
		}

	}

}
