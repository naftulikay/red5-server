package org.red5.server.api;

/**
 * Invoke a call against a context
 * 
 * @author luke
 */
public interface IServiceInvoker {

	public ICall invoke(ICall call, IContext context); // note no scope involved.
	
}
