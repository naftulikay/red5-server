package org.red5.server.plugin.icy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

	private static ExecutorService executor;

	@Override
	public void afterPropertiesSet() throws Exception {
		executor = Executors.newFixedThreadPool(poolSize);
	}

	@Override
	public void destroy() throws Exception {
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
		
		//start the consumer
		executor.execute(consumer);
	}

	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}
}
