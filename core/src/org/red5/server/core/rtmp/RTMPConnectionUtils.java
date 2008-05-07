package org.red5.server.core.rtmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.red5.server.common.rtmp.packet.RTMPInvoke;

public class RTMPConnectionUtils {
	public static void returnResultForInvoke(
			RTMPConnection connection, Object result,
			RTMPInvoke invoke) {
		returnResult(connection, result,
				invoke.getInvokeId(),
				invoke.getStreamId(),
				invoke.getChannel()
				);
	}
	
	public static void returnErrorForInvoke(
			RTMPConnection connection, Object result,
			RTMPInvoke invoke) {
		returnError(connection, result,
				invoke.getInvokeId(),
				invoke.getStreamId(),
				invoke.getChannel()
				);
	}
	
	public static void returnResult(
			RTMPConnection connection, Object result,
			long invokeId, int streamId, int channel) {
		Object objReturn = result;
		if (result instanceof RTMPStatus) {
			RTMPStatus status = (RTMPStatus) result;
			Map<String,Object> statusMap = new HashMap<String,Object>();
			statusMap.put("code", status.getCode());
			statusMap.put("level", status.getCode());
			statusMap.put("description", status.getCode());
			if (status.getStreamId() > 0) {
				statusMap.put("streamId", status.getStreamId());
			}
			statusMap.putAll(status.getAdditionals());
			objReturn = statusMap;
		}
		returnResultOrError(connection, objReturn,
				invokeId, streamId, channel, false);
	}
	
	public static void returnError(
			RTMPConnection connection, Object result,
			long invokeId, int streamId, int channel) {
		returnResultOrError(connection, result,
				invokeId, streamId, channel, true);
	}
	
	private static void returnResultOrError(
			RTMPConnection connection, Object result,
			long invokeId, int streamId, int channel, boolean isError) {
		RTMPInvoke response = new RTMPInvoke();
		response.setStreamId(streamId);
		response.setChannel(channel);
		if (isError) {
			response.setAction("_error");
		} else {
			response.setAction("_result");
		}
		ArrayList<Object> args = new ArrayList<Object>();
		args.add(result);
		response.setArguments(args);
		response.setInvokeId(invokeId);
		connection.writePacket(response);
	}
	
	/**
	 * Parse the connection parameters and put the Red5 specific keys
	 * @param connectionParams
	 * @return <tt>true</tt> when successfully processed, <tt>false</tt> otherwise.
	 */
	public static boolean processConnectionParams(Map<String,Object> connectionParams) {
		// TODO enhance the implementation with regular expression
		String tcUrl = (String) connectionParams.get(RTMPConnection.CONNECT_PARAM_KEY_TCURL);
		String app = (String) connectionParams.get(RTMPConnection.CONNECT_PARAM_KEY_APP);
		int appIdx = tcUrl.lastIndexOf(app);
		if (appIdx <= 0) {
			return false;
		}
		// process url prefix = protocol:[//host][:port]
		String urlPrefix = tcUrl.substring(0, appIdx-1); // remove the last "/"
		int colonIdx = urlPrefix.indexOf(":");
		if (colonIdx < 0) {
			return false;
		}
		String protocol = urlPrefix.substring(0, colonIdx);
		String hostPort = urlPrefix.substring(colonIdx+1);
		colonIdx = hostPort.indexOf(":");
		String host = null;
		String portStr = null;
		if (colonIdx >= 0) {
			host = hostPort.substring(0, colonIdx);
			portStr = hostPort.substring(colonIdx+1);
		} else {
			host = hostPort;
			portStr = "";
		}
		if (host.startsWith("//")) {
			host.substring(2);
		}
		if (host.equals("")) {
			host = "localhost";
		}
		if (portStr.equals("")) {
			portStr = "1935";
		}
		int port = 1935;
		try {
			port = Integer.parseInt(portStr);
		} catch (NumberFormatException e) {
			return false;
		}
		// process app string = app[/instance][?queryString]
		int questionMark = app.indexOf("?");
		String queryString = null;
		if (questionMark >= 0) {
			queryString = app.substring(questionMark+1);
			app = app.substring(0, questionMark);
		}
		int slashIdx = app.indexOf("/");
		String appName = null;
		String appInstanceName = null;
		if (slashIdx >= 0) {
			appName = app.substring(0, slashIdx);
			appInstanceName = app.substring(slashIdx+1);
		} else {
			appName = app;
			appInstanceName = "";
		}
		if (appInstanceName.equals("")) {
			appInstanceName = RTMPApplicationInstance.DEFAULT_INSTANCE_NAME;
		}
		// now put the parsed key/value pairs to the map
		connectionParams.put(RTMPConnection.CONNECT_PARAM_KEY_PROTOCOL, protocol);
		connectionParams.put(RTMPConnection.CONNECT_PARAM_KEY_HOST, host);
		connectionParams.put(RTMPConnection.CONNECT_PARAM_KEY_PORT, port);
		connectionParams.put(RTMPConnection.CONNECT_PARAM_KEY_APP_NAME, appName);
		connectionParams.put(RTMPConnection.CONNECT_PARAM_KEY_APP_INST, appInstanceName);
		if (queryString != null) {
			connectionParams.put(RTMPConnection.CONNECT_PARAM_KEY_QUERYSTRING, queryString);
		}
		return true;
	}
}
