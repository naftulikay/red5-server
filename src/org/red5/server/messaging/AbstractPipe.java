/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright ? 2006 by respective authors (see below). All rights reserved.
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

package org.red5.server.messaging;

import java.util.ArrayList;
import java.util.List;

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
	
	protected List consumers = new ArrayList();
	protected List providers = new ArrayList();
	protected List listeners = new ArrayList();
	
	public void subscribe(IConsumer consumer) {
		synchronized (consumers) {
			if (consumers.contains(consumer)) return;
			consumers.add(consumer);
		}
		if (consumer instanceof IPipeConnectionListener) {
			synchronized (listeners) {
				listeners.add(consumer);
			}
		}
		fireConsumerConnectionEvent(consumer, PipeConnectionEvent.CONSUMER_CONNECT_PUSH);
	}

	public void subscribe(IProvider provider) {
		synchronized (providers) {
			if (providers.contains(provider)) return;
			providers.add(provider);
		}
		if (provider instanceof IPipeConnectionListener) {
			synchronized (listeners) {
				listeners.add(provider);
			}
		}
		fireProviderConnectionEvent(provider, PipeConnectionEvent.PROVIDER_CONNECT_PUSH);
	}

	public void unsubscribe(IProvider provider) {
		synchronized (providers) {
			if (!providers.contains(provider)) return;
			providers.remove(provider);
		}
		fireProviderConnectionEvent(provider, PipeConnectionEvent.PROVIDER_DISCONNECT);
		if (provider instanceof IPipeConnectionListener) {
			synchronized (listeners) {
				listeners.remove(provider);
			}
		}
	}

	public void unsubscribe(IConsumer consumer) {
		synchronized (consumers) {
			if (!consumers.contains(consumer)) return;
			consumers.remove(consumer);
		}
		fireConsumerConnectionEvent(consumer, PipeConnectionEvent.CONSUMER_DISCONNECT);
		if (consumer instanceof IPipeConnectionListener) {
			synchronized (listeners) {
				listeners.remove(consumer);
			}
		}
	}
	
	protected void fireConsumerConnectionEvent(IConsumer consumer, int type) {
		PipeConnectionEvent event = new PipeConnectionEvent(this);
		event.setConsumer(consumer);
		event.setType(type);
		firePipeConnectionEvent(event);
	}
	
	protected void fireProviderConnectionEvent(IProvider provider, int type) {
		PipeConnectionEvent event = new PipeConnectionEvent(this);
		event.setProvider(provider);
		event.setType(type);
		firePipeConnectionEvent(event);
	}
	
	protected void firePipeConnectionEvent(PipeConnectionEvent event) {
		IPipeConnectionListener[] listenerArray = null;
		synchronized (listeners) {
			listenerArray = (IPipeConnectionListener[]) listeners.toArray(new IPipeConnectionListener[]{});
		}
		for (int i = 0; i < listenerArray.length; i++) {
			try {
				listenerArray[i].onPipeConnectionEvent(event);
			} catch (Throwable t) {
				log.error("exception when handling pipe connection event", t);
			}
		}
	}
}
