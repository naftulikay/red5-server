package org.red5.server.api;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;

public interface IContext extends ResourcePatternResolver {

	public IScopeResolver getScopeResolver();
	public IClientRegistry getClientRegistry();
	public IServiceInvoker getServiceInvoker();
	public IMappingStrategy getMappingStrategy();
	
	public Object lookupService(String serviceName);
	public IScopeHandler lookupScopeHandler(String contextPath);
	
	public ApplicationContext getApplicationContext();
	
}