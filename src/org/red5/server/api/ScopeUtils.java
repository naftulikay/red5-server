package org.red5.server.api;

/**
 * Collection of utils for working with scopes
 */
public class ScopeUtils {
	
	private static final int GLOBAL = 0x00;
	private static final int HOST = 0x01;
	private static final int APPLICATION = 0x02;
	private static final int INSTANCE = 0x04;
	
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
			current = current.getChildScope(part);
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
	
	public static boolean isAncestor(IScope from, IScope ancestor){
		IScope current = from;
		while(current.hasParent()){
			current = current.getParent();
			if(current.equals(ancestor)) return true;
		}
		return false;
	}

	public static boolean isGlobal(IScope scope){
		return scope.getDepth() == GLOBAL;	
	}
	
	public static boolean isHost(IScope scope){
		return scope.getDepth() == HOST;
	}
	
	public static boolean isApplication(IScope scope){
		return scope.getDepth() == APPLICATION;
	}

	public static boolean isInstance(IScope scope){
		return scope.getDepth() == INSTANCE;
	}
	
}
