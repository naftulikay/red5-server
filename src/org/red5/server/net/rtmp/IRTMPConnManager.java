package org.red5.server.net.rtmp;

public interface IRTMPConnManager {
	RTMPConnection getConnection(int clientId);
	RTMPConnection createConnection(Class connCls);
	RTMPConnection removeConnection(int clientId);
}
