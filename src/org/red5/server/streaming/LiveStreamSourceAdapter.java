package org.red5.server.streaming;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.messaging.IPipe;
import org.red5.server.messaging.IPipeConnectionListener;
import org.red5.server.messaging.IProvider;
import org.red5.server.messaging.PipeConnectionEvent;
import org.red5.server.net.rtmp.Channel;
import org.red5.server.net.rtmp.message.AudioData;
import org.red5.server.net.rtmp.message.Invoke;
import org.red5.server.net.rtmp.message.Message;
import org.red5.server.net.rtmp.message.Status;
import org.red5.server.net.rtmp.message.StreamBytesRead;
import org.red5.server.net.rtmp.message.VideoData;
import org.red5.server.service.Call;
import org.red5.server.stream.Stream;

public class LiveStreamSourceAdapter implements IProvider, IPipeConnectionListener {
	private static final Log log = LogFactory.getLog(LiveStreamSourceAdapter.class);
	
	private IPipe pipe;
	private Stream stream;
	private int audioTS = 0;
	private int videoTS = 0;
	private int dataTS = 0;
	private int bytesReadPacketCount = 0;
	private int bytesReadInterval = 125000;
	private int bytesRead = 0;
	
	public LiveStreamSourceAdapter(Stream stream) {
		this.stream = stream;
	}
	
	public void onPipeConnectionEvent(PipeConnectionEvent event) {
		switch (event.getType()) {
		case PipeConnectionEvent.PROVIDER_CONNECT_PUSH:
			if (this.pipe == null && event.getProvider() == this) {
				this.pipe = (IPipe) event.getSource();
			} else if (event.getProvider() == this) {
				log.warn("This LiveStreamSource already connected!");
			}
			break;
		case PipeConnectionEvent.PROVIDER_DISCONNECT:
			if (this.pipe == event.getSource() && event.getProvider() == this) {
				this.pipe = null;
			}
			break;
		default:
			break;
		}
	}
	
	public void start() {
		Status publish = new Status(Status.NS_PUBLISH_START);
		publish.setClientid(stream.getStreamId());
		publish.setDetails(stream.getName());
		Channel data = stream.getDownstream().getData();
		data.sendStatus(publish);
	}

	private void unpublishNotify() {
		if (this.pipe != null) {
			Status unpublish = new Status(Status.NS_PLAY_UNPUBLISHNOTIFY);
			unpublish.setClientid(stream.getStreamId());
			unpublish.setDetails(stream.getName());
			Call call = new Call(null,"onStatus",new Object[]{unpublish});
			Invoke invoke = new Invoke();
			invoke.setInvokeId(stream.getStreamId());
			invoke.setCall(call);
			RTMPMessage rtmpMsg = new RTMPMessage();
			rtmpMsg.setBody(invoke);
			this.pipe.pushMessage(rtmpMsg);
		}
	}
	
	public void close() {
		// make sure we stop the publishing
		unpublishNotify();
		if (this.pipe != null) this.pipe.unsubscribe(this);
		stream.getDownstream().close();
	}
	
	public void onIncomingMessage(Message message) {
		if (this.pipe != null) {
			bytesRead += message.getData().limit();
			if (bytesReadPacketCount < Math.floor(bytesRead / bytesReadInterval)){
				bytesReadPacketCount++;
				StreamBytesRead streamBytesRead = new StreamBytesRead();
				streamBytesRead.setBytesRead(bytesRead);
				log.debug(streamBytesRead);
				stream.getConnection().getChannel((byte)2).write(streamBytesRead);
			}
			
			if (message instanceof VideoData) {
				if (message.isRelativeTimer()) {
					videoTS += message.getTimestamp();
				} else {
					videoTS = message.getTimestamp();
				}
				message.setTimestamp(videoTS);
			} else if (message instanceof AudioData) {
				if (message.isRelativeTimer()) {
					audioTS += message.getTimestamp();
				} else {
					audioTS = message.getTimestamp();
				}
				message.setTimestamp(audioTS);
			} else {
				if (message.isRelativeTimer()) {
					dataTS += message.getTimestamp();
				} else dataTS = message.getTimestamp();
				message.setTimestamp(dataTS);
			}
			RTMPMessage rtmpMsg = new RTMPMessage();
			rtmpMsg.setBody(message);
			this.pipe.pushMessage(rtmpMsg);
		}
	}
}
