package org.red5.server.net.rtmp;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.impl.AbstractConnection;
import org.red5.server.context.AppContext;
import org.red5.server.net.rtmp.message.Ping;
import org.red5.server.stream.DownStreamSink;
import org.red5.server.stream.Stream;

public abstract class RTMPConnection extends AbstractConnection {

	protected static Log log =
        LogFactory.getLog(RTMPConnection.class.getName());

	private final static int MAX_STREAMS = 12;
	
	//private Context context;
	private Channel[] channels = new Channel[64];
	private Stream[] streams = new Stream[MAX_STREAMS];
	private AppContext appCtx = null;
	private Map params = null;

	public RTMPConnection() {
		// We start with an anonymous connection without a scope.
		// These parameters will be set during the call of "connect" later.
		super(null, "");
	}

	public void setClient(IClient client) {
		this.client = client;
	}
	
	public AppContext getAppContext() {
		return appCtx;
	}
	
	public void setAppContext(AppContext appCtx) {
		this.appCtx = appCtx;
	}

	public String getType() {
		return IConnection.PERSISTENT;
	}
	
	public int getNextAvailableChannelId(){
		int result = -1;
		for(byte i=4; i<channels.length; i++){
			if(!isChannelUsed(i)){
				result = i;
				break;
			}
		}
		return result;
	}
	
	public boolean isChannelUsed(byte channelId){
		return (channels[channelId] != null);
	}

	public Channel getChannel(byte channelId){
		if(!isChannelUsed(channelId)) 
			channels[channelId] = new Channel(this, channelId);
		return channels[channelId];
	}
	
	public void closeChannel(byte channelId){
		channels[channelId] = null;
	}

	public void setParameters(Map params){
		this.params = params;
	}
	
	public Map getParams() {
		return this.params;
	}
	
	/* Returns a stream for the next available stream id or null if all slots are in use. */
	public Stream createNewStream() {
		synchronized (streams) {
			for (int i=0; i<streams.length; i++)
				if (streams[i] == null) {
					Stream stream = createStream(i);
					streams[i] = stream;
					return stream;
				}
		}
		
		return null;
	}
	
	public Stream getStreamById(int id){
		if (id <= 0 || id > MAX_STREAMS-1)
			return null;
		
		return streams[id-1];
	}
	
	public Stream getStreamByChannelId(byte channelId){
		if (channelId < 4)
			return null;
		
		//log.debug("Channel id: "+channelId);
		int streamId = (int) Math.floor((channelId-4)/5);
		//log.debug("Stream: "+streamId);
		return streams[streamId];
	}
	
	public void close(){
		synchronized (streams) {
			for(int i=0; i<streams.length; i++){
				Stream stream = streams[i];
				if(stream != null) {
					stream.close();
					streams[i] = null;
				}
			}
		}
	}
	
	protected Stream createStream(int streamId){
		byte channelId = (byte) (4 + (streamId * 5));
		Stream stream = new Stream(this);
		stream.setStreamId(streamId+1);
		final Channel data = getChannel(channelId++);
		final Channel video = getChannel(channelId++);
		final Channel audio = getChannel(channelId++);
		//final Channel unknown = getChannel(channelId++);
		//final Channel ctrl = getChannel(channelId++);
		final DownStreamSink down = new DownStreamSink(video,audio,data);
		stream.setDownstream(down);
		return stream;
	}
	
	public void deleteStreamById(int streamId) {
		if (streamId >= 0 && streamId < MAX_STREAMS-1)
			streams[streamId-1] = null;
	}
	
	public void ping(Ping ping){
		getChannel((byte)2).write(ping);
	}
	
}
