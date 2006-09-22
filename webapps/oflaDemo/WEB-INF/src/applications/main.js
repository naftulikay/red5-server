/*
 * main.js - a translation into JavaScript of the olfa demo Application class, a red5 example.
 *
 * @author Paul Gregoire
 */

//importPackage(Packages.org.red5.server.adapter);
importPackage(Packages.org.red5.server.api);
importPackage(Packages.org.red5.server.api.stream);
importPackage(Packages.org.red5.server.api.stream.support);
//importPackage(Packages.org.springframework.core.io);
importPackage(Packages.org.apache.commons.logging);

importClass(Packages.org.springframework.core.io.Resource);
importClass(Packages.org.red5.server.api.Red5);
importClass(Packages.org.red5.server.api.IScopeHandler);
//importClass(Packages.org.red5.server.adapter.ApplicationAdapter);
//importClass(Packages.org.red5.server.api.stream.IStreamCapableConnection);
//importClass(Packages.org.red5.server.api.stream.support.SimpleBandwidthConfigure);

//var ApplicationAdapter = new Packages.org.red5.server.adapter.ApplicationAdapter();
var IStreamCapableConnection = Packages.org.red5.server.api.stream.IStreamCapableConnection;

function Application() {
	this.appScope = null;
	this.serverStream = null;
	this.base = ApplicationAdapter;
    //this.__proto__.__proto__=Packages.org.red5.server.api.IScopeHandler;

     
/*
this.start = function() {
        print('Hello from start!!');
    };
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
*/

}	

Application.prototype.appStart = function(app) {
	print('Javascript appStart');
	this.appScope = app;
	return true;
};

Application.prototype.appConnect = function(conn, params) {
	print('Javascript appConnect');
	this.base.easureBandwidth(conn);
	if (conn == typeof(IStreamCapableConnection)) {
		var streamConn = conn;
		var sbc = new Packages.org.red5.server.api.stream.support.SimpleBandwidthConfigure();
		sbc.setMaxBurst(8388608);
		sbc.setBurst(8388608);
		sbc.setOverallBandwidth(2097152);
		streamConn.setBandwidthConfigure(sbc);
	}
	return this.base.appConnect(conn, params);
};

Application.prototype.appDisconnect = function(conn) {
	print('Javascript appDisconnect');
	if (this.appScope == conn.getScope() && this.serverStream)  {
		this.serverStream.close();
	}
	return this.base.appDisconnect(conn);
};

Application.prototype.helloFromAnExtendedMethod = function(conn) {
	print('Hello!!!!!');
};

//set superclass
Application.prototype = ApplicationAdapter;

//create an instance
var instance = new Application();
//instance.base.start = function() {
//    print('Hello from start!!');
//};

Function.prototype.printStackTrace=function(exp) {    
    if (exp === undefined) {
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
        exp.rhinoException !== undefined) {
        exp.rhinoException.printStackTrace();
    }
};




