package org.red5.server.net.rtmp.event;

import org.apache.mina.common.ByteBuffer;
import org.red5.server.api.event.IEvent.Type;
import org.red5.server.net.rtmp.message.Header;
import org.red5.server.stream.IStreamData;


public class AudioData extends BaseEvent implements IHeaderAware, IStreamData {

	protected byte EVENT_DATATYPE = TYPE_AUDIO_DATA;
	
	protected ByteBuffer data = null;
	
	public AudioData(ByteBuffer data){
		super(Type.STREAM_DATA);
		this.data = data;
	}

	public void setHeader(Header header) {
		
	}
	
	public ByteBuffer getData(){
		return data;
	}
	
	public String toString(){
		return "Audio  ts: "+getTimestamp();
	}
	
}