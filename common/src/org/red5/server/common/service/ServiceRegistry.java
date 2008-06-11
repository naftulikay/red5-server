package org.red5.server.common.service;

public interface ServiceRegistry {
	/**
	 * Register a service object with the specified service name.
	 * Name should be unique.
	 * 
	 * @param serviceName The name of service. It could be <tt>null</tt>
	 * to represent the default service object.
	 * @param serviceObject The service object to be registered.
	 * @return The registry key that can be used later for service lookup
	 * or unregister. If the service object is already registered, <tt>null</tt>
	 * is returned.
	 */
	ServiceRegistryKey registerService(String serviceName, Object serviceObject);
	
	/**
	 * Unregister the service by registry key.
	 * 
	 * @param registryKey
	 * @return </tt>true</tt> if success or <tt>false</tt> otherwise.
	 */
	boolean unregisterService(ServiceRegistryKey registryKey);
	
	/**
	 * Unregister the service by the service name and the service object.
	 * 
	 * @param serviceName The name of the service.
	 * @return </tt>true</tt> if success or <tt>false</tt> otherwise.
	 */
	boolean unregisterService(String serviceName);
	
	/**
	 * Get service object by registry key.
	 * @param registryKey
	 * @return
	 */
	Object getService(ServiceRegistryKey registryKey);
	
	/**
	 * Get the service object by service name.
	 * @param serviceName
	 * @return
	 */
	Object getService(String serviceName);
}
