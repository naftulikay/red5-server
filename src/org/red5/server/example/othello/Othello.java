package org.red5.server.example.othello;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.ISharedObject;
import org.red5.server.api.impl.EasyAppAdapter;

public class Othello extends EasyAppAdapter {
	
	protected static Log log = LogFactory.getLog(Othello.class.getName());
	
	protected String sharedObjectName = "game";
	protected String appPath = "/othello";
	protected String gamePrefix = "/game";
	
	//	 optionally set properties by dependancy injection
	
	public void setSharedObjectName(String sharedObjectName) {
		this.sharedObjectName = sharedObjectName;
	}
	
	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}

	public void setGamePrefix(String gamePrefix) {
		this.gamePrefix = gamePrefix;
	}

	// initialization method
	
	public void init(){
		// do any other handler init tasks here.
		// this will be called by container at initialization
		// configure in spring by setting init-method="init
		setAttributeStoreType(STORE_SCOPE);
	}
	
	// authorization methods, I have just added as examples
	
	public boolean canCreateScope(String contextPath) {
		// this method allows us to restrict the scopes which can be create
		// we will allow the default /othello or /othello/game$$ paths
		if(contextPath.equals(appPath)) return true;
		else return contextPath.startsWith(appPath + gamePrefix);
	}

	public boolean canConnect(IConnection conn, IScope scope) {
		// this method allows us to filter connection
		// set the limit for game rooms to 10 clients
		return scope.getClients().size() < 10;
	}
	
	public void onCreateScope(IScope scope) {
		// this method is called anytime a new scope is created
		// scopes may be /othello/game1, /othello/game2, etc
		// lets assume each scope will have one shared object
		log.info("Creating initial shared object: "+sharedObjectName);
		scope.createSharedObject(sharedObjectName, false);
	}

	public void onConnect(IConnection conn) {
		String msg = "New player connected: "+conn.getClient().getId();
		sendMessage(conn.getScope(), msg);
		// the client will be expected to connect to the so.
	}
	
	public void onDisconnect(IConnection conn) {
		String msg = "Player disconnected: "+conn.getClient().getId();
		sendMessage(conn.getScope(), msg);
	}

	protected void sendMessage(IScope scope, String msg){
		ISharedObject so = scope.getSharedObject(sharedObjectName);
		LinkedList args = new LinkedList();
		args.set(0, msg);
		so.sendMessage("showMessage",args);
		//	having to create a list is a little clunky.. 
		// var args in java 5 could help here 
		// eg.. so.send("showMessage", msg, more, opt, args);
		log.info("showMessage: "+ msg);
	}
	
}