package org.red5.server.stream;

import java.io.File;
import java.io.IOException;

import org.red5.server.api.IBasicScope;
import org.red5.server.api.IScope;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.messaging.IMessageInput;
import org.red5.server.messaging.IPipe;
import org.red5.server.messaging.InMemoryPullPullPipe;
import org.red5.server.stream.provider.FileProvider;

public class ProviderService implements IProviderService {
	
	public IMessageInput getProviderInput(IScope scope, String name) {
		IMessageInput msgIn = getLiveProviderInput(scope, name, false);
		if (msgIn == null) return getVODProviderInput(scope, name);
		return msgIn;
	}

	public IMessageInput getLiveProviderInput(IScope scope, String name, boolean needCreate) {
		synchronized (scope) {
			IBasicScope basicScope = scope.getBasicScope(IBroadcastScope.TYPE, name);
			if (basicScope == null) {
				if (needCreate) {
					basicScope = new BroadcastScope(scope, name);
					scope.addChildScope(basicScope);
				} else return null;
			}
			if (!(basicScope instanceof IBroadcastScope)) return null;
			return (IBroadcastScope) basicScope;
		}
	}

	public IMessageInput getVODProviderInput(IScope scope, String name) {
		File file = null;
		try {
			file = scope.getResources(getStreamFilename(name))[0].getFile();
		} catch (IOException e) {}
		if (file == null) {
			return null;
		} else if (!file.exists()) {
			return null;
		}
		IPipe pipe = new InMemoryPullPullPipe();
		pipe.subscribe(new FileProvider(scope, file), null);
		return pipe;
	}

	public boolean registerBroadcastStream(IScope scope, String name, IBroadcastStream bs) {
		synchronized (scope) {
			IBasicScope basicScope = scope.getBasicScope(IBroadcastScope.TYPE, name);
			if (basicScope == null) {
				basicScope = new BroadcastScope(scope, name);
				scope.addChildScope(basicScope);
				((IBroadcastScope) basicScope).subscribe(bs.getProvider(), null);
				return true;
			} else if (!(basicScope instanceof IBroadcastScope)) {
				return false;
			} else {
				((IBroadcastScope) basicScope).subscribe(bs.getProvider(), null);
				return true;
			}
		}
	}

	public boolean unregisterBroadcastStream(IScope scope, String name) {
		synchronized (scope) {
			IBasicScope basicScope = scope.getBasicScope(IBroadcastScope.TYPE, name);
			if (basicScope instanceof IBroadcastScope) {
				scope.removeChildScope(basicScope);
				return true;
			}
			return false;
		}
	}

	private String getStreamDirectory() {
		return "streams/";
	}
	
	private String getStreamFilename(String name) {
		return getStreamFilename(name, null);
	}
	
	private String getStreamFilename(String name, String extension) {
		String result = getStreamDirectory() + name;
		if (extension != null && !extension.equals(""))
			result += extension;
		return result;
	}
}
