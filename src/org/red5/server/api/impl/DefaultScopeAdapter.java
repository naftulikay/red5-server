package org.red5.server.api.impl;

import org.red5.server.api.ICall;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.IScopeHandler;

public class DefaultScopeAdapter implements IScopeHandler {

	private boolean canStart = true;
	private boolean canConnect = true;
	private boolean canJoin = true;
	private boolean canCallService = true;
	private boolean canBroadcastEvent = true;
	private boolean canAddChildScope = true;
	
	public void setCanStart(boolean canStart) {
		this.canStart = canStart;
	}

	public void setCanBroadcastEvent(boolean canBroadcastEvent) {
		this.canBroadcastEvent = canBroadcastEvent;
	}

	public void setCanCallService(boolean canCallService) {
		this.canCallService = canCallService;
	}

	public void setCanConnect(boolean canConnect) {
		this.canConnect = canConnect;
	}

	public void setJoin(boolean canJoin) {
		this.canJoin = canJoin;
	}
	
	public boolean start(IScope scope) {
		return canStart;
	}

	public void stop(IScope scope) {
		// nothing
	}
	
	public boolean connect(IConnection conn) {
		return canConnect;
	}
	
	public void disconnect(IConnection conn) {
		// nothing
	}

	public boolean join(IClient client, IScope scope) {
		return canJoin;
	}
	
	public void leave(IClient client, IScope scope){
		// nothing
	}
	
	public boolean eventBroadcast(Object event) {
		return canBroadcastEvent;
	}

	public boolean serviceCall(ICall call) {
		return canCallService;
	}

	public ICall postProcessServiceCall(ICall call) {
		return call;
	}

	public ICall preProcessServiceCall(ICall call) {
		return call;
	}

	public boolean addChildScope(IScope scope) {
		return canAddChildScope;
	}

	public void removeChildScope(IScope scope) {
		// TODO Auto-generated method stub	
	}
	
	
	/*
	
	private boolean canConnectSharedObject = true;
	private boolean canDeleteSharedObject = true;
	private boolean canUpdateSharedObject = true;
	private boolean canSendSharedObject = true;
	private boolean canPublishStream = true;
	private boolean canRecordStream = true;
	private boolean canBroadcastStream = true;
	private boolean canSubscribeToBroadcastStream = true;
	private boolean canConnectToOnDemandStream = true;
	
	*/
}