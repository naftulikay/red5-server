package org.red5.server.api.impl;

import org.red5.server.api.IBroadcastStream;
import org.red5.server.api.ICall;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IOnDemandStream;
import org.red5.server.api.IScope;
import org.red5.server.api.IScopeAuth;
import org.red5.server.api.IScopeHandler;
import org.red5.server.api.ISharedObject;
import org.red5.server.api.IStream;

public class DefaultScopeAdapter implements IScopeHandler, IScopeAuth {

	private boolean canStart = true;
	private boolean canConnect = true;
	private boolean canJoin = true;
	private boolean canCallService = true;
	private boolean canBroadcastEvent = true;
	private boolean canConnectSharedObject = true;
	private boolean canDeleteSharedObject = true;
	private boolean canUpdateSharedObject = true;
	private boolean canSendSharedObject = true;
	private boolean canPublishStream = true;
	private boolean canRecordStream = true;
	private boolean canBroadcastStream = true;
	private boolean canSubscribeToBroadcastStream = true;
	private boolean canConnectToOnDemandStream = true;
	
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
	
	public void setCanConnectSharedObject(boolean canConnectSharedObject) {
		this.canConnectSharedObject = canConnectSharedObject;
	}

	public void setCanDeleteSharedObject(boolean canDeleteSharedObject) {
		this.canDeleteSharedObject = canDeleteSharedObject;
	}

	public void setCanSendSharedObject(boolean canSendSharedObject) {
		this.canSendSharedObject = canSendSharedObject;
	}

	public void setCanUpdateSharedObject(boolean canUpdateSharedObject) {
		this.canUpdateSharedObject = canUpdateSharedObject;
	}

	public void setCanPublishStream(boolean canPublishStream) {
		this.canPublishStream = canPublishStream;
	}
	
	public void setCanRecordStream(boolean canRecordStream) {
		this.canRecordStream = canRecordStream;
	}

	public void setCanBroadcastStream(boolean canBroadcastStream) {
		this.canBroadcastStream = canBroadcastStream;
	}

	public void setCanConnectToOnDemandStream(boolean canConnectToOnDemandStream) {
		this.canConnectToOnDemandStream = canConnectToOnDemandStream;
	}

	public void setCanSubscribeToBroadcastStream(boolean canSubscribeToBroadcastStream) {
		this.canSubscribeToBroadcastStream = canSubscribeToBroadcastStream;
	}

	public boolean canStart(String path) {
		return canStart;
	}

	public boolean canBroadcastEvent(Object event) {
		return canBroadcastEvent;
	}

	public boolean canCallService(ICall call) {
		return canCallService;
	}

	public boolean canConnect(IConnection conn, IScope scope) {
		return canConnect;
	}
	
	public boolean canJoin(IClient client, IScope scope) {
		return canJoin;
	}

	public boolean canConnectSharedObject(String soName) {
		return canConnectSharedObject;
	}

	public boolean canDeleteSharedObject(ISharedObject so, String key) {
		return canDeleteSharedObject;
	}

	public boolean canSendSharedObject(ISharedObject so, String method, Object[] args) {
		return canSendSharedObject;
	}

	public boolean canUpdateSharedObject(ISharedObject so, String key, Object value) {
		return canUpdateSharedObject;
	}

	public boolean canPublishStream(String name) {
		return canPublishStream;
	}

	public boolean canBroadcastStream(String name) {
		return canBroadcastStream;
	}

	public boolean canRecordStream(String name) {
		return canRecordStream;
	}

	public boolean canConnectToOnDemandStream(String name) {
		return canConnectToOnDemandStream;
	}

	public boolean canSubscribeToBroadcastStream(String name) {
		return canSubscribeToBroadcastStream;
	}

	public IScopeAuth getScopeAuth(IScope scope){
		// you can override this method to provide specific auth objects
		return this;
	}
	
	public void onStart(IScope scope) {
		// nothing
	}

	public void onStop(IScope scope) {
		// nothing
	}
	
	public void onConnect(IConnection conn) {
		// nothing
	}
	
	public void onDisconnect(IConnection conn) {
		// nothing
	}

	public void onJoin(IClient client, IScope scope) {
		// nothing
	}
	
	public void onLeave(IClient client, IScope scope){
		// nothing
	}
	
	public void onEventBroadcast(Object event) {
		// nothing
	}

	public void onServiceCall(ICall call) {
		// nothing
	}

	public void onSharedObjectConnect(ISharedObject so) {
		// nothing
	}

	public void onSharedObjectDelete(ISharedObject so, String key) {
		// nothing
	}

	public void onSharedObjectSend(ISharedObject so, String method, Object[] params) {
		// nothing
	}

	public void onSharedObjectUpdate(ISharedObject so, String key, Object value) {
		// nothing
	}

	public void onStreamPublish(IStream stream) {
		// nothing
	}

	public void onBroadcastStreamStart(IStream stream) {
		// TODO Auto-generated method stub
	}

	public void onBroadcastStreamSubscribe(IBroadcastStream stream) {
		// TODO Auto-generated method stub
	}

	public void onBroadcastStreamUnsubscribe(IBroadcastStream stream) {
		// TODO Auto-generated method stub		
	}

	public void onOnDemandStreamConnect(IOnDemandStream stream) {
		// TODO Auto-generated method stub
	}

	public void onOnDemandStreamDisconnect(IOnDemandStream stream) {
		// TODO Auto-generated method stub
	}

	public void onRecordStreamStart(IStream stream) {
		// TODO Auto-generated method stub
	}

	public void onRecordStreamStop(IStream stream) {
		// TODO Auto-generated method stub
	}

	public void onStreamPublishStart(IStream stream) {
		// TODO Auto-generated method stub
	}

	public void onStreamPublishStop(IStream stream) {
		// TODO Auto-generated method stub
	}

	public ICall postProcessServiceCall(ICall call) {
		return call;
	}

	public ICall preProcessServiceCall(ICall call) {
		return call;
	}
	
}