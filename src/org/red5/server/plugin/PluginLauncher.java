package org.red5.server.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;

import org.java.plugin.ObjectFactory;
import org.java.plugin.Plugin;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLocation;
import org.red5.server.Server;
import org.red5.server.api.plugin.IRed5Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Creates the plug-in environment and cleans up on shutdown.
 * 
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class PluginLauncher implements ApplicationContextAware, InitializingBean, DisposableBean {

	// Initialize Logging
	protected static Logger log = LoggerFactory.getLogger(PluginLauncher.class);
	
	/**
	 * Spring application context
	 */
	private ApplicationContext applicationContext;
	
	private static PluginManager pluginManager;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		ApplicationContext common = (ApplicationContext) applicationContext.getBean("red5.common");
		Server server = (Server) common.getBean("red5.server");
		
		//server should be up and running at this point so load any plug-ins now			

	    //create instance of plug-in manager - uses config values from jpf.properties
	    pluginManager = ObjectFactory.newInstance(null).createManager();

	    //get the plugins dir
	    File pluginsDir = new File(System.getProperty("red5.root"), "plugins");
	    
	    File[] plugins = pluginsDir.listFiles(new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return name.toLowerCase().endsWith(".jar");
	        }
	    });
	    
	    PluginLocation[] locations = new PluginLocation[plugins.length];
	    for (int i = 0; i < plugins.length; i++) {
	        locations[i] = StandardPluginLocation.create(plugins[i]);
	    }
	    
	    //publish discovered plug-ins
	    pluginManager.publishPlugins(locations);			
	    
	    //add the server to each of our plugins so they may actual do something
	    Iterator<PluginDescriptor> it = pluginManager.getRegistry().getPluginDescriptors().iterator();
	    while (it.hasNext()) {
	        PluginDescriptor desc = (PluginDescriptor) it.next();
	        String descriptorId = desc.getId();
	        //PluginClassLoader pLoader = pluginManager.getPluginClassLoader(desc);
	        Plugin o = pluginManager.getPlugin(descriptorId);
	    	if (o instanceof IRed5Plugin) {
		        IRed5Plugin plugin = (IRed5Plugin) o;
		        //set top-level context
		        plugin.setApplicationContext(applicationContext);
		        //set server reference
		        plugin.setServer(server);
	    	}
	    }		
	}
	
	@Override
	public void destroy() throws Exception {
		pluginManager.shutdown();		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		log.debug("Setting application context");
		this.applicationContext = applicationContext;
	}
	
	
}
