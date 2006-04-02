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

import org.red5.server.messaging.AbstractMessage;
import org.red5.server.net.rtmp.message.Message;

/**
 * Wrapper for original Message class.
 * @author The Red5 Project (red5@osflash.org)
 * @author Steven Gong (steven.gong@gmail.com)
 */
public class RTMPMessage extends AbstractMessage {
	private Message body;

	public Message getBody() {
		return body;
	}

	public void setBody(Message body) {
		this.body = body;
	}
	
}
