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
	this.appScope;
	this.serverStream;
	this.base = ApplicationAdapter;
	this.base();
	
	for (property in this.__proto__) {
		print('Application\n');
		try {
			print('>>>' + property);
		} catch(e) {
			e.rhinoException.printStackTrace();
		}	
	}	
	for (property in this.__proto__.__proto__) {
		print('\nApplicationAdapter\n');
		try {
			print('>>>' + property);
		} catch(e) {
			e.rhinoException.printStackTrace();
		}	
	}	

}	

Application.prototype.appStart = function(app) {
	print('Javascript appStart');
	this.appScope = app;
	return true;
};

Application.prototype.appConnect = function(conn, params) {
	print('Javascript appConnect');
	this.measureBandwidth(conn);
	if (conn == typeof(IStreamCapableConnection)) {
		var streamConn = conn;
		var sbc = new SimpleBandwidthConfigure();
		sbc.setMaxBurst(8388608);
		sbc.setBurst(8388608);
		sbc.setOverallBandwidth(2097152);
		streamConn.setBandwidthConfigure(sbc);
	}
	return this.__proto__.__proto__.appConnect(conn, params);
};

Application.prototype.appDisconnect = function(conn) {
	print('Javascript appDisconnect');
	if (this.appScope == conn.getScope() && this.serverStream)  {
		this.serverStream.close();
	}
	return this.__proto__.__proto__.appDisconnect(conn);
};

//set superclass
Application.prototype = new ApplicationAdapter;

//create an instance
instance = new Application();

Function.prototype.printStackTrace=function(exp) {    
    if (exp == undefined) {
        try {
            exp.toString();
        } catch (e) {
            exp = e;
        }
    }
    // note that user could have caught some other
    // "exception"- may be even a string or number -
    // and passed the same as argument. Also, check for
    // rhinoException property before using it
    if (exp instanceof Error && 
        exp.rhinoException != undefined) {
        exp.rhinoException.printStackTrace();
    }
};




