package org.red5.server.net.mrtmp;

import java.net.InetSocketAddress;

import org.apache.mina.common.IoHandler;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;

public class MRTMPClient {
	private IoHandler ioHandler;
	private String server;
	private int port;
	
	public String getServer() {
		return server;
	}
	public void setServer(String address) {
		this.server = address;
	}
	public IoHandler getIoHandler() {
		return ioHandler;
	}
	public void setIoHandler(IoHandler ioHandler) {
		this.ioHandler = ioHandler;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	public void start() {
		SocketConnector connector = new SocketConnector();
		SocketConnectorConfig config = new SocketConnectorConfig();
		SocketSessionConfig sessionConf =
			(SocketSessionConfig) config.getSessionConfig();
		sessionConf.setTcpNoDelay(true);
		connector.connect(new InetSocketAddress(server, port), ioHandler, config);
	}
	
}
