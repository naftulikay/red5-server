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

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * Individual stream configuration generated from the parser when the shoutcast header is received.
 * 
 * @author Paul Gregoire (mondain@gmail.com)
 * @author Andy Shaules (bowljoman@hotmail.com)
 */
public class NSVStreamConfig {

	public int streamId = -1;

	public String videoFormat = null;

	public String audioFormat = null;

	public int videoWidth = 0;

	public int videoHeight = 0;

	public double frameRate = 0;

	public int frameRateEncoded = 0x0;

	public AtomicLong totalFrames = new AtomicLong(0);

	public long startTime = 0;

	public volatile ArrayList<NSVFrame> frames = new ArrayList<NSVFrame>();

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private ReadLock readLock = lock.readLock();

	private WriteLock writeLock = lock.writeLock();

	public void writeFrame(NSVFrame frame) {
		totalFrames.incrementAndGet();
		try {
			writeLock.lock();
			frames.add(frame);
		} finally {
			writeLock.unlock();
		}
	}

	public NSVFrame readFrame() {
		try {
			writeLock.lock();
			return frames.remove(0);
		} finally {
			writeLock.unlock();
		}
	}

	public boolean hasFrames() {
		try {
			readLock.lock();
			return (frames.isEmpty()) ? false : true;
		} finally {
			readLock.unlock();
		}
	}

	public int count() {
		try {
			readLock.lock();
			return frames.size();
		} finally {
			readLock.unlock();
		}
	}

	public void flush() {
		try {
			writeLock.lock();
			frames.clear();
		} finally {
			writeLock.unlock();
		}
	}

}
