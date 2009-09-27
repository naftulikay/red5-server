package org.red5.server.plugin.icy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.plugin.icy.parser.NSVStreamConfig;
import org.red5.server.plugin.icy.stream.NSVConsumer;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Provides a means to manage streams and threads.
 * 
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class StreamManager implements InitializingBean, DisposableBean {

	private static Logger log = Red5LoggerFactory.getLogger(StreamManager.class, "plugins");
	
	//executor thread pool size
	private int poolSize = 1;

	private static AtomicInteger streamId = new AtomicInteger(0);
	
	private static ExecutorService executor;
	
	private static final Set<NSVConsumer> consumers = new HashSet<NSVConsumer>();

	public static ArrayList<NSVStreamConfig> streams = new ArrayList<NSVStreamConfig>();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		executor = Executors.newFixedThreadPool(poolSize);
	}

	@Override
	public void destroy() throws Exception {
		//clear the set
		consumers.clear();
		//disable new tasks from being submitted
		executor.shutdown();
		try {
			//wait a while for existing tasks to terminate
			if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
				executor.shutdownNow(); // cancel currently executing tasks
				//wait a while for tasks to respond to being canceled
				if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
					System.err.println("Notifier pool did not terminate");
				}
			}
		} catch (InterruptedException ie) {
			// re-cancel if current thread also interrupted
			executor.shutdownNow();
			// preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}
	
	public void addConsumer(final NSVConsumer consumer) {
		log.debug("Add consumer: {}", consumer);
		//add consumer to collection
		if (consumers.add(consumer)) {
			Runnable initer = new Runnable() {
				public void run() {
		    		//start the consumer
		    		consumer.init();	
				}
			};
			StreamManager.submit(initer);
		}
	}
	
	public void removeConsumer(NSVConsumer consumer) {
		log.debug("Remove consumer: {}", consumer);
		//remove it
		if (consumers.remove(consumer)) {
			consumer.stop();
		}
	}
	
	/**
	 * Adds a runnable to the executor service.
	 * 
	 * @param runnable
	 */
	public static void submit(Runnable runnable) {
		log.debug("Submit runnable");
		executor.execute(runnable);
	}

	/**
	 * Creates a stream config based on given properties.
	 * 
	 * @param vidtype
	 * @param audtype
	 * @param width
	 * @param height
	 * @param frameRate
	 * @return
	 */
	public static NSVStreamConfig createStreamConfig(String videoType, String audioType, int width, int height, double frameRate) {
		log.debug("Create config - video: {} audio: {} width: {} height: {} fps: {}", new Object[]{videoType, audioType, width, height, frameRate});
		NSVStreamConfig newConfig = new NSVStreamConfig();
		newConfig.streamId = streamId.incrementAndGet();
		newConfig.videoFormat = videoType;
		newConfig.audioFormat = audioType;
		newConfig.videoWidth = width;
		newConfig.videoHeight = height;
		newConfig.frameRate = frameRate;
		//add it to the stream collection
		streams.add(newConfig);
		
		return newConfig;
	}
	
	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
}
