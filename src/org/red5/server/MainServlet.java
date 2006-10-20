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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.naming.factory.BeanFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.ContextLoaderServlet;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Entry point from which the server config file is loaded while
 * running within a J2EE application container.
 *
 * <p>This listener should be registered after Log4jConfigListener in web.xml,
 * if the latter is used.
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class MainServlet extends HttpServlet implements ServletContextListener {

	// Initialize Logging
	public static Logger logger = LogManager.getLogger(MainServlet.class.getName());

	protected static String red5Config = "/WEB-INF/classes/red5-default.xml";

	/* Handle to servlet context */
	private static ServletContext context;

	private ContextLoader contextLoader;	

	/**
	 * Name of the class path resource (relative to the ContextLoader class)
	 * that defines ContextLoader's default strategy names.
	 */
	private static final String DEFAULT_STRATEGIES_PATH = "ContextLoader.properties";

	private static final Properties defaultStrategies;

	static {
		// Load default strategy implementations from properties file.
		// This is currently strictly internal and not meant to be customized
		// by application developers.
		try {
			ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, ContextLoader.class);
			defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not load 'ContextLoader.properties': " + ex.getMessage());
		}
	}	
	
	/**
	 * Main entry point for the Red5 Server as a war
	 */
	//Notification that the web application is ready to process requests
	public void contextInitialized(ServletContextEvent sce) {
		if (null != context) {
			return;
		}
		context = sce.getServletContext();
		String prefix = context.getRealPath("/");

		this.contextLoader = createContextLoader();
		this.contextLoader.initWebApplicationContext(context);		
		
		long time = System.currentTimeMillis();

		logger.info("RED5 Server (http://www.osflash.org/red5)");
		logger.info("Loading red5 global context from: " + red5Config);
		logger.info("Path: " + prefix);
		
		try {
			// Detect root of Red5 configuration and set as system property
			String root;
			String classpath = System.getProperty("java.class.path");
			File fp = new File(prefix + red5Config);
			fp = fp.getCanonicalFile();
			if (!fp.isFile()) {
				// Given file does not exist, search it on the classpath
				String[] paths = classpath.split(System
						.getProperty("path.separator"));
				for (String element : paths) {
					fp = new File(element + "/" + red5Config);
					fp = fp.getCanonicalFile();
					if (fp.isFile()) {
						break;
					}
				}
			}			
			if (!fp.isFile()) {
				throw new Exception("could not find configuration file " + red5Config + " on your classpath " + classpath);
			}

			root = fp.getAbsolutePath();
			root = root.replace('\\', '/');
			int idx = root.lastIndexOf('/');
			root = root.substring(0, idx);
			//update classpath
			System.setProperty("java.class.path", File.pathSeparatorChar + root);
			logger.info("New classpath: " + System.getProperty("java.class.path"));
			//set configuration root
			System.setProperty("red5.config_root", root);
			logger.info("Setting configuation root to " + root);

			// Setup system properties so they can be evaluated by Jetty
			Properties props = new Properties();
			props.load(new FileInputStream(root + "/red5-web.properties"));
			Iterator it = props.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				if (key != null && !key.equals("")) {
					System.setProperty(key, props.getProperty(key));
				}
			}

			// Store root directory of Red5
			idx = root.lastIndexOf('/');
			root = root.substring(0, idx);
			if (System.getProperty("file.separator").equals("/")) {
				// Workaround for linux systems
				root = "/" + root;
			}
			System.setProperty("red5.root", root);
			logger.info("Setting Red5 root to " + root);

			//ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {"red5-core.xml", "red5-common.xml", "red5-default.xml"});
	        // of course, an ApplicationContext is just a BeanFactory
	        //BeanFactory factory = (BeanFactory) ctx;			
			
			//ContextSingletonBeanFactoryLocator.getInstance(red5Config).useBeanFactory("red5.common");
			
		} catch (Throwable e) {
			logger.error(e);
		}
		
		long startupIn = System.currentTimeMillis() - time;
		logger.info("Startup done in: " + startupIn + " ms");

	}

	/**
	 * Clearing the in-memory configuration parameters, we will receive
	 * notification that the servlet context is about to be shut down
	 */
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("Webapp shutdown, clearing configuration params.");
		if (this.contextLoader != null) {
			this.contextLoader.closeWebApplicationContext(context);
		}		
	}


	/**
	 * Create the ContextLoader to use. Can be overridden in subclasses.
	 * @return the new ContextLoader
	 */
	protected ContextLoader createContextLoader() {
		return new Red5ContextLoader();
	}

	/**
	 * Return the ContextLoader used by this listener.
	 */
	public ContextLoader getContextLoader() {
		return contextLoader;
	}	
	

/**
 * Performs the actual initialization work for the root application context.
 * Called by ContextLoaderListener and ContextLoaderServlet.
 * 
 * <p>Looks for a "contextClass" parameter at the web.xml context-param level
 * to specify the context class type, falling back to the default of
 * {@link XmlWebApplicationContext} if not found. With the default ContextLoader
 * implementation, any context class specified needs to implement
 * ConfigurableWebApplicationContext.
 *
 * <p>Passes a "contextConfigLocation" context-param to the context instance,
 * parsing it into potentially multiple file paths which can be separated by
 * any number of commas and spaces, like "applicationContext1.xml,
 * applicationContext2.xml". If not explicitly specified, the context
 * implementation is supposed to use a default location (with
 * XmlWebApplicationContext: "/WEB-INF/applicationContext.xml").
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files, at least when using one of
 * Spring's default ApplicationContext implementations. This can be leveraged
 * to deliberately override certain bean definitions via an extra XML file.
 *
 * <p>Above and beyond loading the root application context, this class can
 * optionally load or obtain and hook up a shared parent context to the root
 * application context. See the
 * {@link #loadParentContext(ServletContext)} method for more information.
 *
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @since 17.02.2003
 * @see ContextLoaderListener
 * @see ContextLoaderServlet
 * @see ConfigurableWebApplicationContext
 * @see org.springframework.web.context.support.XmlWebApplicationContext
 */
public class Red5ContextLoader extends ContextLoader {

	/**
	 * Config param for the root WebApplicationContext implementation class to
	 * use: "contextClass"
	 */
	public static final String CONTEXT_CLASS_PARAM = "contextClass";

	/**
	 * Name of servlet context parameter that can specify the config location
	 * for the root context, falling back to the implementation's default
	 * otherwise.
	 * @see org.springframework.web.context.support.XmlWebApplicationContext#DEFAULT_CONFIG_LOCATION
	 */
	public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";

	/**
	 * Optional servlet context parameter used only when obtaining a parent
	 * context using the default implementation of
	 * {@link #loadParentContext(ServletContext servletContext)}. Specifies the
	 * 'selector' used in the
	 * {@link ContextSingletonBeanFactoryLocator#getInstance(String selector)}
	 * method call used to obtain the BeanFactoryLocator instance from which the
	 * parent context is obtained.
	 * <p>This will normally be set to <code>classpath*:beanRefContext.xml</code>
	 * to match the default applied for the
	 * {@link ContextSingletonBeanFactoryLocator#getInstance()} method.
	 */
	public static final String LOCATOR_FACTORY_SELECTOR_PARAM = "locatorFactorySelector";

	/**
	 * Optional servlet context parameter used only when obtaining a parent
	 * context using the default implementation of
	 * {@link #loadParentContext(ServletContext servletContext)}. Specifies the
	 * 'factoryKey' used in the
	 * {@link BeanFactoryLocator#useBeanFactory(String factoryKey)} method call
	 * used to obtain the parent application context from the BeanFactoryLocator
	 * instance.
	 */
	public static final String LOCATOR_FACTORY_KEY_PARAM = "parentContextKey";

	/**
	 * The root WebApplicationContext instance that this loaded manages.
	 */
	private WebApplicationContext context;

	/**
	 * Holds BeanFactoryReference when loading parent factory via
	 * ContextSingletonBeanFactoryLocator.
	 */
	private BeanFactoryReference parentContextRef;

	/**
	 * Initialize Spring's web application context for the given servlet context,
	 * according to the "contextClass" and "contextConfigLocation" context-params.
	 * @param servletContext current servlet context
	 * @return the new WebApplicationContext
	 * @throws IllegalStateException if there is already a root application context present
	 * @throws BeansException if the context failed to initialize
	 * @see #CONTEXT_CLASS_PARAM
	 * @see #CONFIG_LOCATION_PARAM
	 */
	public WebApplicationContext initWebApplicationContext(ServletContext servletContext)
			throws IllegalStateException, BeansException {

		if (servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) != null) {
			throw new IllegalStateException(
					"Cannot initialize context because there is already a root application context present - " +
					"check whether you have multiple ContextLoader* definitions in your web.xml!");
		}

		long startTime = System.currentTimeMillis();
		if (logger.isInfoEnabled()) {
			logger.info("Root WebApplicationContext: initialization started");
		}
		servletContext.log("Loading Spring root WebApplicationContext");

		try {
			// Determine parent for root web application context, if any.
			ApplicationContext parent = loadParentContext(servletContext);

			// Store context in local instance variable, to guarantee that
			// it is available on ServletContext shutdown.
			this.context = createWebApplicationContext(servletContext, parent);
			servletContext.setAttribute(
					WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.context);

			if (logger.isInfoEnabled()) {
				logger.info("Using context class [" + this.context.getClass().getName() +
						"] for root WebApplicationContext");
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Published root WebApplicationContext [" + this.context +
						"] as ServletContext attribute with name [" +
						WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
			}
			if (logger.isInfoEnabled()) {
				long elapsedTime = System.currentTimeMillis() - startTime;
				logger.info("Root WebApplicationContext: initialization completed in " + elapsedTime + " ms");
			}

			return this.context;
		}
		catch (RuntimeException ex) {
			logger.error("Context initialization failed", ex);
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ex);
			throw ex;
		}
		catch (Error err) {
			logger.error("Context initialization failed", err);
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, err);
			throw err;
		}
	}

	/**
	 * Instantiate the root WebApplicationContext for this loader, either the
	 * default context class or a custom context class if specified.
	 * <p>This implementation expects custom contexts to implement
	 * ConfigurableWebApplicationContext. Can be overridden in subclasses.
	 * @param servletContext current servlet context
	 * @param parent the parent ApplicationContext to use, or <code>null</code> if none
	 * @return the root WebApplicationContext
	 * @throws BeansException if the context couldn't be initialized
	 * @see ConfigurableWebApplicationContext
	 */
	protected WebApplicationContext createWebApplicationContext(
			ServletContext servletContext, ApplicationContext parent) throws BeansException {

		Class contextClass = determineContextClass(servletContext);
		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
			throw new ApplicationContextException("Custom context class [" + contextClass.getName() +
					"] is not of type ConfigurableWebApplicationContext");
		}

		ConfigurableWebApplicationContext wac =
				(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
		wac.setParent(parent);
		wac.setServletContext(servletContext);
		String configLocation = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
		if (configLocation != null) {
			wac.setConfigLocations(StringUtils.tokenizeToStringArray(configLocation,
					ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS));
		}

		wac.refresh();
		return wac;
	}

	/**
	 * Return the WebApplicationContext implementation class to use, either the
	 * default XmlWebApplicationContext or a custom context class if specified.
	 * @param servletContext current servlet context
	 * @return the WebApplicationContext implementation class to use
	 * @throws ApplicationContextException if the context class couldn't be loaded
	 * @see #CONTEXT_CLASS_PARAM
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	protected Class determineContextClass(ServletContext servletContext) throws ApplicationContextException {
		String contextClassName = servletContext.getInitParameter(CONTEXT_CLASS_PARAM);
		if (contextClassName != null) {
			try {
				return ClassUtils.forName(contextClassName);
			}
			catch (ClassNotFoundException ex) {
				throw new ApplicationContextException(
						"Failed to load custom context class [" + contextClassName + "]", ex);
			}
		}
		else {
			contextClassName = defaultStrategies.getProperty(WebApplicationContext.class.getName());
			try {
				return ClassUtils.forName(contextClassName);
			}
			catch (ClassNotFoundException ex) {
				throw new ApplicationContextException(
						"Failed to load default context class [" + contextClassName + "]", ex);
			}
		}
	}

	/**
	 * Template method with default implementation (which may be overridden by a
	 * subclass), to load or obtain an ApplicationContext instance which will be
	 * used as the parent context of the root WebApplicationContext. If the
	 * return value from the method is null, no parent context is set.
	 * <p>The main reason to load a parent context here is to allow multiple root
	 * web application contexts to all be children of a shared EAR context, or
	 * alternately to also share the same parent context that is visible to
	 * EJBs. For pure web applications, there is usually no need to worry about
	 * having a parent context to the root web application context.
	 * <p>The default implementation uses ContextSingletonBeanFactoryLocator,
	 * configured via {@link #LOCATOR_FACTORY_SELECTOR_PARAM} and
	 * {@link #LOCATOR_FACTORY_KEY_PARAM}, to load a parent context
	 * which will be shared by all other users of ContextsingletonBeanFactoryLocator
	 * which also use the same configuration parameters.
	 * @param servletContext current servlet context
	 * @return the parent application context, or <code>null</code> if none
	 * @throws BeansException if the context couldn't be initialized
	 * @see org.springframework.beans.factory.access.BeanFactoryLocator
	 * @see org.springframework.context.access.ContextSingletonBeanFactoryLocator
	 */
	protected ApplicationContext loadParentContext(ServletContext servletContext)
			throws BeansException {

		ApplicationContext parentContext = null;

		String locatorFactorySelector = servletContext.getInitParameter(LOCATOR_FACTORY_SELECTOR_PARAM);
		String parentContextKey = servletContext.getInitParameter(LOCATOR_FACTORY_KEY_PARAM);

		if (locatorFactorySelector != null) {
			BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance(locatorFactorySelector);

			if (logger.isInfoEnabled()) {
				logger.info("Getting parent context definition: using parent context key of '" +
						parentContextKey + "' with BeanFactoryLocator");
			}

			this.parentContextRef = locator.useBeanFactory(parentContextKey);
			parentContext = (ApplicationContext) this.parentContextRef.getFactory();
		}

		return parentContext;
	}


	/**
	 * Close Spring's web application context for the given servlet context. If
	 * the default {@link #loadParentContext(ServletContext)}implementation,
	 * which uses ContextSingletonBeanFactoryLocator, has loaded any shared
	 * parent context, release one reference to that shared parent context.
	 * <p>If overriding {@link #loadParentContext(ServletContext)}, you may have
	 * to override this method as well.
	 */
	public void closeWebApplicationContext(ServletContext servletContext) {
		servletContext.log("Closing Spring root WebApplicationContext");
		try {
			if (this.context instanceof ConfigurableWebApplicationContext) {
				((ConfigurableWebApplicationContext) this.context).close();
			}
		}
		finally {
			if (this.parentContextRef != null) {
				this.parentContextRef.release();
			}
		}
	}

}
}