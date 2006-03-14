package org.red5.server.api;

/**
 * Resolve the scope given a host and path
 */
public interface IScopeResolver {

	public IScope resolveScope(String hostname, String path);
	
}