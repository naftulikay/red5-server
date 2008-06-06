package org.red5.server.core.rtmp.impl;

import java.util.ArrayList;
import java.util.List;

import org.red5.server.common.BufferEx;
import org.red5.server.common.rtmp.packet.RTMPHandshake;
import org.red5.server.common.rtmp.packet.RTMPInvoke;
import org.red5.server.common.rtmp.packet.RTMPNotify;
import org.red5.server.common.rtmp.packet.RTMPPacket;
import org.red5.server.common.service.ServiceCall;
import org.red5.server.common.service.ServiceInvocationException;
import org.red5.server.common.service.ServiceInvoker;
import org.red5.server.common.service.ServiceNotFoundException;
import org.red5.server.common.service.ServiceRegistry;
import org.red5.server.core.rtmp.RTMPApplicationInstance;
import org.red5.server.core.rtmp.RTMPConnection;
import org.red5.server.core.rtmp.RTMPConnectionUtils;
import org.red5.server.core.rtmp.RTMPConnectorHandler;
import org.red5.server.core.rtmp.RTMPStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreRTMPConnectorHandler implements RTMPConnectorHandler {
	private static final Logger log = LoggerFactory.getLogger(CoreRTMPConnectorHandler.class);
	
	private Object connectService;
	private ServiceRegistry coreRegistry;
	private ServiceInvoker<RTMPConnection> serviceInvoker;

	@Override
	public void onPacket(RTMPConnection connection, RTMPPacket event) {
		switch (event.getType()) {
		case RTMPPacket.TYPE_RTMP_HANDSHAKE:
			onHandshake(connection, (RTMPHandshake) event);
			break;
		case RTMPPacket.TYPE_RTMP_NOTIFY:
		case RTMPPacket.TYPE_RTMP_INVOKE:
		case RTMPPacket.TYPE_RTMP_FLEX_STREAM_SEND:
		case RTMPPacket.TYPE_RTMP_FLEX_MESSAGE:
			onNotify(connection, (RTMPNotify) event);
			break;
		}
	}

	protected void onHandshake(RTMPConnection connection, RTMPHandshake handshake) {
		switch (connection.getState()) {
		case RTMPConnection.RTMP_CONN_STATE_INIT:
			// first handshake
			connection.onReceiveHandshake(handshake);
			RTMPHandshake response = new RTMPHandshake();
			BufferEx handshakeData = BufferEx.allocate(RTMPHandshake.HANDSHAKE_SIZE*2+1);
			handshakeData.put((byte)0x03);
			// TODO: the first four bytes of the handshake reply seem to be the
			//       server uptime - send something better here...
			handshakeData.putInt(0x01);
			for (int i = handshakeData.position(); i < RTMPHandshake.HANDSHAKE_SIZE+1; i++) {
				handshakeData.put((byte)0x00);
			}
			// put in the handshake from client
			handshakeData.put(handshake.getHandshakeData());
			handshakeData.flip();
			response.setHandshakeData(handshakeData);
			break;
		case RTMPConnection.RTMP_CONN_STATE_HANDSHAKE:
			// second handshake
			connection.onReceiveHandshake(handshake);
			break;
		default:
			// invalid state, ignore
			break;
		}
	}
	
	protected void onNotify(RTMPConnection connection, RTMPNotify notify) {
		String action = notify.getAction();
		if (action == null) {
			log.error("Notify action should not be null!");
			return;
		}
		String serviceName = null;
		String methodName = null;
		int dotIdx = action.indexOf('.');
		if (dotIdx >= 0) {
			serviceName = action.substring(0, dotIdx);
			methodName = action.substring(dotIdx+1);
		} else {
			methodName = action;
		}
		
		// check server->client result
		if (serviceName == null &&
			("_result".equals(methodName) || "_error".equals(methodName)) &&
			notify instanceof RTMPInvoke && notify.getArguments().size() == 1) {
			RTMPInvoke invoke = (RTMPInvoke) notify;
			Object result = invoke.getArguments().get(0);
			// TODO pass channel and stream id?
			if ("_result".equals(methodName)) {
				connection.onResult(invoke.getInvokeId(), result);
			} else {
				connection.onError(invoke.getInvokeId(), result);
			}
			return;
		}
		
		// handle client->server call
		ServiceCall<RTMPConnection> call = new ServiceCall<RTMPConnection>();
		call.setCallContext(connection);
		call.setServiceName(serviceName);
		call.setMethodName(methodName);
		List<Object> arguments = new ArrayList<Object>();
		if (serviceName == null && "connect".equals(methodName) && notify instanceof RTMPInvoke) {
			RTMPInvoke invoke = (RTMPInvoke) notify;
			arguments.add(invoke.getConnectionParams());
			arguments.add(invoke.getArguments());
		} else {
			// add stream id to the beginning
			if (notify.getStreamId() > 0) {
				arguments.add(new Integer(notify.getStreamId()));
			}
			arguments.addAll(notify.getArguments());
		}
		call.setArguments(arguments.toArray());
		
		// check connect/disconnect call
		if (serviceName == null &&
			("connect".equals(methodName) || ("disconnect".equals(methodName))) &&
			notify instanceof RTMPInvoke) {
			handleConnect(call, (RTMPInvoke) notify);
			return;
		}

		Object result = null;
		// Core services
		try {
			// TODO use async call instead
			result = serviceInvoker.syncInvoke(coreRegistry, call);
		} catch (ServiceNotFoundException e) {
			result = e;
			if (connection.getState() == RTMPConnection.RTMP_CONN_STATE_CONNECTED) {
				// Try services in app instance
				RTMPApplicationInstance appInstance = connection.getApplicationInstance();
				ServiceRegistry instanceRegistry = appInstance.getInstanceRegistry();
				if (instanceRegistry != null) {
					try {
						result = serviceInvoker.syncInvoke(instanceRegistry, call);
					} catch (ServiceNotFoundException e1) {
						result = e1;
					} catch (ServiceInvocationException e1) {
						result = e1;
					}
				}
			}
		} catch (ServiceInvocationException e) {
			result = e;
		}
		if (notify instanceof RTMPInvoke) {
			RTMPInvoke invoke = (RTMPInvoke) notify;
			if (result instanceof ServiceNotFoundException ||
					result instanceof ServiceInvocationException) {
				result = RTMPStatus.generateErrorResult(
						RTMPStatus.NC_CALL_FAILED,
						((ServiceInvocationException) result).getCause()
				);
			}
			RTMPConnectionUtils.returnResultForInvoke(connection, result, invoke);
		}
	}

	@Override
	public void packetSent(RTMPConnection connection, RTMPPacket packet) {
		// TODO fire packet sent event
	}

	@Override
	public void sessionClosed(RTMPConnection connection) {
		// TODO fire close event
		connection.close();
	}

	@Override
	public void sessionOpened(RTMPConnection connection) {
		// TODO fire open event
	}

	public void setCoreRegistry(ServiceRegistry coreRegistry) {
		this.coreRegistry = coreRegistry;
	}

	public void setServiceInvoker(ServiceInvoker<RTMPConnection> serviceInvoker) {
		this.serviceInvoker = serviceInvoker;
	}

	public void setConnectService(Object connectService) {
		this.connectService = connectService;
	}
	
	protected void handleConnect(ServiceCall<RTMPConnection> call, RTMPInvoke invoke) {
		RTMPConnection connection = call.getCallContext();
		Object result = null;
		try {
			// TODO use async call instead
			result = serviceInvoker.syncInvoke(connectService, call);
		} catch (ServiceNotFoundException e) {
			result = e;
		} catch (ServiceInvocationException e) {
			result = e;
		}
		boolean isError = false;
		if (result instanceof RTMPStatus) {
			// only connect call will return this
			RTMPStatus rtmpStatus = (RTMPStatus) result;
			if (!rtmpStatus.getCode().equals(RTMPStatus.NC_CONNECT_SUCCESS)) {
				isError = true;
			}
			RTMPConnectionUtils.returnResultForInvoke(connection, result, invoke);
		} else if (result instanceof Throwable) {
			isError = true;
		}
		// close on error
		if (isError) {
			connection.close();
		}
	}
}
