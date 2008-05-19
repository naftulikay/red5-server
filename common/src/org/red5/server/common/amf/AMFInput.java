package org.red5.server.common.amf;

import org.red5.server.common.BufferEx;

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
	 * @param classLoader The class loader to load the customized classes.
	 * Current thread's loader is used when it is <tt>null</tt>.
	 * @return The mapped Java object from AMF object. If the remaining
	 * bytes are not enough for an object, <tt>null</tt> will be returned.
	 * @exception ClassCastException The Java object can't be casted
	 * to objectClass.
	 * @exception AMFInputOutputException If the input doesn't follow AMF format
	 * or the java class mapping specified in the content is not found.
	 */
	<T> T read(BufferEx buf, Class<T> objectClass, ClassLoader classLoader)
	throws AMFInputOutputException, ClassCastException;
	
	/**
	 * Read as many objects as possible from the input byte buffer.
	 * The input buffer will be set to the original state if any exception
	 * occurs, otherwise all bytes in the buffer will be consumed.
	 * 
	 * @param buf Buffer to read AMF objects from.
	 * @param objectClasses An array of class types to cast objects to.
	 * @param classLoader The class loader to load the customized classes.
	 * Current thread's loader is used when it is <tt>null</tt>.
	 * @return An array of mapped Java objects from AMF objects.
	 */
	@SuppressWarnings("unchecked")
	Object[] readAll(BufferEx buf, Class[] objectClasses, ClassLoader classLoader)
	throws AMFInputOutputException, ClassCastException;
	
	/**
	 * Read an AMF object with the default class loader.
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
	<T> T read(BufferEx buf, Class<T> objectClass)
	throws AMFInputOutputException, ClassCastException;
	
	/**
	 * Read as many objects as possible from the input byte buffer with
	 * the default class loader.
	 * 
	 * @param buf Buffer to read AMF objects from.
	 * @param objectClasses An array of class types to cast objects to.
	 * @return An array of mapped Java objects from AMF objects.
	 */
	@SuppressWarnings("unchecked")
	Object[] readAll(BufferEx buf, Class[] objectClasses)
	throws AMFInputOutputException, ClassCastException;
	
	/**
	 * Read a direct string. The encoding depends on
	 * the current input mode.
	 * @param buf
	 * @return
	 */
	String readString(BufferEx buf);
	
	/**
	 * Get the default input mode: AMF0 or AMF3.
	 * @return
	 */
	int getDefaultInputMode();
		
	/**
	 * Get the current input mode.
	 * @return AMF0 or AMF3
	 */
	int getInputMode();
	
	/**
	 * Set the current input mode. All the internal state
	 * will be reset.
	 * @param mode AMF0 or AMF3
	 */
	void setInputMode(int mode);
	
	/**
	 * Reset this input to the initial state. All the internal state
	 * will be reset and the input mode will be set to the default one.
	 */
	void resetInput();
	
	/**
	 * Set the default class loader used by this input. Current thread's
	 * class loader will be used.
	 * 
	 * @param defaultClassLoader
	 */
	void setDefaultClassLoader(ClassLoader defaultClassLoader);
}
