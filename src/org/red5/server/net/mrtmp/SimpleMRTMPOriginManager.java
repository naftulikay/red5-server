package org.red5.server.net.mrtmp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SimpleMRTMPOriginManager implements IMRTMPOriginManager {
	
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private Set<IMRTMPConnection> connSet = new HashSet<IMRTMPConnection>();
	private Map<Integer, IMRTMPConnection> clientToConnMap;
	
	public SimpleMRTMPOriginManager() {
		clientToConnMap = Collections.synchronizedMap(
				new WeakHashMap<Integer, IMRTMPConnection>());
	}
	
	public void setAfinity(IMRTMPConnection conn, int clientId) {
		clientToConnMap.put(clientId, conn);
	}

	public IMRTMPConnection lookupMRTMPConnection(int clientId) {
		lock.readLock().lock();
		try {
			IMRTMPConnection conn = clientToConnMap.get(clientId);
			if (conn != null && !connSet.contains(conn)) {
				clientToConnMap.remove(clientId);
				conn = null;
			}
			if (conn == null) {
				if (connSet.size() > 0) {
					conn = connSet.iterator().next();
				}
			}
			// TODO handle conn == null case
			return conn;
		} finally {
			lock.readLock().unlock();
		}
	}

	public void registerConnection(IMRTMPConnection conn) {
		lock.writeLock().lock();
		try {
			connSet.add(conn);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void unregisterConnection(IMRTMPConnection conn) {
		lock.writeLock().lock();
		try {
			connSet.remove(conn);
		} finally {
			lock.writeLock().unlock();
		}
	}

}
