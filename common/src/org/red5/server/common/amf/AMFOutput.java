package org.red5.server.common.amf;

import java.nio.BufferOverflowException;

import org.red5.server.common.BufferEx;

public interface AMFOutput {
	/**
	 * Write a Java object as an AMF object to a user-specified buffer.
	 * 
	 * @param buf Output buffer to write to.
	 * @param object Java object to write.
	 * @param amfType If not <tt>null</tt>, a Java object to AMF object
	 * mapping is specified.
	 * @exception BufferOverflowException When the output buffer is not
	 * enough to hold the object.
	 * @exception AMFInputOutputException If the Java object to AMF object
	 * mapping is not possible.
	 */
	void write(BufferEx buf, Object object, AMFType amfType)
	throws BufferOverflowException, AMFInputOutputException;
	
	/**
	 * Write an array of Java objects as AMF objects to an output buffer.
	 * 
	 * @param buf Output buffer to write to.
	 * @param objects Java objects to write.
	 * @param amfTypes An array of specified AMF types for mapping.
	 */
	void writeAll(BufferEx buf, Object[] objects, AMFType[] amfTypes)
	throws BufferOverflowException, AMFInputOutputException;
	
	/**
	 * Write a direct string. The encoding depends on
	 * the current output mode.
	 * @param buf
	 * @param value
	 */
	void writeString(BufferEx buf, String value);
	
	/**
	 * The default output mode: AMF0 or AMF3.
	 * @return
	 */
	int getDefaultOutputMode();
	
	/**
	 * The current output mode: AUTO, AMF0 or AMF3.
	 * @return
	 */
	int getOutputMode();
	
	/**
	 * Set output mode: AUTO, AMF0 or AMF3.
	 * AMF0: Only output AMF0 encoding
	 * AMF3: Only output AMF3 encoding
	 * AUTO: When the default output is AMF0,
	 * only AMF0 encoding is used.
	 * When the default output is AMF3,
	 * AMF3 encoding is wrapped in AMF0.
	 * 
	 * The internal state will be reset on return.
	 * @param mode
	 */
	void setOutputMode(int mode);
	
	/**
	 * Reset the output to the initial state.
	 */
	void resetOutput();
}
