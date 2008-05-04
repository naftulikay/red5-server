package org.red5.server.core.rtmp.impl;

import java.util.ArrayList;
import java.util.List;

import org.red5.server.common.ExByteBuffer;
import org.red5.server.common.rtmp.RTMPHandler;
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
import org.red5.server.core.rtmp.RTMPStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTMPEventCoreHandler implements RTMPHandler<RTMPConnection> {
	private static final Logger log = LoggerFactory.getLogger(RTMPEventCoreHandler.class);
	
	private Object connectHandler;
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
			connection.setState(RTMPConnection.RTMP_CONN_STATE_HANDSHAKE);
			RTMPHandshake response = new RTMPHandshake();
			ExByteBuffer handshakeData = ExByteBuffer.allocate(RTMPHandshake.HANDSHAKE_SIZE*2+1);
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
			connection.setState(RTMPConnection.RTMP_CONN_STATE_CONNECTING);
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
			if ("_result".equals(methodName)) {
				connection.onResult(invoke.getInvokeId(), result, invoke.getChannel());
			} else {
				connection.onError(invoke.getInvokeId(), result, invoke.getChannel());
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
			result = serviceInvoker.syncInvoke(coreRegistry, call);
		} catch (ServiceNotFoundException e) {
			if (connection.getState() == RTMPConnection.RTMP_CONN_STATE_CONNECTED) {
				// Try services in app instance
				RTMPApplicationInstance appInstance = connection.getApplicationInstance();
				ServiceRegistry instanceRegistry = appInstance.getInstanceRegistry();
				try {
					result = serviceInvoker.syncInvoke(instanceRegistry, call);
				} catch (ServiceNotFoundException e1) {
					result = e1;
				} catch (ServiceInvocationException e1) {
					result = e1;
				}
			}
		} catch (ServiceInvocationException e) {
			result = e;
		}
		if (notify instanceof RTMPInvoke) {
			RTMPInvoke invoke = (RTMPInvoke) notify;
			boolean isSuccess = true;
			if (result instanceof ServiceNotFoundException ||
					result instanceof ServiceInvocationException) {
				isSuccess = false;
				if (result instanceof ServiceInvocationException) {
					result = RTMPStatus.generateErrorResult(
								RTMPStatus.NC_CALL_FAILED,
								((ServiceInvocationException) result).getCause()
								);
				}
			}
			if (isSuccess) {
				RTMPConnectionUtils.returnResultForInvoke(connection, result, invoke);
			} else {
				RTMPConnectionUtils.returnErrorForInvoke(connection, result, invoke);
			}
		}
	}

	public void setCoreRegistry(ServiceRegistry coreRegistry) {
		this.coreRegistry = coreRegistry;
	}

	public void setServiceInvoker(ServiceInvoker<RTMPConnection> serviceInvoker) {
		this.serviceInvoker = serviceInvoker;
	}

	public void setConnectHandler(Object connectHandler) {
		this.connectHandler = connectHandler;
	}
	
	protected void handleConnect(ServiceCall<RTMPConnection> call, RTMPInvoke invoke) {
		RTMPConnection connection = call.getCallContext();
		Object result = null;
		try {
			result = serviceInvoker.syncInvoke(connectHandler, call);
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
			if (isError) {
				RTMPConnectionUtils.returnErrorForInvoke(connection, result, invoke);
			} else {
				RTMPConnectionUtils.returnResultForInvoke(connection, result, invoke);
			}
		} else if (result instanceof Throwable) {
			isError = true;
		}
		if (isError) {
			connection.close();
		}
	}
}
