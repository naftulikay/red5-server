package org.red5.server.streaming;

import org.red5.server.messaging.IMessage;
import org.red5.server.messaging.IPipe;
import org.red5.server.messaging.IPipeConnectionListener;
import org.red5.server.messaging.IPushableConsumer;
import org.red5.server.messaging.PipeConnectionEvent;
import org.red5.server.stream.FileStreamSink;

public class FileStreamSinkAdapter implements IPushableConsumer,
		IPipeConnectionListener {
	private IPipe pipe;
	private FileStreamSink sink;
	
	public FileStreamSinkAdapter(FileStreamSink sink) {
		this.sink = sink;
	}

	public void pushMessage(IPipe pipe, IMessage message) {
		if (message instanceof RTMPMessage) {
			RTMPMessage rtmpMsg = (RTMPMessage) message;
			if (sink.canAccept()) {
				sink.enqueue(rtmpMsg.getBody());
			}
		}
	}

	public void onPipeConnectionEvent(PipeConnectionEvent event) {
		switch (event.getType()) {
		case PipeConnectionEvent.CONSUMER_CONNECT_PUSH:
			if (event.getConsumer() == this && this.pipe == null) {
				this.pipe = (IPipe) event.getSource();
			}
			break;
		case PipeConnectionEvent.CONSUMER_DISCONNECT:
			if (event.getConsumer() == this && this.pipe == event.getSource()) {
				this.pipe = null;
			}
			break;
		case PipeConnectionEvent.PROVIDER_DISCONNECT:
			if (this.pipe == event.getSource()) {
				// close the file sink on provider disconnect
				// and unsubscribe itself
				close();
				this.pipe.unsubscribe(this);
			}
			break;
		default:
			break;
		}
	}

	public void close() {
		sink.close();
	}
}
