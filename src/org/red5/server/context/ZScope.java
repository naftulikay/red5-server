package org.red5.server.context;

import org.red5.server.net.rtmp.Channel;
import org.red5.server.net.rtmp.status.StatusObjectService;
import org.red5.server.stream.Stream;

public class ZScope {

	private static ThreadLocal channelLocal = new ThreadLocal();
	private static ThreadLocal statusObjectServiceLocal = new ThreadLocal();
	private static ThreadLocal streamLocal = new ThreadLocal();
	
	public static Channel getChannel(){
		return (Channel) channelLocal.get();
	}

	public static void setChannel(Channel channel) {
		channelLocal.set(channel);
	}
	
	public static StatusObjectService getStatusObjectService(){
		return (StatusObjectService) statusObjectServiceLocal.get();
	}

	public static void setStatusObjectService(StatusObjectService sos) {
		statusObjectServiceLocal.set(sos);
	}
	
	public static void setStream(Stream stream){
		streamLocal.set(stream);
	}
	
	public static Stream getStream(){
		return (Stream) streamLocal.get();
	}
	
}
