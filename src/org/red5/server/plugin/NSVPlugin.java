package org.red5.server.plugin;

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

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IScope;
import org.red5.server.plugin.icy.StreamManager;
import org.red5.server.plugin.icy.marshal.ICYMarshal;
import org.red5.server.plugin.icy.stream.NSVConsumer;
import org.slf4j.Logger;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Provides a means to stream media via NSV and Shoutcast.
 * 
 * @author Paul Gregoire (mondain@gmail.com)
 * @author Wittawas Nakkasem (vittee@hotmail.com)
 * @author Andy Shaules (bowljoman@hotmail.com)
 */
public class NSVPlugin extends Red5Plugin {

	private static Logger log = Red5LoggerFactory.getLogger(NSVPlugin.class, "plugins");

	private static FileSystemXmlApplicationContext nsvContext;
	
	/**
	 * Create a thread to listen for a connection from nsv or winamp shoutcast dsp encoders.
	 * @param outputScope the stream is registered in.
	 * @param outputName stream name in output scope.
	 * @param port Port to open.
	 * @param password Pass word to accept.
	 * @return The running thread wrapper.
	 */
	public static NSVConsumer openServerPort(IScope outputScope, String outputName, int port, String password) {
		ICYMarshal marsh = new ICYMarshal(outputScope, outputName);
		NSVConsumer nsv = new NSVConsumer(NSVConsumer.SERVER_MODE, marsh);
		nsv.setPort(port);
		nsv.setPassword(password);
		//lookup the stream manager
		if (nsvContext.containsBean("streamManager")) {
			StreamManager mgr = (StreamManager) nsvContext.getBean("streamManager");
			//add the consumer for execution
			mgr.addConsumer(nsv);
		}
		return nsv;
	}

	/**
	 * Create a thread to subscribe to a shoutcast server. Host format "http://host:port/;stream.nsv".
	 * Note. The ';' is not a typo. 
	 * @param outputScope The stream is registered to.
	 * @param outputName The output stream name.
	 * @param host	The url to subscribe to.
	 * @return The running thread wrapper.
	 */
	public static NSVConsumer openExternalURI(IScope outputScope, String outputName, String host) {
		ICYMarshal marsh = new ICYMarshal(outputScope, outputName);
		NSVConsumer nsv = new NSVConsumer(NSVConsumer.CLIENT_MODE, marsh, host);
		//lookup the stream manager
		if (nsvContext.containsBean("streamManager")) {
			StreamManager mgr = (StreamManager) nsvContext.getBean("streamManager");
			//add the consumer for execution
			mgr.addConsumer(nsv);
		}
		return nsv;
	}

	public void doStart() throws Exception {
		log.debug("Start");
		//create app context
		nsvContext = new FileSystemXmlApplicationContext(new String[]{"classpath:/nsv.xml"}, true, context);		
	}

	public void doStop() throws Exception {
		log.debug("Stop");
		nsvContext.close();
	}

	@Override
	public String getName() {
		return "nsvPlugin";
	}

}
