package org.red5.server.tomcat;

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

import java.io.File;
import java.io.FileInputStream;
import java.util.EventListener;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.Context;
import org.red5.server.ContextLoader;
import org.red5.server.TomcatLoader;
import org.red5.server.WebScope;
import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IClientRegistry;
import org.red5.server.api.IMappingStrategy;
import org.red5.server.api.IScopeResolver;
import org.red5.server.api.IServer;
import org.red5.server.api.service.IServiceInvoker;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class Red5WebPropertiesConfiguration implements EventListener {

	// Initialize Logging
	protected static Log log = LogFactory
			.getLog(Red5WebPropertiesConfiguration.class.getName());

	protected static org.apache.catalina.Context _context;

	public void setWebAppContext(org.apache.catalina.Context context) {
		_context = context;
	}

	public org.apache.catalina.Context getWebAppContext() {
		return _context;
	}

	public void configureClassLoader() throws Exception {
		// TODO Auto-generated method stub
	}

	public void configureDefaults() throws Exception {
		// TODO Auto-generated method stub
	}

	public void configureWebApp() throws Exception {
		log.debug("Configuring Tomcat webapp");
		org.apache.catalina.Context context = getWebAppContext();
		if (context.getConfigured()) {
			log.debug("Cannot configure webapp after it is started");
			return;
		}

		log.debug("---> Config file: " + context.getConfigFile());
		File webInf = new File(context.getConfigFile());
		if (webInf != null && webInf.isDirectory()) {
			File config = new File(webInf.getCanonicalFile()
					+ "red5-web.properties");
			if (config.exists()) {
				log.debug("Configuring red5-web.properties");
				Properties props = new Properties();
				props.load(new FileInputStream(config));
				String contextPath = props.getProperty("webapp.contextPath");
				String virtualHosts = props.getProperty("webapp.virtualHosts");
				String[] hostnames = virtualHosts.split(",");
				for (int i = 0; i < hostnames.length; i++) {
					hostnames[i] = hostnames[i].trim();
					if (hostnames[i].equals("*")) {
						// A virtual host "null" must be used so requests for
						// any host will be served.
						hostnames = null;
						break;
					}
				}
				log.debug("ContextPath: " + contextPath);
				log.debug("Virtuals: " + virtualHosts);
				// context.setVirtualHosts(hostnames);
				// context.setContextPath(contextPath);
			}
		} else if (webInf == null) {
			// No WEB-INF directory found, register as default application
			log.info("No WEB-INF directory found for " + context.getPath()
					+ ", creating default application.");
			BeanFactoryLocator bfl = ContextSingletonBeanFactoryLocator
					.getInstance("red5.xml");
			BeanFactoryReference bfr = bfl.useBeanFactory("red5.common");

			// Create WebScope dynamically
			WebScope scope = new WebScope();
			IServer server = (IServer) bfr.getFactory().getBean(IServer.ID);
			scope.setServer(server);
			scope.setGlobalScope(server.getGlobal("default"));

			// Get default Red5 context
			ApplicationContext appCtx = TomcatLoader.getApplicationContext();
			ContextLoader loader = (ContextLoader) appCtx
					.getBean("context.loader");
			appCtx = loader.getContext("default.context");

			// Create context for the WebScope and initialize
			Context scopeContext = new Context();
			scopeContext.setContextPath("/");
			scopeContext.setClientRegistry((IClientRegistry) appCtx
					.getBean("global.clientRegistry"));
			scopeContext.setMappingStrategy((IMappingStrategy) appCtx
					.getBean("global.mappingStrategy"));
			scopeContext.setServiceInvoker((IServiceInvoker) appCtx
					.getBean("global.serviceInvoker"));
			scopeContext.setScopeResolver((IScopeResolver) appCtx
					.getBean("red5.scopeResolver"));

			// The context needs an ApplicationContext so resources can be
			// resolved
			GenericWebApplicationContext webCtx = new GenericWebApplicationContext();
			webCtx.setDisplayName("Automatic generated WebAppContext");
			webCtx.setParent(appCtx);
			// webCtx.setServletContext(ContextHandler.getCurrentContext());
			webCtx.setServletContext(context.getServletContext());
			scopeContext.setApplicationContext(webCtx);

			// Store context in scope
			scope.setContext(scopeContext);

			// Use default ApplicationAdapter as handler
			scope.setHandler(new ApplicationAdapter());

			// Make available as "/<directoryName>" and allow access from all
			// hosts
			scope.setContextPath(context.getPath());
			scope.setVirtualHosts("*");

			// Register WebScope in server
			scope.register();
		}
	}

	public void deconfigureWebApp() throws Exception {
		// TODO Auto-generated method stub
	}

}
