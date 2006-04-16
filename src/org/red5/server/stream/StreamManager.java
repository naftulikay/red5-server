package org.red5.server.stream;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.io.flv.IFLV;
import org.red5.io.flv.IFLVService;
import org.red5.io.flv.IWriter;
import org.red5.server.messaging.IPipe;
import org.red5.server.messaging.IPipeConnectionListener;
import org.red5.server.messaging.InMemoryPushPushPipe;
import org.red5.server.messaging.PipeConnectionEvent;
import org.red5.server.net.rtmp.message.Status;
import org.red5.server.streaming.DownStreamAdapter;
import org.red5.server.streaming.FileStreamSinkAdapter;
import org.red5.server.streaming.LiveStreamSourceAdapter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

public class StreamManager implements ApplicationContextAware {

	protected static Log log =
        LogFactory.getLog(StreamManager.class.getName());
	
	private ApplicationContext appCtx = null;
	private String streamDir = "streams";
	private HashMap published = new HashMap();
	private IFLVService flvService;

	public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
		this.appCtx = appCtx;
	}
	
	public void setFlvService(IFLVService flvService) {
		this.flvService = flvService;
	}

	public void publishStream(Stream stream){
		
		// If we have a read mode stream, we shouldnt be publishing return
		if(stream.getMode().equals(Stream.MODE_READ)) return;
		
		PipeBookkeeper bookkeeper = null;
		synchronized (published) {
			bookkeeper = (PipeBookkeeper) published.get(stream.getName());
			if (bookkeeper == null) {
				IPipe pipe = new InMemoryPushPushPipe();
				bookkeeper = new PipeBookkeeper(stream.getName(), pipe);
				published.put(stream.getName(), bookkeeper);
			}
		}
		LiveStreamSourceAdapter liveStream = new LiveStreamSourceAdapter(stream);
		stream.setLiveStreamAdapter(liveStream);
		bookkeeper.getPipe().subscribe(liveStream);
		
		// If the mode is live, we dont need to do anything else
		if (stream.getMode().equals(Stream.MODE_LIVE)) return;
		
		// The mode must be record or append
		try {				
			Resource res = appCtx.getResource("streams/" + stream.getName()+".flv");
			if(stream.getMode().equals(Stream.MODE_RECORD) && res.exists()) 
				res.getFile().delete();
			if(!res.exists()) res = appCtx.getResource("streams/").createRelative(stream.getName()+".flv");
			if(!res.exists()) res.getFile().createNewFile(); 
			File file = res.getFile();
			IFLV flv = flvService.getFLV(file);
			IWriter writer = null; 
			if(stream.getMode().equals(Stream.MODE_RECORD)) 
				writer = flv.writer();
			else if(stream.getMode().equals(Stream.MODE_APPEND))
				writer = flv.append();
			FileStreamSinkAdapter fssa = new FileStreamSinkAdapter(
					new FileStreamSink(writer));
			bookkeeper.getPipe().subscribe(fssa);
		} catch (IOException e) {
			log.error("Error recording stream: "+stream, e);
		}
//		
//		MultiStreamSink multi = (MultiStreamSink) published.get(stream.getName());
//		if (multi == null)
//			// sink doesn't exist, create new
//			multi = new MultiStreamSink();
//			
//		stream.setUpstream(multi);
//		published.put(stream.getName(),multi);
//		
//		// If the mode is live, we dont need to do anything else
//		if(stream.getMode().equals(Stream.MODE_LIVE)) return;
//		
//		// The mode must be record or append
//		try {				
//			Resource res = appCtx.getResource("streams/" + stream.getName()+".flv");
//			if(stream.getMode().equals(Stream.MODE_RECORD) && res.exists()) 
//				res.getFile().delete();
//			if(!res.exists()) res = appCtx.getResource("streams/").createRelative(stream.getName()+".flv");
//			if(!res.exists()) res.getFile().createNewFile(); 
//			File file = res.getFile();
//			IFLV flv = flvService.getFLV(file);
//			IWriter writer = null; 
//			if(stream.getMode().equals(Stream.MODE_RECORD)) 
//				writer = flv.writer();
//			else if(stream.getMode().equals(Stream.MODE_APPEND))
//				writer = flv.append();
//			multi.connect(new FileStreamSink(writer));
//		} catch (IOException e) {
//			log.error("Error recording stream: "+stream, e);
//		}
	}
	
	public void deleteStream(Stream stream){
//		if (stream.getUpstream() != null && published.containsKey(stream.getName())) {
//			// Notify all clients that stream is no longer published
//			MultiStreamSink multi = (MultiStreamSink) published.get(stream.getName());
//			Status unpublish = new Status(Status.NS_PLAY_UNPUBLISHNOTIFY);
//			unpublish.setClientid(stream.getStreamId());
//			unpublish.setDetails(stream.getName());
//			Iterator it = multi.streams.iterator();
//			while (it.hasNext()) {
//				Stream s = (Stream) it.next();
//				s.getDownstream().getData().sendStatus(unpublish);
//			}
//			published.remove(stream.getName());
//		}
		stream.close();
	}
	
	public boolean isPublishedStream(String name){
		return published.containsKey(name);
	}
	
	public boolean isFileStream(String name) {
		if (this.isPublishedStream(name))
			// A stream cannot be published and file based at the same time
			return false;
		
		try {
			File file = appCtx.getResources("streams/" + name)[0].getFile();
			if (file.exists())
				return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	public void connectToPublishedStream(Stream stream){
		// connect to a live stream, wait if it has not been published yet.
		PipeBookkeeper bookkeeper = null;
		synchronized (published) {
			bookkeeper = (PipeBookkeeper) published.get(stream.getName());
			if (bookkeeper == null) {
				IPipe pipe = new InMemoryPushPushPipe();
				bookkeeper = new PipeBookkeeper(stream.getName(), pipe);
				published.put(stream.getName(), bookkeeper);
			}
		}
		// connect to the pipe
		DownStreamAdapter downstream = new DownStreamAdapter(stream);
		stream.setDownStreamAdapter(downstream);
		bookkeeper.getPipe().subscribe(downstream);
//		MultiStreamSink multi = (MultiStreamSink) published.get(stream.getName());
//		multi.connect(stream);
	}
	
	public IStreamSource lookupStreamSource(String name){
		return createFileStreamSource(name);
	}

	protected IStreamSource createFileStreamSource(String name){
		Resource[] resource = null;
		FileStreamSource source = null;
		try {
			File file = appCtx.getResources("streams/" + name)[0].getFile();
			IFLV flv = flvService.getFLV(file);
			source = new FileStreamSource(flv.reader());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return source;
	}
	
	private class PipeBookkeeper implements IPipeConnectionListener {
		private String name;
		private IPipe pipe;
		// count of providers and consumers connected
		// to this pipe
		private int userCount;
		
		public PipeBookkeeper(String name, IPipe pipe) {
			this.name = name;
			this.pipe = pipe;
			this.userCount = 0;
			this.pipe.addPipeConnectionListener(this);
		}
		
		public IPipe getPipe() {
			return this.pipe;
		}

		public void onPipeConnectionEvent(PipeConnectionEvent event) {
			switch (event.getType()) {
			case PipeConnectionEvent.PROVIDER_CONNECT_PUSH:
			case PipeConnectionEvent.CONSUMER_CONNECT_PUSH:
			case PipeConnectionEvent.PROVIDER_CONNECT_PULL:
			case PipeConnectionEvent.CONSUMER_CONNECT_PULL:
				userCount++;
				log.info("Add " + userCount + " client for " + name);
				break;
			case PipeConnectionEvent.PROVIDER_DISCONNECT:
			case PipeConnectionEvent.CONSUMER_DISCONNECT:
				log.info("Remove " + userCount + " client for " + name);
				if (--userCount == 0) {
					pipe.removePipeConnectionListener(this);
					// remove this
					published.remove(name);
					log.info("Released pipe for " + name);
				}
				break;
			default:
				break;
			}
		}
		
	}
}
