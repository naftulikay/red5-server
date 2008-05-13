package org.red5.server.common;

import java.nio.ByteOrder;

public class BufferExUtils {
	public static int readMediumIntBE(byte[] byteArray) {
		int result;
		result = (byteArray[0] & 0x0ff) << 16;
		result += (byteArray[1] & 0x0ff) << 8;
		result += (byteArray[2] & 0x0ff);
		return result;
	}
	
	public static int readMediumIntBE(BufferEx buf) {
		byte[] byteArray = new byte[3];
		buf.get(byteArray);
		return readMediumIntBE(byteArray);
	}
	
	public static int readMediumIntLE(byte[] byteArray) {
		int result;
		result = (byteArray[2] & 0x0ff) << 16;
		result += (byteArray[1] & 0x0ff) << 8;
		result += (byteArray[0] & 0x0ff);
		return result;
	}
	
	public static int readMediumIntLE(BufferEx buf) {
		byte[] byteArray = new byte[3];
		buf.get(byteArray);
		return readMediumIntLE(byteArray);
	}
	
	public static int readIntBE(BufferEx buf) {
		ByteOrder originOrder = buf.order();
		buf.order(ByteOrder.BIG_ENDIAN);
		int result = buf.getInt();
		buf.order(originOrder);
		return result;
	}
	
	public static int readIntLE(BufferEx buf) {
		ByteOrder originOrder = buf.order();
		buf.order(ByteOrder.LITTLE_ENDIAN);
		int result = buf.getInt();
		buf.order(originOrder);
		return result;
	}
	
	public static void getBufferByLength(BufferEx dest, BufferEx src, int length) {
		int srcRemaining = src.remaining();
		if (srcRemaining >= length) {
			int originLimit = src.limit();
			src.limit(src.position() + srcRemaining);
			dest.put(src);
			src.limit(originLimit);
		} else {
			dest.put(src);			
		}
	}
}
