package org.red5.server.messaging;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright (c) 2006 by respective authors (see below). All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 2.1 of the License, or (at your option) any later 
 * version. 
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this library; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract pipe that books providers/consumers and listeners.
 * Aim to ease the implementation of concrete pipes.
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Steven Gong (steven.gong@gmail.com)
 */
public abstract class AbstractPipe implements IPipe {
	private static final Log log = LogFactory.getLog(AbstractPipe.class);
	
	protected List<IConsumer> consumers = new CopyOnWriteArrayList<IConsumer>();
	protected List<IProvider> providers = new CopyOnWriteArrayList<IProvider>();
	protected List<IPipeConnectionListener> listeners = new CopyOnWriteArrayList<IPipeConnectionListener>();
	
	public boolean subscribe(IConsumer consumer, Map paramMap) {
		synchronized (consumers) {
			if (consumers.contains(consumer)) return false;
			consumers.add(consumer);
		}
		if (consumer instanceof IPipeConnectionListener) {
			listeners.add((IPipeConnectionListener) consumer);
		}
		return true;
	}

	public boolean subscribe(IProvider provider, Map paramMap) {
		synchronized (providers) {
			if (providers.contains(provider)) return false;
			providers.add(provider);
		}
		if (provider instanceof IPipeConnectionListener) {
			listeners.add((IPipeConnectionListener) provider);
		}
		return true;
	}

	public boolean unsubscribe(IProvider provider) {
		if (!providers.remove(provider)) return false;
		fireProviderConnectionEvent(provider, PipeConnectionEvent.PROVIDER_DISCONNECT, null);
		if (provider instanceof IPipeConnectionListener) {
			listeners.remove(provider);
		}
		return true;
	}

	public boolean unsubscribe(IConsumer consumer) {
		if (!consumers.remove(consumer)) return false;
		fireConsumerConnectionEvent(consumer, PipeConnectionEvent.CONSUMER_DISCONNECT, null);
		if (consumer instanceof IPipeConnectionListener) {
			listeners.remove(consumer);
		}
		return true;
	}
	
	public void addPipeConnectionListener(IPipeConnectionListener listener) {
		listeners.add(listener);
	}

	public void removePipeConnectionListener(IPipeConnectionListener listener) {
		listeners.remove(listener);
	}

	public void sendOOBControlMessage(IProvider provider, OOBControlMessage oobCtrlMsg) {
		for (IConsumer consumer : consumers) {
			try {
				consumer.onOOBControlMessage(provider, this, oobCtrlMsg);
			} catch (Throwable t) {
				log.error("exception when passing OOBCM from provider to consumers", t);
			}
		}
	}

	public void sendOOBControlMessage(IConsumer consumer, OOBControlMessage oobCtrlMsg) {
		for (IProvider provider : providers) {
			try {
				provider.onOOBControlMessage(consumer, this, oobCtrlMsg);
			} catch (Throwable t) {
				log.error("exception when passing OOBCM from consumer to providers", t);
			}
		}
	}

	public List<IProvider> getProviders() {
		return providers;
	}
	
	public List<IConsumer> getConsumers() {
		return consumers;
	}    
	
	protected void fireConsumerConnectionEvent(IConsumer consumer, int type, Map paramMap) {
		PipeConnectionEvent event = new PipeConnectionEvent(this);
		event.setConsumer(consumer);
		event.setType(type);
		event.setParamMap(paramMap);
		firePipeConnectionEvent(event);
	}
	
	protected void fireProviderConnectionEvent(IProvider provider, int type, Map paramMap) {
		PipeConnectionEvent event = new PipeConnectionEvent(this);
		event.setProvider(provider);
		event.setType(type);
		event.setParamMap(paramMap);
		firePipeConnectionEvent(event);
	}
	
	protected void firePipeConnectionEvent(PipeConnectionEvent event) {
		for (IPipeConnectionListener listener: listeners) {
			try {
				listener.onPipeConnectionEvent(event);
			} catch (Throwable t) {
				log.error("exception when handling pipe connection event", t);
			}
		}
		// execute the pending tasks
		for (Runnable task : event.getTaskList()) {
			try {
				task.run();
			} catch (Throwable t) {}
		}
		event.getTaskList().clear();
	}
}
