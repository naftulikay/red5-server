package org.red5.server.net;

import org.red5.server.ex.StreamControlException;
import org.red5.server.ex.StreamDataException;
import org.red5.server.net.message.IStreamControl;
import org.red5.server.net.message.IStreamData;

public interface IStreamHandler {
	
	public void handleControl(IStreamControl control) throws StreamControlException;
	
	public void handleData(IStreamData data) throws StreamDataException;

}
