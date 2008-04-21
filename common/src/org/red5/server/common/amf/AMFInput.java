package org.red5.server.common.amf;

import org.red5.server.common.ExByteBuffer;

/**
 * Interface to read AMF objects.
 * @author Steven Gong (steven.gong@gmail.com)
 */
public interface AMFInput {
	/**
	 * Read an AMF object from a user-provided buffer. The AMF
	 * object will be mapped to a corresponding Java object. If the
	 * objectClass is provided, the object will be casted to the
	 * specified objectClass or ignored if it is <tt>null</tt> which
	 * means the default AMF to Java mapping is used.
	 * <p>
	 * NOTE: AMF null and AMF undefined will be mapped to
	 * AMFType.AMF_NULL and AMFType.AMF_UNDEFINED respectively.
	 * <p>
	 * The input buffer will be set to the original state if any exception
	 * occurs, otherwise all bytes in the buffer will be consumed if
	 * it is not enough to create an object.
	 * 
	 * @param buf Buffer to read AMF object from.
	 * @param objectClass The Java class the object is casted to.
	 * @return The mapped Java object from AMF object. If the remaining
	 * bytes are not enough for an object, <tt>null</tt> will be returned.
	 * @exception ClassCastException The Java object can't be casted
	 * to objectClass.
	 * @exception AMFInputOutputException If the input doesn't follow AMF format
	 * or the java class mapping specified in the content is not found.
	 */
	<T> T read(ExByteBuffer buf, Class<T> objectClass)
	throws AMFInputOutputException, ClassCastException;
	
	/**
	 * Read as many objects as possible from the input byte buffer.
	 * The input buffer will be set to the original state if any exception
	 * occurs, otherwise all bytes in the buffer will be consumed.
	 * 
	 * @param buf Buffer to read AMF objects from.
	 * @param objectClasses An array of class types to cast objects to.
	 * @return An array of mapped Java objects from AMF objects.
	 */
	@SuppressWarnings("unchecked")
	Object[] readAll(ExByteBuffer buf, Class[] objectClasses)
	throws AMFInputOutputException, ClassCastException;
	
	/**
	 * Get the initial input mode of this input.
	 * An input can start with AMF0 or AMF3 input mode.
	 * @return
	 */
	AMFMode getInitialInputMode();
	
	/**
	 * Get the current input mode.
	 * @return
	 */
	AMFMode getCurrentInputMode();
	
	/**
	 * Reset this input to the initial state. All the internal state
	 * will be reset and the input mode will be set to the intial one.
	 */
	void resetInput();
	
	/**
	 * Reset this input to the initial state. All the internal state
	 * will be reset and the input mode is set to the mode provided.
	 * @param newMode The new mode to start with.
	 */
	void resetInput(AMFMode newMode);
}
