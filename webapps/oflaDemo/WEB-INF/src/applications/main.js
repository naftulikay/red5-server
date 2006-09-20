/*
 * main.js - a translation into JavaScript of the olfa demo Application class, a red5 example.
 *
 * @author Paul Gregoire
 */

importPackage(Packages.org.red5.server.adapter);
importPackage(Packages.org.red5.server.api);
importPackage(Packages.org.red5.server.api.stream);
importPackage(Packages.org.red5.server.api.stream.support);
importPackage(Packages.org.springframework.core.io);
importPackage(Packages.org.apache.commons.logging);

importClass(Packages.org.springframework.core.io.Resource);
importClass(Packages.org.red5.server.api.Red5);
importClass(Packages.org.red5.server.api.IScopeHandler);
importClass(Packages.org.red5.server.adapter.ApplicationAdapter);
importClass(Packages.org.red5.server.api.stream.IStreamCapableConnection);
importClass(Packages.org.red5.server.api.stream.support.SimpleBandwidthConfigure);

function Application() {
	var appScope;
	var serverStream;
	Application.extend(this, ApplicationAdapter);
	//r5.extend(this, Packages.org.red5.server.adapter.ApplicationAdapter);
}	

//public boolean appConnect(IConnection conn, Object[] params) {
Application.prototype.appConnect = function (conn, params) {
	print('Javascript appConnect');
	this.superClass.measureBandwidth(conn);
	if (conn == typeof(IStreamCapableConnection)) {
		var streamConn = conn;
		var sbc = new SimpleBandwidthConfigure();
		sbc.setMaxBurst(8388608);
		sbc.setBurst(8388608);
		sbc.setOverallBandwidth(2097152);
		streamConn.setBandwidthConfigure(sbc);
	}
	return this.superClass.appConnect(conn, params);
};

//public boolean appStart(IScope app) 
Application.prototype.appStart = function (app) {
	print('Javascript appStart');
	this.appScope = app;
	return true;
};
	
//public void appDisconnect(IConnection conn) 
Application.prototype.appDisconnect = function (conn) {
	print('Javascript appDisconnect');
	if (this.appScope == conn.getScope() && this.serverStream)  {
		this.serverStream.close();
	}
	return this.superClass.appDisconnect(conn);
};

Function.prototype.extend=function(subClass, superClass){
   	function inheritance() {}
   	inheritance.prototype = superClass.prototype;	
	subClass.prototype=new inheritance();
	subClass.prototype.constructor=subClass;
	subClass.superClass=superClass;
	subClass.prototype.superClass=new superClass();
	//copy public static stuff
	for (property in superClass) {
		print('>>>' + property);
		if (!subClass[property]) {			
			try {
				subClass[property] = superClass[property];	
			} catch(e) {
				print('Extend error: ' + e);
			}				
		}
	}
	//copy instance stuff
	for (property in subClass.prototype.superClass) {
		print('>>>>>' + property);
		if (!subClass[property]) {
			try {
				subClass[property] = subClass.prototype.superClass[property];	
			} catch(e) {
				print('Extend error: ' + e);
			}
		}
	}
	
};




