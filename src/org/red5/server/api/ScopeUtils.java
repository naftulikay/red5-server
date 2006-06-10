package org.red5.server.api;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright � 2006 by respective authors (see below). All rights reserved.
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

import org.red5.server.api.persistence.IPersistable;
import org.springframework.context.ApplicationContext;

/**
 * Collection of utils for working with scopes
 */
public class ScopeUtils {
	
	private static final int GLOBAL = 0x00;
	private static final int APPLICATION = 0x01;
	private static final int ROOM = 0x02;
	private static final String SERVICE_CACHE_PREFIX = "__service_cache:";
	
	private static final String SLASH = "/";
	
	public static IScope resolveScope(IScope from, String path){
		IScope current = from;
		if(path.startsWith(SLASH)){
			current = ScopeUtils.findRoot(current);
			path = path.substring(1,path.length());
		}
		if(path.endsWith(SLASH)){
			path = path.substring(0,path.length()-1);
		}
		String[] parts = path.split(SLASH);
		for(int i=0; i<parts.length; i++){
			String part = parts[i];
			if(part.equals(".")) continue;
			if(part.equals("..")){
				if(!current.hasParent()) return null;
				current = current.getParent();
				continue;
			}
			if(!current.hasChildScope(part)) return null;
			current = current.getScope(part);
		}
		return current;
	}

	public static IScope findRoot(IScope from){
		IScope current = from;
		while(current.hasParent()){
			current = current.getParent();
		}
		return current;
	}
	
	public static IScope findApplication(IScope from){
		IScope current = from;
		while(current.hasParent() && current.getDepth() != APPLICATION){
			current = current.getParent();
		}
		return current;
	}
	
	public static boolean isAncestor(IBasicScope from, IBasicScope ancestor){
		IBasicScope current = from;
		while(current.hasParent()){
			current = current.getParent();
			if(current.equals(ancestor)) return true;
		}
		return false;
	}

	public static boolean isRoot(IBasicScope scope){
		return !scope.hasParent();	
	}

	public static boolean isGlobal(IBasicScope scope){
		return scope.getDepth() == GLOBAL;	
	}
	
	public static boolean isApp(IBasicScope scope){
		return scope.getDepth() == APPLICATION;
	}

	public static boolean isRoom(IBasicScope scope){
		return scope.getDepth() >= ROOM;
	}
	
	public static Object getScopeService(IScope scope, String name) {
		return getScopeService(scope, name, null);
	}
	
	public static Object getScopeService(IScope scope, String name, Class defaultClass) {
		if (scope == null)
			return null;
		
		if (scope.hasAttribute(IPersistable.TRANSIENT_PREFIX + SERVICE_CACHE_PREFIX + name))
			// Return cached service
			return scope.getAttribute(IPersistable.TRANSIENT_PREFIX + SERVICE_CACHE_PREFIX + name);
		
		final IContext context = scope.getContext();
		ApplicationContext appCtx = context.getApplicationContext();
		Object result;
		if (!appCtx.containsBean(name)) {
			if (defaultClass == null)
				return null;
			
			try {
				result = defaultClass.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else
			result = appCtx.getBean(name);
		
		// Cache service
		scope.setAttribute(IPersistable.TRANSIENT_PREFIX + SERVICE_CACHE_PREFIX + name, result);
		return result;
	}

}
