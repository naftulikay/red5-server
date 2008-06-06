package org.red5.server.core.rtmp.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.red5.server.common.amf.AMFConstants;
import org.red5.server.core.rtmp.RTMPApplication;
import org.red5.server.core.rtmp.RTMPApplicationInstance;
import org.red5.server.core.rtmp.RTMPApplicationManager;
import org.red5.server.core.rtmp.RTMPConnection;
import org.red5.server.core.rtmp.RTMPConnectionUtils;
import org.red5.server.core.rtmp.RTMPStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRTMPConnectService {
	private static final Logger log = LoggerFactory.getLogger(DefaultRTMPConnectService.class);
	
	private RTMPApplicationManager applicationManager;
	
	public RTMPStatus connect(RTMPConnection connection,
			Map<String,Object> connectionParams,
			List<Object> args) {
		if (connection.getState() != RTMPConnection.RTMP_CONN_STATE_CONNECTING) {
			return RTMPStatus.generateErrorResult(
					RTMPStatus.NC_CONNECT_FAILED, "Invalid connection state");
		}
		Map<String,Object> connParamsProcessed = new HashMap<String,Object>(connectionParams);
		if (!RTMPConnectionUtils.processConnectionParams(connParamsProcessed)) {
			return RTMPStatus.generateErrorResult(
					RTMPStatus.NC_CONNECT_FAILED, "Invalid connection parameters");
		}
		
		String host = (String) connParamsProcessed.get(RTMPConnection.CONNECT_PARAM_KEY_HOST);
		String appName = (String) connParamsProcessed.get(RTMPConnection.CONNECT_PARAM_KEY_APP_NAME);
		String appInstanceName = (String) connParamsProcessed.get(RTMPConnection.CONNECT_PARAM_KEY_APP_INST);
		
		RTMPApplication application = applicationManager.getApplication(appName, host);
		if (application == null) {
			return RTMPStatus.generateErrorResult(
					RTMPStatus.NC_CONNECT_INVALID_APPLICATION,
					"Invalid application " + appName + " for virtual host " + host);
		}
		RTMPApplicationInstance appInstance = application.acquireInstance(appInstanceName);
		if (appInstance == null) {
			return RTMPStatus.generateErrorResult(
					RTMPStatus.NC_CONNECT_INVALID_APPLICATION,
					"Invalid application instance " + appInstanceName +
					" for app " + appName + " virtual host " + host);
		}
		try {
			Object errorObject = appInstance.connect(connection, connParamsProcessed, args);
			RTMPStatus rtmpStatus;
			if (errorObject == null) {
				// TODO fire connect accept event
				if (connection.connectToAppInstance(appInstance, connParamsProcessed)) {
					rtmpStatus = RTMPStatus.generateStatusResult(
							RTMPStatus.NC_CONNECT_SUCCESS, "");
				} else {
					rtmpStatus = RTMPStatus.generateErrorResult(
							RTMPStatus.NC_CONNECT_FAILED,
							"Cannot connect to app instance " + appInstanceName);
				}
			} else {
				// TODO fire connect reject event
				rtmpStatus = RTMPStatus.generateErrorResult(
						RTMPStatus.NC_CONNECT_REJECTED,
						"Application instance " + appInstanceName + " is rejected from " + appName + ", host " + host,
						errorObject);
			}
			if (Integer.valueOf(AMFConstants.AMF_MODE_3).equals(connParamsProcessed.get(
					RTMPConnection.CONNECT_PARAM_KEY_ENCODING))) {
				rtmpStatus.getAdditionals().put(
						RTMPConnection.CONNECT_PARAM_KEY_ENCODING, AMFConstants.AMF_MODE_3);
			}
			return rtmpStatus;
		} finally {
			if (appInstance != null) {
				appInstance.release();
			}
		}
	}
	
	public void disconnect(RTMPConnection connection) {
		connection.close();
	}
	
	public void setApplicationManager(RTMPApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
	}
}
