package org.red5.server.common.rtmp.packet;

public class RTMPPing extends RTMPPacket {
	public static final short PING_STREAM_CLEAR         = 0;
	public static final short PING_STREAM_BUFFER_CLEAR  = 1;
	public static final short PING_UNKNOWN_2            = 2;
	public static final short PING_CLIENT_BUFFER        = 3;
	public static final short PING_STREAM_RESET         = 4;
	public static final short PING_UNKNOWN_5            = 5;
	public static final short PING_CLIENT               = 6;
	public static final short PONG_SERVER               = 7;
    public static final short PING_UNKNOWN_8            = 8;
    
    public static final int PING_PARAM_UNDEFINED        = -1;
	
	private short pingType;
	private int param0;
	private int param1;
	private int param2;
	
	public RTMPPing() {
		super(TYPE_RTMP_PING);
		param0 = param1 = param2 = PING_PARAM_UNDEFINED;
	}

	public short getPingType() {
		return pingType;
	}

	public void setPingType(short pingType) {
		this.pingType = pingType;
	}

	public int getParam0() {
		return param0;
	}

	public void setParam0(int param0) {
		this.param0 = param0;
	}

	public int getParam1() {
		return param1;
	}

	public void setParam1(int param1) {
		this.param1 = param1;
	}

	public int getParam2() {
		return param2;
	}

	public void setParam2(int param2) {
		this.param2 = param2;
	}

}
