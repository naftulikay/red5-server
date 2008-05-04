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
}
