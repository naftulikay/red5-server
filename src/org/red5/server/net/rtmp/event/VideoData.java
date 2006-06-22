package org.red5.server.net.rtmp.event;

import org.apache.mina.common.ByteBuffer;
import org.red5.server.stream.IStreamData;
import org.red5.server.net.rtmp.message.Header;

public class VideoData extends BaseEvent implements IHeaderAware, IStreamData {

	protected byte EVENT_DATATYPE = TYPE_VIDEO_DATA;
	protected ByteBuffer data = null;
	
	public VideoData(ByteBuffer data){
		super(Type.STREAM_DATA);
		this.data = data;
	}

	public void setHeader(Header header){
		
	}
	
	public ByteBuffer getData(){
		return data;
	}
	
	public String toString(){
		return "Audio  ts: "+getTimestamp();
	}
	
}