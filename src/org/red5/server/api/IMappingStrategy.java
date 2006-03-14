package org.red5.server.api;

public interface IMappingStrategy {

	public String mapServiceName(String name);
	public String mapScopeHandlerName(String contextPath);
	public String mapResourcePrefix(String contextPath);
	
}