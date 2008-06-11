package org.red5.server.common.amf.impl;

import java.nio.BufferOverflowException;

import org.red5.server.common.BufferEx;
import org.red5.server.common.amf.AMFInputOutputException;
import org.red5.server.common.amf.AMFOutput;
import org.red5.server.common.amf.AMFType;

public class AMFOutputLegacyWrapper implements AMFOutput {

	@Override
	public int getDefaultOutputMode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getOutputMode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void resetOutput() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOutputMode(int mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(BufferEx buf, Object object, AMFType amfType)
			throws BufferOverflowException, AMFInputOutputException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeAll(BufferEx buf, Object[] objects, AMFType[] amfTypes)
			throws BufferOverflowException, AMFInputOutputException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeString(BufferEx buf, String value) {
		// TODO Auto-generated method stub

	}

}
