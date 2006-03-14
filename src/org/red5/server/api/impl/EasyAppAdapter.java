package org.red5.server.api.impl;

import java.util.Map;
import java.util.Set;

import org.red5.server.api.IAttributeStore;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.Red5;
import org.red5.server.api.ScopeUtils;

public abstract class EasyAppAdapter extends DefaultScopeAdapter implements IAttributeStore {
	
	public static final String STORE_CONNECTION = "connection";
	public static final String STORE_SCOPE = "scope";
	public static final String STORE_CLIENT = "client";
	
	protected String attributeStoreType = STORE_SCOPE;
	
	public void setAttributeStoreType(String attributeStoreType) {
		this.attributeStoreType = attributeStoreType;
	}

	protected IAttributeStore getAttributeStore(){
		final IConnection conn = Red5.getConnectionLocal();
		if(conn == null) throw new RuntimeException("Connection local not set, no access to attribute store");
		if(attributeStoreType.equals(STORE_SCOPE))
			return conn.getScope();
		else if(attributeStoreType.equals(STORE_CLIENT))
			return conn.getClient();
		else if(attributeStoreType.equals(STORE_CONNECTION))
			return conn.getScope();
		else throw new RuntimeException("Invalid attribute store type: "+attributeStoreType);
	}
	
	public Object getAttribute(String name) {
		return getAttributeStore().getAttribute(name);
	}

	public Set getAttributeNames() {
		return getAttributeStore().getAttributeNames();
	}

	public boolean hasAttribute(String name) {
		return getAttributeStore().hasAttribute(name);
	}

	public boolean removeAttribute(String name) {
		return getAttributeStore().removeAttribute(name);
	}

	public void removeAttributes() {
		getAttributeStore().removeAttributes();
	}

	public boolean setAttribute(String name, Object value) {
		return getAttributeStore().setAttribute(name,value);
	}

	public void setAttributes(IAttributeStore values) {
		getAttributeStore().setAttributes(values);
	}

	public void setAttributes(Map values) {
		getAttributeStore().setAttributes(values);
	}
	
	public void onConnect(IConnection conn) {
		if(ScopeUtils.isApplication(conn.getScope())) ezAppConnect(conn);
		else if(ScopeUtils.isInstance(conn.getScope())) ezInstanceConnect(conn);
	}

	public void onCreateScope(IScope scope) {
		if(ScopeUtils.isApplication(scope)) ezAppStart(scope);
		else if(ScopeUtils.isInstance(scope)) ezInstanceStart(scope);
	}

	public void onDisconnect(IConnection conn) {
		if(ScopeUtils.isApplication(conn.getScope())) ezAppDisconnect(conn);
		else if(ScopeUtils.isInstance(conn.getScope())) ezInstanceDisconnect(conn);
	}

	public void onDisposeScope(IScope scope) {
		if(ScopeUtils.isApplication(scope)) ezAppStop(scope);
		else if(ScopeUtils.isInstance(scope)) ezInstanceStop(scope);
	}

	public void ezAppStart(IScope app){
		// do nothing
	}

	public void ezAppStop(IScope app){
		// do nothing
	}
	
	public void ezInstanceStart(IScope instance){
		//	do nothing
	}
	
	public void ezInstanceStop(IScope instance){
		//	do nothing
	}
	
	public void ezAppConnect(IConnection conn){
		// do nothing
	}
	
	public void ezInstanceConnect(IConnection conn){
		// do nothing
	}
	
	public void ezAppDisconnect(IConnection conn){
		// do nothing
	}
	
	public void ezInstanceDisconnect(IConnection conn){
		// do nothing
	}
	
}