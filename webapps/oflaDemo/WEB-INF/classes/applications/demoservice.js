/*
 * demoservice.js - a translation into JavaScript of the olfa demo DemoService class, a red5 example.
 *
 * @see http://developer.mozilla.org/en/docs/Core_JavaScript_1.5_Reference
 * @author Paul Gregoire
 */

var javaNames = JavaImporter();

javaNames.importPackage(Packages.org.red5.server.api);
javaNames.importPackage(Packages.org.springframework.core.io);

function DemoService() {

	function getListOfAvailableFLVs() {
		var scope = Red5.getConnectionLocal().getScope();
		var filesMap = new HashMap();
		var fileInfo;
		try {
			print('Getting the FLV files');
			var flvs = scope.getResources("streams/*.flv"); //Resource[]
			for (var flv in flvs) {
				var file = flv.getFile();
				var lastModified = this.formatDate(new Date(file.lastModified()));
				var flvName = file.getName();
				var flvBytes = file.length();
	
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
			print('Getting the MP3 files');
			var mp3s = scope.getResources("streams/*.mp3");
			for (var mp3 in mp3s) {
				file = mp3.getFile();
				lastModified = this.formatDate(new Date(file.lastModified()));
				var mpName = file.getName();
				var mpBytes = file.length();
	
				print('MP3 Name: ' + mpName);
				print('Last modified date: ' + lastModified);
				print('Size: ' + mpBytes);
				print('-------');
				
				fileInfo = new HashMap();
				fileInfo.put("name", mpName);
				fileInfo.put("lastModified", lastModified);
				fileInfo.put("size", mpBytes);
				filesMap.put(mpName, fileInfo);
			}
		} catch (e) {
			print('Exception: ' + e);
		}
		return filesMap;
	}

	function formatDate(date) {
		return date.getDate() + '/' + date.getMonth() + '/' + date.getFullYear() + ' ' + date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds();
	}
	
}