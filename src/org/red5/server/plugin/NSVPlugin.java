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

import org.java.plugin.Plugin;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.Server;
import org.red5.server.api.plugin.IRed5Plugin;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * Provides a means to stream media via NSV.
 * 
 * @author Paul Gregoire
 */
public class NSVPlugin extends Plugin implements IRed5Plugin {

	private static Logger log = Red5LoggerFactory.getLogger(DummyPlugin.class, "plugins");
	
	@SuppressWarnings("unused")
	private ApplicationContext context;
	
	@SuppressWarnings("unused")
	private Server server;
	
	@Override
	protected void doStart() throws Exception {
		log.debug("Start");
	}

	@Override
	protected void doStop() throws Exception {
		log.debug("Stop");
	}

	@Override
	public void setApplicationContext(ApplicationContext context) {
		log.debug("Set application context: {}", context);
		this.context = context;
	}

	@Override
	public void setServer(Server server) {
		log.debug("Set server: {}", server);
		this.server = server;
	}
	
	
}