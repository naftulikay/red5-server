package org.red5.server;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright (c) 2006-2007 by respective authors (see below). All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 2.1 of the License, or (at your option) any later 
 * version. 
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this library; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.red5.server.Scope.PrefixFilteringStringIterator;
import org.red5.server.api.IBasicScope;
import org.red5.server.api.IGlobalScope;
import org.red5.server.api.IScope;
import org.red5.server.api.IServer;
import org.red5.server.api.persistence.IPersistenceStore;
import org.red5.server.api.persistence.PersistenceUtils;

/**
 * Global scope is a top level scope. Server instance is meant to be injected with Spring before
 * initialization (otherwise NullPointerException is thrown).
 *
 * @see  org.red5.server.api.IGlobalScope
 * @see  org.red5.server.api.IScope
 */
public class GlobalScope extends Scope implements IGlobalScope {
    // Red5 Server instance
	protected IServer server;

    /**
     *
     * @param persistenceClass          Persistent class name
     * @throws Exception                Exception
     */
    @Override
	public void setPersistenceClass(String persistenceClass) throws Exception {
		this.persistenceClass = persistenceClass;
		// We'll have to wait for creation of the store object
		// until all classes have been initialized.
	}

    /**
     * Get persistence store for scope
     *
     * @return            Persistence store
     */
    @Override
	public IPersistenceStore getStore() {
		if (store != null) {
			return store;
		}

		try {
			store = PersistenceUtils.getPersistenceStore(this,
					this.persistenceClass);
		} catch (Exception error) {
			log.error("Could not create persistence store.", error);
			store = null;
		}
		return store;
	}

	/**
     * Setter for server
     *
     * @param server Server
     */
    public void setServer(IServer server) {
		this.server = server;
	}

    /** {@inheritDoc} */
	public IServer getServer() {
		return server;
	}

    /**
     *  Register global scope in server instance, then call initialization
     */
    public void register() {
		server.registerGlobal(this);
		init();
	}

    private final Map<String, IBasicScope> myChildren = new HashMap<String, IBasicScope>();
    
    @Override
	public IBasicScope getBasicScope(String type, String name) {
    	synchronized (myChildren) {
    		return myChildren.get(type + SEPARATOR + name);
    	}
	}

	@Override
	public Iterator<String> getBasicScopeNames(String type) {
		synchronized (myChildren) {
			if (type == null) {
				return myChildren.keySet().iterator();
			} else {
				return new PrefixFilteringStringIterator(myChildren.keySet()
						.iterator(), type + SEPARATOR);
			}
		}
	}

	@Override
	public IScope getScope(String name) {
		synchronized (myChildren) {
			return (IScope) myChildren.get(TYPE + SEPARATOR + name);
		}
	}

	@Override
	public Iterator<String> getScopeNames() {
		synchronized (myChildren) {
			return new PrefixFilteringStringIterator(myChildren.keySet().iterator(),
			"scope");
		}
	}

	@Override
	public boolean hasChildScope(String type, String name) {
		synchronized (myChildren) {
			return myChildren.containsKey(type + SEPARATOR + name);
		}
	}

	@Override
	public boolean hasChildScope(String name) {
		synchronized (myChildren) {
			if (log.isDebugEnabled()) {
				log.debug("Has child scope? " + name + " in " + this);
			}
			return myChildren.containsKey(TYPE + SEPARATOR + name);
		}
	}

	@Override
	public Iterator<IBasicScope> iterator() {
		synchronized (myChildren) {
			return myChildren.values().iterator();
		}
	}

	@Override
	public void removeChildScope(IBasicScope scope) {
		synchronized (myChildren) {
			myChildren.remove(scope.getType() + SEPARATOR + scope.getName());
			scope.setStore(null);
		}
	}

	public boolean addChildScope(IBasicScope scope) {
    	synchronized (myChildren) {
    		String key = scope.getType() + SEPARATOR + scope.getName();
    		if (! myChildren.containsKey(key)) {
    			super.addChildScope(scope);
    			myChildren.put(key, scope);
    		} else {
    			// it's the scope created from other nodes
    			// add transient fields back
    			IBasicScope basicScope0 = myChildren.get(key);
    			if (basicScope0 instanceof Scope &&
    					scope instanceof Scope) {
    				Scope scope0 = (Scope) basicScope0;
    				Scope scope1 = (Scope) scope;
    				scope0.setHandler(scope1.getHandler());
    				scope0.setContext(scope1.getContext());
    			}
    			if (basicScope0 instanceof WebScope &&
    					scope instanceof WebScope) {
    				WebScope webScope0 = (WebScope) basicScope0;
    				WebScope webScope1 = (WebScope) scope;
    				webScope0.setServer(webScope1.getServer());
    				webScope0.setGlobalScope(this);
    				webScope0.setParent(this);
    				webScope0.setServletContext(webScope1.getServletContext());
    			}
    		}
    	}
    	return true;
    }
    
}
