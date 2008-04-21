package org.red5.server.common.service;

public interface ServiceCallback {
	void onResult(Object result, Throwable error);
}
