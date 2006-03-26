package org.red5.server.adapter;

import org.red5.server.api.IBasicScope;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.IScopeHandler;
import org.red5.server.api.event.IEvent;
import org.red5.server.api.service.IServiceCall;

public abstract class AbstractScopeAdapter implements IScopeHandler {

	private boolean canStart = true;
	private boolean canConnect = true;
	private boolean canJoin = true;
	private boolean canCallService = true;
	private boolean canAddChildScope = true;
	private boolean canHandleEvent = true;
	
	public void setCanStart(boolean canStart) {
		this.canStart = canStart;
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

	public boolean serviceCall(IConnection conn, IServiceCall call) {
		return canCallService;
	}

	public IServiceCall postProcessServiceCall(IConnection conn, IServiceCall call) {
		return call;
	}

	public IServiceCall preProcessServiceCall(IConnection conn, IServiceCall call) {
		return call;
	}

	public boolean addChildScope(IBasicScope scope) {
		return canAddChildScope;
	}

	public void removeChildScope(IBasicScope scope) {
		// TODO Auto-generated method stub	
	}
	
	public boolean handleEvent(IEvent event){
		return canHandleEvent;
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