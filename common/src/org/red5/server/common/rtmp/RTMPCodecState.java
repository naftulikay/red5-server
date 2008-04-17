package org.red5.server.common.rtmp;

public enum RTMPCodecState {
	HANDSHAKE_1, // state to read/write the first 1537 bytes
	HANDSHAKE_2, // state to read/write the second 1536 bytes
	GENERIC_RTMP // state to read/write normal RTMP packets
}
