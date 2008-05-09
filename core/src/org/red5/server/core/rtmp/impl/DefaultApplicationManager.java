package org.red5.server.core.rtmp.impl;

import org.red5.server.core.rtmp.RTMPApplication;
import org.red5.server.core.rtmp.RTMPApplicationManager;

public class DefaultApplicationManager implements RTMPApplicationManager {

	@Override
	public RTMPApplication getApplication(String appName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RTMPApplication getApplication(String appName, String virtualHost) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getVirtualHosts(String appName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean registerApplication(RTMPApplication application,
			String[] virtualHosts) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RTMPApplication unregisterApplication(String appName) {
		// TODO Auto-generated method stub
		return null;
	}

}
