package org.red5.server.common.service;

public interface ServiceRegistry {
	/**
	 * Register a service object with the specified service name.
	 * Multiple service objects can be registered with the same name.
	 * The same service object can also be registered with multiple names.
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
	 * @param serviceObject
	 * @return </tt>true</tt> if success or <tt>false</tt> otherwise.
	 */
	boolean unregisterService(String serviceName, Object serviceObject);
	
	/**
	 * Get service object by registry key.
	 * @param registryKey
	 * @return
	 */
	Object getService(ServiceRegistryKey registryKey);
	
	/**
	 * Get all the service objects by service name.
	 * @param serviceName
	 * @return
	 */
	Object[] getService(String serviceName);
	
	/**
	 * Get all the service objects by service name and method name
	 * that the service object contains.
	 * @param serviceName
	 * @param methodName
	 * @return
	 */
	Object[] getService(String serviceName, String methodName);
}
