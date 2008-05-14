package org.red5.server.common.rtmp;

public interface RTMPConstants {
	public static final int HEADER_STANDARD    = 0;
	public static final int HEADER_SAME_TARGET = 1;
	public static final int HEADER_SAME_SIZE   = 2;
	public static final int HEADER_CONTINUE    = 3;
	
	public static final int SUB_HEADER_SIZE[] = { 11, 7, 3, 0 }; // without channel id
}
