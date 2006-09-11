/*
 * application.js - a translation into JavaScript of the olfa demo application, a red5 example.
 *
 * @author Paul Gregoire
 */

var javaNames = JavaImporter();

javaNames.importPackage(Packages.org.red5.server.adapter);
javaNames.importPackage(Packages.org.red5.server.api);
javaNames.importPackage(Packages.org.red5.server.api.stream);
javaNames.importPackage(Packages.org.red5.server.api.stream.support);

//namespace
Red5 = {};

/**
 * A function used to extend one class with another
 * 
 * @param {Object} subClass
 * 		The inheriting class, or subclass
 * @param {Object} baseClass
 * 		The class from which to inherit
 */
Red5.extend = function(subClass, baseClass) {
   function inheritance() {}
   inheritance.prototype = baseClass.prototype;
   subClass.prototype = new inheritance();
   subClass.prototype.constructor = subClass;
   subClass.baseConstructor = baseClass;
   //subClass.superClass = baseClass.prototype; //use if base is Javascript
   subClass.superClass = new baseClass(); //use if base is Java
};

function Application() {
	var appScope;
	var serverStream;
	//subclass ApplicationAdapter
	Red5.extend(this, Packages.org.red5.server.adapter.ApplicationAdapter);
}	

//public boolean appConnect(IConnection conn, Object[] params) {
Application.prototype.appConnect = function (conn, params) {
	print('Javascript appConnect');
	this.superClass.measureBandwidth(conn);
	with(javaNames) {
		if (conn == typeof(IStreamCapableConnection)) {
			var streamConn = conn;
			var sbc = new SimpleBandwidthConfigure();
			sbc.setMaxBurst(8388608);
			sbc.setBurst(8388608);
			sbc.setOverallBandwidth(2097152);
			streamConn.setBandwidthConfigure(sbc);
		}
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
	
Application.prototype.toString = function () {
    return 'Javascript toString ' + this.superClass.toString();
};

print('Javascript application BEGIN');
	
try {
	var ap = new Application();
	///
	//for (prop in ap) {
	//	print(prop + ' type: ' + typeof(prop));
	//}
	///
	print('Script - To string: ' + ap.toString());
	print('Script - App: ' + ap.appStart(null));
	ap.appConnect(null, null);
	ap.appDisconnect(null);
	ap.measureBandwidth();
	ap.getStreamLength('temp.flv');
} catch(e) {
	print('Script - Exception: ' + e);
}

print('Javascript application END');
