package org.red5.server.common.rtmp.packet;

public class RTMPSharedObject {
	public static final int SERVER_CONNECT          = 0x1;
	public static final int SERVER_DISCONNECT       = 0x2;
	public static final int SERVER_SET_ATTRIBUTE    = 0x3;
	public static final int CLIENT_UPDATE_DATA      = 0x4; 
	public static final int CLIENT_UPDATE_ATTRIBUTE = 0x5;
	public static final int SEND_MESSAGE            = 0x6;
	public static final int CLIENT_STATUS           = 0x7;
	public static final int CLIENT_CLEAR_DATA       = 0x8;
	public static final int CLIENT_DELETE_DATA      = 0x9;
	public static final int SERVER_DELETE_ATTRIBUTE = 0xa;
	public static final int CLIENT_INITIAL_DATA     = 0xb;
	
	private int soType;
	private int soLength;
	private String key;
	private Object value;
	
	public RTMPSharedObject(int soType) {
		this.soType = soType;
	}
	
	public int getSoType() {
		return soType;
	}
	
	protected void setSoType(int soType) {
		this.soType = soType;
	}
	
	public int getSoLength() {
		return soLength;
	}
	
	public void setSoLength(int soLength) {
		this.soLength = soLength;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
