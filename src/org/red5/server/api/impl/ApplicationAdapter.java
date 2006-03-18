package org.red5.server.api.impl;

import java.util.Map;
import java.util.Set;

import org.red5.server.api.IAttributeStore;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IApplication;
import org.red5.server.api.IScope;
import org.red5.server.api.IScopeAware;
import org.red5.server.api.ISharedObject;
import org.red5.server.api.ISharedObjectService;
import org.red5.server.api.Red5;
import org.red5.server.api.ScopeUtils;

public class ApplicationAdapter extends DefaultScopeAdapter 
	implements IAttributeStore, IApplication, IScopeAware {
	
	protected IScope scope;
	
	public void setScope(IScope scope) {
		this.scope = scope;	
	}

	public Object getAttribute(String name) {
		return scope.getAttribute(name);
	}

	public Set getAttributeNames() {
		return scope.getAttributeNames();
	}

	public boolean hasAttribute(String name) {
		return scope.hasAttribute(name);
	}

	public boolean removeAttribute(String name) {
		return scope.removeAttribute(name);
	}

	public void removeAttributes() {
		scope.removeAttributes();
	}

	public boolean setAttribute(String name, Object value) {
		return scope.setAttribute(name,value);
	}

	public void setAttributes(IAttributeStore values) {
		scope.setAttributes(values);
	}

	public void setAttributes(Map values) {
		scope.setAttributes(values);
	}
	
	public boolean connect(IConnection conn) {
		if(ScopeUtils.isApp(conn.getScope())) return appConnect(conn);
		else if(ScopeUtils.isRoom(conn.getScope())) return roomConnect(conn);
		else return false;
	}

	public boolean start(IScope scope) {
		if(ScopeUtils.isApp(scope)) return appStart(scope);
		else if(ScopeUtils.isRoom(scope)) return roomStart(scope);
		else return false;
	}

	public void disconnect(IConnection conn) {
		if(ScopeUtils.isApp(conn.getScope())) appDisconnect(conn);
		else if(ScopeUtils.isRoom(conn.getScope())) roomDisconnect(conn);
	}

	public void stop(IScope scope) {
		if(ScopeUtils.isApp(scope)) appStop(scope);
		else if(ScopeUtils.isRoom(scope)) roomStop(scope);
	}

	public boolean join(IClient client, IScope scope) {
		if(ScopeUtils.isApp(scope)) return appJoin(client, scope);
		else if(ScopeUtils.isRoom(scope)) return roomJoin(client, scope);
		else return false;
	}

	public void leave(IClient client, IScope scope) {
		if(ScopeUtils.isApp(scope)) appLeave(client, scope);
		else if(ScopeUtils.isRoom(scope)) roomLeave(client, scope);
	}

	public boolean appStart(IScope app){
		return true;
	}

	public void appStop(IScope app){
		// do nothing
	}
	
	public boolean roomStart(IScope room){
		return true;
	}
	
	public void roomStop(IScope room){
		//	do nothing
	}
	
	public boolean appConnect(IConnection conn){
		return true;
	}
	
	public boolean roomConnect(IConnection conn){
		return true;
	}
	
	public void appDisconnect(IConnection conn){
		// do nothing
	}
	
	public void roomDisconnect(IConnection conn){
		// do nothing
	}
	
	public boolean appJoin(IClient client, IScope app){
		return true;
	}
	
	public void appLeave(IClient client, IScope app){
		
	}
	
	public boolean roomJoin(IClient client, IScope room){
		return true;
	}
	
	public void roomLeave(IClient client, IScope room){
		
	}
	
}