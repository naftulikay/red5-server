package org.red5.server.net.rtmp.event;

import org.apache.mina.common.ByteBuffer;
import org.red5.server.api.service.IPendingServiceCall;

public class Invoke extends Notify {
	
	protected byte EVENT_DATATYPE = TYPE_INVOKE;
	
	public Invoke(){
		super();
	}
	
	public Invoke(ByteBuffer data) {
		super(data);
	}
	
	public Invoke(IPendingServiceCall call){
		super(call);
	}
	
	public IPendingServiceCall getCall() {
		return (IPendingServiceCall) call;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Invoke: ").append(call);
		return sb.toString();
	}
	
	public boolean equals(Object obj){
		if(obj == null) return false;
		if(!(obj instanceof Invoke)) return false;
		return  super.equals(obj);
	}
	
}
