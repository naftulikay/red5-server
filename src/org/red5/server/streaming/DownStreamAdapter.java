/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright ? 2006 by respective authors (see below). All rights reserved.
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
package org.red5.server.streaming;

import org.red5.server.messaging.IConsumer;
import org.red5.server.messaging.IPipe;
import org.red5.server.messaging.IPipeConnectionListener;
import org.red5.server.messaging.PipeConnectionEvent;
import org.red5.server.net.rtmp.message.AudioData;
import org.red5.server.net.rtmp.message.Message;
import org.red5.server.net.rtmp.message.Status;
import org.red5.server.stream.DownStreamSink;
import org.red5.server.stream.Stream;

/**
 * Adapter to wrap a DownStreamSink.
 * @author The Red5 Project (red5@osflash.org)
 * @author Steven Gong (steven.gong@gmail.com)
 */
public class DownStreamAdapter implements IConsumer, IPipeConnectionListener {
	private Stream stream;
	private IPipe pipe;
	
	public DownStreamAdapter(Stream stream) {
		this.stream = stream;
	}
	
	public void start() {
		int streamId = stream.getStreamId();
		String name = stream.getName();
		DownStreamSink downstream = stream.getDownstream();
		
		Status reset = new Status(Status.NS_PLAY_RESET);
		Status start = new Status(Status.NS_PLAY_START);
		reset.setClientid(streamId);
		start.setClientid(streamId);
		reset.setDetails(name);
		start.setDetails(name);
		
		AudioData blankAudio = new AudioData();
		downstream.getData().write(blankAudio);

		downstream.getData().sendStatus(reset);
		downstream.getVideo().sendStatus(start);
	}
	
	synchronized public void messageSent(Message msg) {
		if (this.pipe != null) {
			RTMPMessage message = (RTMPMessage) this.pipe.pullMessage();
			if (message == null) return;
			stream.getDownstream().enqueue(message.getBody());
		}
	}

	public void onPipeConnectionEvent(PipeConnectionEvent event) {
		switch (event.getType()) {
		case PipeConnectionEvent.CONSUMER_CONNECT_PULL:
			if (this.pipe != null || event.getConsumer() != this) return;
			this.pipe = (IPipe) event.getSource();
			break;
		case PipeConnectionEvent.CONSUMER_DISCONNECT:
			if (this.pipe == event.getSource() && event.getConsumer() == this) {
				this.pipe = null;
			}
			break;
		default:
			break;
		}
	}
	
	public void close() {
		if (this.pipe != null) this.pipe.unsubscribe(this);
		stream.getDownstream().close();
	}
}
