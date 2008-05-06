package org.red5.server.core.rtmp;

public interface RTMPApplicationManager {
	/**
	 * Register an application tied with a set of virtual hosts.
	 * The name of the application is the unique key.
	 * @param application
	 * @param virtualHosts
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise.
	 */
	boolean registerApplication(RTMPApplication application, String[] virtualHosts);
	
	/**
	 * Unregister an application by its name.
	 * @param appName
	 * @return The application unregistered.
	 */
	RTMPApplication unregisterApplication(String appName);
	
	/**
	 * Get the registered application with the app name.
	 * @param appName
	 * @return
	 */
	RTMPApplication getApplication(String appName);
	
	/**
	 * Get the registered application with the app name and the virtual host
	 * it belongs to.
	 * @param appName
	 * @param virtualHost
	 * @return
	 */
	RTMPApplication getApplication(String appName, String virtualHost);
	
	/**
	 * Get the virtual hosts a registered app belongs to.
	 * @param appName
	 * @return A set of virtual hosts the app belongs to or <tt>null</tt> if
	 * the application name is not registered.
	 */
	String[] getVirtualHosts(String appName);
}
