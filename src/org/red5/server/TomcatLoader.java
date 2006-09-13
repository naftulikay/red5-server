package org.red5.server;

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

import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Realm;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class TomcatLoader implements ApplicationContextAware {
	// Initialize Logging
	protected static Log log = LogFactory.getLog(TomcatLoader.class.getName());

	// Instance variables:
	private String name = "red5";

	private int portNumber = 8080;

	private Embedded embedded;
	private Realm realm;
	private Engine engine;

	private Host baseHost;

	private Connector connector;

	// We store the application context in a ThreadLocal so we can access it
	// from "org.red5.server.tomcat.Red5WebPropertiesConfiguration" later.
	private static ThreadLocal<ApplicationContext> applicationContext = new ThreadLocal<ApplicationContext>();
	
	{
		//shutdown in case of untimely death
    Runtime.getRuntime().addShutdownHook(new ShutdownHook());
	}
	
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		applicationContext.set(context);
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext.get();
	}

	public void init() {
		log.info("Loading tomcat context");

		try {
			getApplicationContext();
		} catch (Exception e) {
			log.error("Error loading tomcat configuration", e);
		}
	
		String prt = null;
		this.portNumber = (null == (prt = System.getProperty("http.port")) ? 8080
				: Integer.parseInt(prt));

		org.apache.catalina.Context context;
		String baseEngineName;
		String hostName;
		//root location for servlet container 
		String serverRoot = System.getProperty("red5.root");
		log.info("Server root: " + serverRoot);
		//root location for servlet container 
		String appRoot = serverRoot + "/webapps";
		log.info("Application root: " + appRoot);
		//set in the system for tomcat classes
		System.setProperty("catalina.home", serverRoot);

		// set default logger
		//FileLogger fileLog = new FileLogger(); fileLog.setDirectory(".");
		//fileLog.setPrefix(name); fileLog.setSuffix(".log");
		//fileLog.setTimestamp(true); embedded.setLogger(fileLog);

		//realm.setPathname(System.getProperty("red5.config_root") + File.separatorChar);
		//realm.setPathname("C:/servers/tomcat/conf/tomcat-users.xml");
		embedded.setRealm(realm);

		// create an Engine
		////engine = embedded.createEngine();

		// set Engine properties
		baseEngineName = name + "Engine";
		//hostName = name + "Host";
		hostName = "localhost";

		engine.setName(baseEngineName);
		engine.setDefaultHost(hostName);

		baseHost = embedded.createHost(hostName, appRoot);
		engine.addChild(baseHost);

		// RootContext
		context = addContext("", appRoot + "/root");
		
		//load up any additional contexts
		context = addContext("oflaDemo", appRoot + "/oflaDemo");
		// set any props on the catalina context
		log.debug("----> Context: " + context.toString());
		
		// add new Engine to set of Engine for embedded server
		embedded.addEngine(engine);

		// add new Connector to set of Connectors for embedded server,
		// associated with Engine
		embedded.addConnector(connector);

		// start server
		try {
			log.info("Starting tomcat servlet engine");
			embedded.start();
		} catch (org.apache.catalina.LifecycleException e) {
			log.error("Error loading tomcat", e);
			// fileLog.log("Startup failed");
			// fileLog.log(ex.getMessage());
			//e.printStackTrace();
		}
	}

	public Realm getRealm()
	{
		return realm;
	}

	public void setRealm(Realm realm)
	{
		log.info("Setting realm: " + realm.getClass().getName());
		this.realm = realm;
	}

	public Engine getEngine()
	{
		return engine;
	}

	public void setEngine(Engine engine)
	{
		log.info("Setting engine: " + engine.getClass().getName());
		this.engine = engine;
	}

	public Host getBaseHost()
	{
		return baseHost;
	}

	public void setBaseHost(Host baseHost)
	{
		this.baseHost = baseHost;
	}

	public Embedded getEmbedded()
	{
		return embedded;
	}

	public void setEmbedded(Embedded embedded)
	{
		log.info("Setting embedded: " + embedded.getClass().getName());
		this.embedded = embedded;
	}

	public Connector getConnector()
	{
		return connector;
	}

	public void setConnector(Connector connector)
	{
		log.info("Setting connector: " + connector.getClass().getName());		
		this.connector = connector;
	}

	public org.apache.catalina.Context addContext(String path, String docBase) {
		org.apache.catalina.Context c = embedded.createContext(path, docBase);
		baseHost.addChild(c);
		return c;
	}

	public void shutdown() {
		log.info("Shutting down tomcat context");
    try {
    	embedded.stop();
    } catch (Exception e) {
    	log.warn("Tomcat could not be stopped", e);
    }		
	}
	
    /**
     * Catches an abrupt exit of the application and completes given
     * tasks prior to the actual exit.
     */
    class ShutdownHook extends Thread {
        public void run() {
            log.info("Shutdown hook called");
            shutdown();
        }
    }
}