package org.red5.server.net.rtmp.codec;

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

import java.util.HashMap;
import java.util.Map;

import org.red5.server.net.protocol.ProtocolState;
import org.red5.server.net.rtmp.message.Header;
import org.red5.server.net.rtmp.message.Packet;

public class RTMP extends ProtocolState {
	
	public static final byte STATE_CONNECT = 0x00;
	public static final byte STATE_HANDSHAKE = 0x01;
	public static final byte STATE_CONNECTED = 0x02;
	public static final byte STATE_ERROR = 0x03;
	public static final byte STATE_DISCONNECTED = 0x04;
	
	public static final boolean MODE_CLIENT = true;
	public static final boolean MODE_SERVER = false;
	
	public static final int DEFAULT_CHUNK_SIZE = 128;
	
	private byte state = STATE_CONNECT;
	private boolean mode = MODE_SERVER;
	private boolean debug = false;
	
	private int lastReadChannel = 0x00;
	private int lastWriteChannel = 0x00;
	private Map<Integer, Header> readHeaders = new HashMap<Integer, Header>(); 
	private Map<Integer, Header> writeHeaders = new HashMap<Integer, Header>(); 
	private Map<Integer, Packet> readPackets = new HashMap<Integer, Packet>();
	private Map<Integer, Packet> writePackets = new HashMap<Integer, Packet>();
	private int readChunkSize = DEFAULT_CHUNK_SIZE;
	private int writeChunkSize = DEFAULT_CHUNK_SIZE;
	
	private long bytesRead = 0;
	private long bytesWritten = 0; 
	private long bytesRecieved = 0;
	
	public RTMP(boolean mode){
		this.mode = mode;
	}
	
	public boolean getMode(){
		return mode;
	}
	
	public boolean isDebug() {
		return debug;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public byte getState() {
		return state;
	}
	
	private void freePackets(Map<Integer, Packet> packets) {
		for (Packet packet : packets.values()) {
			if (packet != null && packet.getData() != null) {
				packet.getData().release();
				packet.setData(null);
			}
		}
		packets.clear();
	}
	
	public void setState(byte state) {
		this.state = state;
		if (state == STATE_DISCONNECTED) {
			// Free temporary packets
			freePackets(readPackets);
			freePackets(writePackets);
		}
	}
	
	public void setLastReadHeader(int channelId, Header header){
		lastReadChannel = channelId;
		readHeaders.put(channelId, header);
	}
	
	public Header getLastReadHeader(int channelId){
		return readHeaders.get(channelId);
	}
	
	public void setLastWriteHeader(int channelId, Header header){
		lastWriteChannel = channelId;
		writeHeaders.put(channelId, header);
	}
	
	public Header getLastWriteHeader(int channelId){
		return writeHeaders.get(channelId);
	}

	public void setLastReadPacket(int channelId, Packet packet){
		Packet prevPacket = readPackets.get(channelId);
		if (prevPacket != null && prevPacket.getData() != null) {
			prevPacket.getData().release();
			prevPacket.setData(null);
		}

		readPackets.put(channelId, packet);
	}
	
	public Packet getLastReadPacket(int channelId){
		return readPackets.get(channelId);
	}
	
	public void setLastWritePacket(int channelId, Packet packet){
		Packet prevPacket = writePackets.get(channelId);
		if (prevPacket != null && prevPacket.getData() != null) {
			prevPacket.getData().release();
			prevPacket.setData(null);
		}

		writePackets.put(channelId, packet);
	}
	
	public Packet getLastWritePacket(int channelId){
		return writePackets.get(channelId);
	}

	public int getLastReadChannel() {
		return lastReadChannel;
	}

	public int getLastWriteChannel() {
		return lastWriteChannel;
	}

	public int getReadChunkSize() {
		return readChunkSize;
	}

	public void setReadChunkSize(int readChunkSize) {
		this.readChunkSize = readChunkSize;
	}

	public int getWriteChunkSize() {
		return writeChunkSize;
	}

	public void setWriteChunkSize(int writeChunkSize) {
		this.writeChunkSize = writeChunkSize;
	}
	
}