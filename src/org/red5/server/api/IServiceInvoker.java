package org.red5.server.api;

public interface IServiceInvoker {

	public ICall invoke(ICall call, IContext context); // note no scope involved.
	
}
