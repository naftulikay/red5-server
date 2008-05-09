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
	 * Get the initial AMF mode.
	 * @return
	 */
	AMFMode getInitialOutputMode();
	
	/**
	 * Get the current AMF mode.
	 * @return
	 */
	AMFMode getCurrentOutputMode();
	
	/**
	 * Reset the output to the initial state.
	 */
	void resetOutput();
	
	/**
	 * Reset the output to the initial state and set the AMF mode as
	 * specified.
	 * @param newMode
	 */
	void resetOutput(AMFMode newMode);
}
