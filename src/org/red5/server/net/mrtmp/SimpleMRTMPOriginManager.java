package org.red5.server.net.mrtmp;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.red5.server.api.IConnection;
import org.red5.server.net.rtmp.RTMPConnection;

public class SimpleMRTMPOriginManager implements IMRTMPOriginManager {
	
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private Set<IMRTMPConnection> connSet = new HashSet<IMRTMPConnection>();
	private Map<RTMPConnection, IMRTMPConnection> clientToConnMap;
	
	public SimpleMRTMPOriginManager() {
		// XXX Use HashMap instead of WeakHashMap temporarily
		// to avoid package routing issue before Terracotta
		// integration.
		clientToConnMap = Collections.synchronizedMap(
				new HashMap<RTMPConnection, IMRTMPConnection>());
	}

	public boolean registerConnection(IMRTMPConnection conn) {
		lock.writeLock().lock();
		try {
			return connSet.add(conn);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public boolean unregisterConnection(IMRTMPConnection conn) {
		lock.writeLock().lock();
		try {
			return connSet.remove(conn);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void associate(RTMPConnection rtmpConn, IMRTMPConnection mrtmpConn) {
		clientToConnMap.put(rtmpConn, mrtmpConn);
	}

	public void dissociate(RTMPConnection rtmpConn) {
		clientToConnMap.remove(rtmpConn);
	}

	public IMRTMPConnection lookupMRTMPConnection(RTMPConnection rtmpConn) {
		lock.readLock().lock();
		try {
			IMRTMPConnection conn = clientToConnMap.get(rtmpConn);
			if (conn != null && !connSet.contains(conn)) {
				clientToConnMap.remove(rtmpConn);
				conn = null;
			}
			// mrtmp connection not found, we locate the next mrtmp connection
			// when the connection is not persistent.
			if (conn == null && !rtmpConn.getType().equals(IConnection.PERSISTENT)) {
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

}
