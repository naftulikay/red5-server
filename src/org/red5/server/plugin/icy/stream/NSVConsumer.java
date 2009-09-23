package org.red5.server.plugin.icy.stream;

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
import org.red5.server.plugin.icy.StreamManager;
import org.red5.server.plugin.icy.parser.NSVSenderThread;
import org.red5.server.plugin.icy.parser.NSVThread;

/**
 * The NSVConsumer will consume or subscribe to data from a winamp shoutcast dsp, 
 * nsv encoder, or a shoutcast server.
 * 
 * @author Wittawas Nakkasem (vittee@hotmail.com)
 * @author Andy Shaules (bowljoman@hotmail.com)
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class NSVConsumer {

	public static int SERVER_MODE = 0;

	public static int CLIENT_MODE = 1;

	private IICYMarshal handler;

	private int port;

	private String password = "changeme";

	private NSVThread nsv;

	private int mode = 1;

	private String host;

	public NSVConsumer(int serverType, IICYMarshal handler, String host) {
		mode = serverType;
		this.handler = handler;
		this.host = host;
	}

	public NSVConsumer(int serverType, IICYMarshal handler) {
		mode = serverType;
		this.handler = handler;
		this.host = "";
	}
	
	public void init() {
		//create a thread to handle the nsv stream
		nsv = new NSVThread(mode, host, handler, new NSVSenderThread(handler));
		nsv.setPort(port);
		nsv.setPassword(password);
		//initialize the inputs
		nsv.listen();
		//submit the thread for execution
		StreamManager.submit(nsv);
	}

	public void stop() {
		if (nsv != null) {
			nsv.stop();
		}
	}

	public boolean isConnected() {
		if (nsv != null) {
			return nsv.isConnected();
		} else {
			return false;
		}
	}

	public IICYMarshal getMarshal() {
		return handler;
	}

	public NSVThread getNSVThread() {
		return nsv;
	}

	public void setHost(String val) {
		host = val;
	}

	public int getMode() {
		if (nsv != null) {
			return nsv.getMode();
		} else {
			return mode;
		}
	}

	public void setPort(int val) {
		port = val;
	}

	public void setPassword(String val) {
		password = val;
	}

}
