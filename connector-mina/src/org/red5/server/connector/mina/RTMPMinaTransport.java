package org.red5.server.connector.mina;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.common.IoHandler;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTMPMinaTransport {
	private static final Logger log = LoggerFactory.getLogger(RTMPMinaTransport.class);

	private SocketAcceptor acceptor;
	private IoHandler ioHandler;
	
	public void start() throws IOException {
		log.info("RTMP Mina Transport starting...");
		acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors()+1);
		acceptor.getSessionConfig().setReuseAddress(true);
		acceptor.getSessionConfig().setTcpNoDelay(true);
		acceptor.getFilterChain().addLast(
				"threading", new ExecutorFilter(new OrderedThreadPoolExecutor(4, 16)));
		acceptor.setHandler(ioHandler);
		acceptor.bind(new InetSocketAddress(1935));
		log.info("RTMP Socket Acceptor bound to :1935");
	}
	
	public void stop() {
		acceptor.unbind();
		log.info("RTMP Mina Transport stopped");
	}

	public void setIoHandler(IoHandler ioHandler) {
		this.ioHandler = ioHandler;
	}
}
