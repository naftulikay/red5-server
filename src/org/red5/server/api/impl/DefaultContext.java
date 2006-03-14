package org.red5.server.api.impl;

import java.io.IOException;

import org.red5.server.api.IClientRegistry;
import org.red5.server.api.IContext;
import org.red5.server.api.IMappingStrategy;
import org.red5.server.api.IScope;
import org.red5.server.api.IScopeHandler;
import org.red5.server.api.IScopeResolver;
import org.red5.server.api.IServiceInvoker;
import org.red5.server.service.ServiceNotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

public class DefaultContext implements IContext, ApplicationContextAware {

	protected static final String RED5_SERVICE_PREFIX = "red5.";
	protected static final String SCOPE_RESOLVER = "scopeResolver";
	protected static final String CLIENT_REGISTRY = "clientRegistry";
	protected static final String SERVICE_INVOKER = "serviceInvoker";
	protected static final String MAPPING_STRATEGY = "mappingStrategy";
	
	private ApplicationContext applicationContext; 
	private String contextPath;
	
	private IScopeResolver scopeResolver;
	private IClientRegistry clientRegistry;
	private IServiceInvoker serviceInvoker;
	private IMappingStrategy mappingStrategy;
	
	public DefaultContext(ApplicationContext context, String contextPath){
		this.applicationContext = context;
		this.contextPath = contextPath;
	}

	public void setApplicationContext(ApplicationContext context) {
		this.applicationContext = context;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	public void setContextPath(String contextPath){
		this.contextPath = contextPath;
	}
	
	public void init() {
		scopeResolver = (IScopeResolver) applicationContext.getBean(
				RED5_SERVICE_PREFIX+SCOPE_RESOLVER);
		clientRegistry = (IClientRegistry) applicationContext.getBean(
				RED5_SERVICE_PREFIX+CLIENT_REGISTRY);
		serviceInvoker = (IServiceInvoker) applicationContext.getBean(
				RED5_SERVICE_PREFIX+SERVICE_INVOKER);
		mappingStrategy = (IMappingStrategy) applicationContext.getBean(
				RED5_SERVICE_PREFIX+MAPPING_STRATEGY);
	}

	public IClientRegistry getClientRegistry() {
		return clientRegistry;
	}

	public IScope getScope() {
		// TODO Auto-generated method stub
		return null;
	}

	public IServiceInvoker getServiceInvoker() {
		return serviceInvoker;
	}

	public Object lookupService(String serviceName) {
		serviceName = getMappingStrategy().mapServiceName(serviceName); 
		Object bean = applicationContext.getBean(serviceName);
		if(bean != null ) return bean;
		else throw new ServiceNotFoundException(serviceName);
	}

	public IScopeResolver getScopeResolver() {
		return scopeResolver;
	}

	public IScopeHandler lookupScopeHandler(String contextPath) {
		String scopeHandlerName = getMappingStrategy().mapScopeHandlerName(contextPath); 
		Object bean = applicationContext.getBean(scopeHandlerName);
		if(bean != null && bean instanceof IScopeHandler){
			return (IScopeHandler) bean;
		} else throw new ScopeHandlerNotFoundException(scopeHandlerName);
	}

	public IMappingStrategy getMappingStrategy() {
		return mappingStrategy;
	}

	public Resource[] getResources(String pattern) throws IOException {
		return applicationContext.getResources(pattern);
	}

	public Resource getResource(String path) {
		return applicationContext.getResource(path);
	}

}
