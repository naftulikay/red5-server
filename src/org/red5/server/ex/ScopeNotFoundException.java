package org.red5.server.ex;

import org.red5.server.api.IScope;

public class ScopeNotFoundException extends RuntimeException {
	
	public ScopeNotFoundException(IScope scope, String childName){
		super("Scope not found: "+childName+" in "+scope);
	}

}
