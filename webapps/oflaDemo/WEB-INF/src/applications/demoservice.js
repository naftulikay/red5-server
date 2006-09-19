/*
 * demoservice.js - a translation into JavaScript of the olfa demo DemoService class, a red5 example.
 *
 * @see http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference
 * @author Paul Gregoire
 */

var javaNames = JavaImporter();

javaNames.importPackage(Packages.org.red5.server.api);
javaNames.importPackage(Packages.org.springframework.core.io);
javaNames.importPackage(Packages.org.apache.commons.logging);

importClass(java.io.File);
importClass(java.io.FileInputStream);
importClass(java.util.HashMap);
importClass(Packages.org.springframework.core.io.Resource);
importClass(Packages.org.red5.server.api.Red5);

log.debug('DemoService init');

function DemoService() {
	
	this.getListOfAvailableFLVs = function() {
		log.debug('getListOfAvailableFLVs');
		log.debug('Con local: ' + Red5.getConnectionLocal());
		var scope = Red5.getConnectionLocal().getScope();
		log.debug('Scope: ' + scope);
		var filesMap = new HashMap();
		var fileInfo;
		try {
			print('Getting the FLV files');
			var flvs = scope.getResources("streams/*.flv"); //Resource[]
			log.debug('Flvs: ' + flvs);
			log.debug('Number of flvs: ' + flvs.length);
			for (var i=0;i<flvs.length;i++) {
				var file = flvs[i];
				log.debug('file: ' + file);
				log.debug('file type: ' + (file == typeof(java.io.File)));
				log.debug('file type: ' + typeof(file));
				log.debug('file path: ' + file.path);
				log.debug('file path: ' + file.URL);
				//var fso = new File(new FileInputStream('../../' + file.getInputStream()));
				//var fso = new File(file.path);
				//var fso = new File('../../..' + file.path);
				var fso = new File(file.URL);
				var flvName = fso.getName();
				log.debug('flvName: ' + flvName);
				log.debug('exist: ' + fso.exists());
				var flvBytes = fso.getLength();
				log.debug('flvBytes: ' + flvBytes);
				var lastModified = this.formatDate(new java.util.Date(fso.getLastModified()));
	
				print('FLV Name: ' + flvName);
				print('Last modified date: ' + lastModified);
				print('Size: ' + flvBytes);
				print('-------');
				
				fileInfo = new HashMap();
				fileInfo.put("name", flvName);
				fileInfo.put("lastModified", lastModified);
				fileInfo.put("size", flvBytes);
				filesMap.put(flvName, fileInfo);
			}
		} catch (e) {
			log.debug('Error in getListOfAvailableFLVs: ' + e);
			//print('Exception: ' + e);
		}
		return filesMap;
	};

	this.formatDate = function(date) {
		log.debug('formatDate');
		return date.getDate() + '/' + date.getMonth() + '/' + date.getFullYear() + ' ' + date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds();
	};
	
}
