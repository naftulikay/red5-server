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

import org.red5.server.messaging.IMessage;
import org.red5.server.messaging.IPipe;
import org.red5.server.messaging.IPipeConnectionListener;
import org.red5.server.messaging.IPullableProvider;
import org.red5.server.messaging.PipeConnectionEvent;
import org.red5.server.stream.FileStreamSource;

/**
 * Adapter to wrap a FileStreamSource.
 * @author The Red5 Project (red5@osflash.org)
 * @author Steven Gong (steven.gong@gmail.com)
 */
public class FileStreamSourceAdapter implements IPullableProvider, IPipeConnectionListener {
	private IPipe pipe;
	private FileStreamSource source;
	private int length;
	
	public FileStreamSourceAdapter(FileStreamSource source) {
		this.source = source;
	}
	
	public IMessage pullMessage() {
		if (!source.hasMore()) return null;
		RTMPMessage msg = new RTMPMessage();
		msg.setBody(source.dequeue());
		return msg;
	}

	public IMessage pullMessage(long wait) {
		return pullMessage();
	}
	
	public void seek(int ts) {
		source.seek(ts);
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void onPipeConnectionEvent(PipeConnectionEvent event) {
		switch (event.getType()) {
		case PipeConnectionEvent.CONSUMER_DISCONNECT:
			if (this.pipe == event.getSource()) close();
			break;
		case PipeConnectionEvent.PROVIDER_CONNECT_PULL:
			if (event.getProvider() == this) {
				this.pipe = (IPipe) event.getSource();
			}
			break;
		case PipeConnectionEvent.PROVIDER_DISCONNECT:
			if (this.pipe == event.getSource() && event.getProvider() == this) {
				this.pipe = null;
			}
		default:
			break;
		}
	}
	
	public void close() {
		if (this.pipe != null) {
			this.pipe.unsubscribe(this);
			source.close();
		}
	}
}
