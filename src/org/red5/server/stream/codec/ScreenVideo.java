package org.red5.server.stream.codec;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright (c) 2006 by respective authors (see below). All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ByteBuffer;
import org.red5.server.api.stream.IVideoStreamCodec;

/**
 * Red5 video codec for the screen capture format. 
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Joachim Bauch (jojo@struktur.de)
 */
public class ScreenVideo implements IVideoStreamCodec {

	private Log log = LogFactory.getLog(ScreenVideo.class.getName());
	
	static final String CODEC_NAME = "ScreenVideo";
	static final byte FLV_FRAME_KEY = 0x10;
	static final byte FLV_CODEC_SCREEN = 0x03;
	static final int COPY_BUFFER_SIZE = 8192;
	static final int INITIAL_BUFFER_SIZE = 1048576;
	
	private ByteBuffer blockData = null;
	private int[] blockSize;
	private int width;
	private int height;
	private int widthInfo;
	private int heightInfo;
	private int blockWidth;
	private int blockHeight;
	private int blockCount;
	private int blockDataSize;
	private int totalBlockDataSize;
	private byte[] tmpData = new byte[COPY_BUFFER_SIZE];
	
	public ScreenVideo() {
		blockData = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);
		blockData.setAutoExpand(true);
		reset();
	}
	
	protected void finalize() throws Throwable {
		if (blockData != null)
			blockData.release();
		super.finalize();
	}
	
	public String getName() {
		return CODEC_NAME;
	}
	
	public synchronized void reset() {
		blockData.clear();
		blockSize = null;
		width = 0;
		height = 0;
		widthInfo = 0;
		heightInfo = 0;
		blockWidth = 0;
		blockHeight = 0;
		blockCount = 0;
		blockDataSize = 0;
		totalBlockDataSize = 0;
	}

	public boolean canHandleData(ByteBuffer data) {
		byte first = data.get();
		boolean result = ((first & 0x0f) == FLV_CODEC_SCREEN);
		data.rewind();
		return result;
	}

	public boolean canDropFrames() {
		return false;
	}
	
	private void copyStream(InputStream in, OutputStream out, int size) {
		int read = 0;
		while (size > 0) {
			try {
				read = in.read(tmpData, 0, size > COPY_BUFFER_SIZE ? COPY_BUFFER_SIZE : size);
				out.write(tmpData, 0, read);
			} catch (IOException err) {
				log.error("Could not copy data.", err);
				break;
			}
			size -= read;
		}
	}
	
	/*
	 * This uses the same algorithm as "compressBound" from zlib
	 */
	private int maxCompressedSize(int size) {
		return size + (size >> 12) + (size >> 14) + 11;
	}
	
	private void updateSize(ByteBuffer data) {
		widthInfo = data.getShort();
		heightInfo = data.getShort();
		// extract width and height of the frame
		width = widthInfo & 0xfff;
		height = heightInfo & 0xfff;
		// calculate size of blocks
		blockWidth = ((widthInfo >> 12) + 1) << 4;
		blockHeight = ((heightInfo >> 12) + 1) << 4;
		
		int xblocks = width / blockWidth;
		if ((width % blockWidth) != 0)
			// partial block
			xblocks += 1;

		int yblocks = height / blockHeight;
		if ((height % blockHeight) != 0)
			// partial block
			yblocks += 1;

		blockCount = xblocks * yblocks;
		
		int maxBlockSize = this.maxCompressedSize(blockWidth * blockHeight * 3);
		int totalBlockSize = maxBlockSize * blockCount;
		if (totalBlockDataSize != totalBlockSize) {
			log.info("Allocating memory for " + blockCount + " compressed blocks.");
			blockDataSize = maxBlockSize;
			totalBlockDataSize = totalBlockSize;
			blockSize = new int[blockCount];
			// Reset the sizes to zero
			for (int idx=0; idx<blockCount; idx++)
				blockSize[idx] = 0;
		}
	}
	
	public synchronized boolean addData(ByteBuffer data) {
		if (!canHandleData(data))
			return false;
		
		data.get();
		updateSize(data);
		int idx = 0;
		int pos = 0;;
		
		blockData.clear();
		while (data.remaining() > 0) {
			short size = data.getShort();
			if (size == 0) {
				// Block has not been modified
				idx += 1;
				pos += blockDataSize;
				blockData.position(pos);
				continue;
			}
			
			// Store new block data
			copyStream(data.asInputStream(), blockData.asOutputStream(), size);
			blockSize[idx] = size;
			idx += 1;
			pos += blockDataSize;
			blockData.position(pos);
		}
		
		data.rewind();
		return true;
	}

	public synchronized ByteBuffer getKeyframe() {
		if (blockSize == null)
			return null;
		
		ByteBuffer result = ByteBuffer.allocate(1024);
		result.setAutoExpand(true);

		// Header
		result.put((byte) (FLV_FRAME_KEY | FLV_CODEC_SCREEN));
		
		// Frame size
		result.putShort((short) widthInfo);
		result.putShort((short) heightInfo);
		
		// Get compressed blocks
		int pos=0;
		for (int idx=0; idx<blockCount; idx++) {
			int size = blockSize[idx];
			if (size == 0)
				// this should not happen: no data for this block
				return null;
			
			result.putShort((short) size);
			blockData.position(pos);
			copyStream(blockData.asInputStream(), result.asOutputStream(), size);
			pos += blockDataSize;
		}
		
		result.flip();
		return result;
	}

}
