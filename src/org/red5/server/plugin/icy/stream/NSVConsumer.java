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

import org.red5.server.plugin.icy.IFlowControl;
import org.red5.server.plugin.icy.IICYMarshal;
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
public class NSVConsumer implements IFlowControl, Runnable {

	public static int SERVER_MODE = 0;

	public static int CLIENT_MODE = 1;

	private IICYMarshal handler;

	private int port;

	private String password = "changeme";

	private NSVThread nsv;

	private int mode = 1;

	private String host;

	private boolean mKeepRunning = true;

	private int waitTime = 50;

	public NSVConsumer(int serverType, IICYMarshal pHandler, String host) {
		handler = pHandler;
		this.host = host;
		mode = serverType;
	}

	public NSVConsumer(int serverType, IICYMarshal pHandler) {
		handler = pHandler;
		this.host = "";
		mode = serverType;
	}

	public IICYMarshal getMarshal() {
		return handler;
	}

	public NSVThread getNSVThread() {
		return nsv;
	}

	public void setHost(String val) {
		host = val;
		if (nsv != null)
			nsv.setHost(val);
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
		if (nsv != null) {
			nsv.setPort(val);
		}
	}

	public void setPassword(String val) {
		password = val;
		if (nsv != null) {
			nsv.setPassword(val);
		}
	}

	@Override
	public void run() {
		
		nsv = new NSVThread(mode, host, handler, this, new NSVSenderThread(handler));
		nsv.setPort(port);
		nsv.setPassword(password);
		nsv.listen();

		while (mKeepRunning) {

			process();

			try {
				Thread.sleep(5000);
			} catch (Exception e) {

			}

		}
	}

	private void process() {

		while (mKeepRunning) {
			try {
				nsv.execute(null);
			} catch (CloneNotSupportedException e1) {
				e1.printStackTrace();
			}

			try {
				Thread.sleep(waitTime);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void notifyIdler(int stat) {
		if (stat == 0) {
			waitTime = 100;
		} else {
			waitTime = 1;
		}
		waitTime = (waitTime < 1) ? 1 : waitTime;
		waitTime = (waitTime > 300) ? 300 : waitTime;
	}

	public void start() {

		thread.start();
	}

	public void stop() {
		mKeepRunning = false;
	}

	public boolean isConnected() {
		if (nsv != null) {
			return nsv.isConnected();
		} else {
			return false;
		}
	}

}
