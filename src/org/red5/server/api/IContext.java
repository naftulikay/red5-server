package org.red5.server.api;

import org.red5.server.api.service.IServiceInvoker;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * The current context, this object basically wraps the spring context
 * or in the case of the .Net version, any similar system.
 * 
 */
public interface IContext extends ResourcePatternResolver {

	// public IScopeResolver getScopeResolver();
	public IClientRegistry getClientRegistry();
	public IServiceInvoker getServiceInvoker();
	public IMappingStrategy getMappingStrategy();
	public Object lookupService(String serviceName);
	public IScopeHandler lookupScopeHandler(String path);
	public IScope resolveScope(String host, String path);
	public IScope getRootScope();
	public ApplicationContext getApplicationContext();
	
}