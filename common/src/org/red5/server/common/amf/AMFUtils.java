package org.red5.server.common.amf;

import org.red5.server.common.BufferEx;

public class AMFUtils {
	public static boolean isAMFNull(Object object) {
		return object == AMFType.AMF_UNDEFINED ||
		object == AMFType.AMF_NULL;
	}
	
	public static Object amfReadObject(AMFInput amfInput, BufferEx buf) {
		return amfReadObject(amfInput, buf, null);
	}
	
	public static Object amfReadObject(AMFInput amfInput, BufferEx buf,
			ClassLoader classLoader) {
		Object object = amfInput.read(buf, Object.class, classLoader);
		if (isAMFNull(object)) {
			return null;
		} else {
			return object;
		}
	}
}
