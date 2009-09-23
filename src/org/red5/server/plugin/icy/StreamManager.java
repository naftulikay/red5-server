package org.red5.server.plugin.icy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.red5.server.plugin.icy.parser.NSVStreamConfig;
import org.red5.server.plugin.icy.stream.NSVConsumer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Provides a means to manage streams and threads.
 * 
 * @author Paul Gregoire (mondain@gmail.com)
 */
public class StreamManager implements InitializingBean, DisposableBean {

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
	
	public void addConsumer(NSVConsumer consumer) {
		//add consumer to collection
		if (consumers.add(consumer)) {
    		//start the consumer
    		consumer.init();
		}
	}
	
	public void removeConsumer(NSVConsumer consumer) {
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
	public static NSVStreamConfig createStreamConfig(String vidtype, String audtype, int width, int height, double frameRate) {
		NSVStreamConfig newConfig = new NSVStreamConfig();
		newConfig.streamId = streamId.incrementAndGet();
		newConfig.videoFormat = vidtype;
		newConfig.audioFormat = audtype;
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
