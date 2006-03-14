package org.red5.server.api;

public interface IScopeResolver {

	public IScope resolveScope(String hostname, String path);
	
}