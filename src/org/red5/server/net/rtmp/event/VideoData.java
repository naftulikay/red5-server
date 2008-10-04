package org.red5.server.net.rtmp.event;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright (c) 2006-2008 by respective authors (see below). All rights reserved.
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
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.mina.common.ByteBuffer;
import org.red5.io.IoConstants;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.stream.IStreamData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Video data event
 */
public class VideoData extends BaseEvent implements IoConstants, IStreamData, IStreamPacket {

	private static final long serialVersionUID = 5538859593815804830L;

    private static Logger log = LoggerFactory.getLogger(VideoData.class);
    
	/**
     * Videoframe type
     */
    public static enum FrameType {
		UNKNOWN, KEYFRAME, INTERFRAME, DISPOSABLE_INTERFRAME,
	}

    /**
     * Video data
     */
    protected ByteBuffer data;
    
    /**
     * Data type
     */
    private byte dataType = TYPE_VIDEO_DATA;

    /**
     * Frame type, unknown by default
     */
    protected FrameType frameType = FrameType.UNKNOWN;

	/** Constructs a new VideoData. */
    public VideoData() {
		this(ByteBuffer.allocate(0).flip());
	}

    /**
     * Create video data event with given data buffer
     * @param data            Video data
     */
    public VideoData(ByteBuffer data) {
		super(Type.STREAM_DATA);
		this.data = data;
		if (data != null && data.limit() > 0) {
			int oldPos = data.position();
			int firstByte = (data.get()) & 0xff;
			log.debug("old pos: {} first byte: {}", oldPos, firstByte);
			data.position(oldPos);
			int frameType = (firstByte & MASK_VIDEO_FRAMETYPE) >> 4;
			if (frameType == FLAG_FRAMETYPE_KEYFRAME) {
				log.debug("Frame type = Keyframe");
				this.frameType = FrameType.KEYFRAME;
			} else if (frameType == FLAG_FRAMETYPE_INTERFRAME) {
				log.debug("Frame type = Interframe");
				this.frameType = FrameType.INTERFRAME;
			} else if (frameType == FLAG_FRAMETYPE_DISPOSABLE) {
				log.debug("Frame type = Disposable interframe");
				this.frameType = FrameType.DISPOSABLE_INTERFRAME;
			} else {
				log.debug("Frame type = Unknown");
				this.frameType = FrameType.UNKNOWN;
			}
		}
	}

	/** {@inheritDoc} */
    @Override
	public byte getDataType() {
		return dataType;
	}

	public void setDataType(byte dataType) {
		this.dataType = dataType;
	}

	/** {@inheritDoc} */
    public ByteBuffer getData() {
		return data;
	}

	/** {@inheritDoc} */
    @Override
	public String toString() {
		return "Video  ts: " + getTimestamp();
	}

	/**
     * Getter for frame type
     *
     * @return  Type of video frame
     */
    public FrameType getFrameType() {
		return frameType;
	}

	/** {@inheritDoc} */
    @Override
	protected void releaseInternal() {
		if (data != null) {
			data.release();
			data = null;
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		frameType = (FrameType) in.readObject();
		byte[] byteBuf = (byte[]) in.readObject();
		if (byteBuf != null) {
			data = ByteBuffer.allocate(byteBuf.length);
			data.setAutoExpand(true);
			SerializeUtils.ByteArrayToByteBuffer(byteBuf, data);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(frameType);
		if (data != null) {
			out.writeObject(SerializeUtils.ByteBufferToByteArray(data));
		} else {
			out.writeObject(null);
		}
	}
}