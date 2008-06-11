package org.red5.server.common.amf.impl;

import org.red5.server.common.BufferEx;
import org.red5.server.common.amf.AMFInput;
import org.red5.server.common.amf.AMFInputOutputException;

public class AMFInputLegacyWrapper implements AMFInput {

	@Override
	public int getDefaultInputMode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInputMode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> T read(BufferEx buf, Class<T> objectClass,
			ClassLoader classLoader) throws AMFInputOutputException,
			ClassCastException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T read(BufferEx buf, Class<T> objectClass)
			throws AMFInputOutputException, ClassCastException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] readAll(BufferEx buf, Class<?>[] objectClasses,
			ClassLoader classLoader) throws AMFInputOutputException,
			ClassCastException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] readAll(BufferEx buf, Class<?>[] objectClasses)
			throws AMFInputOutputException, ClassCastException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String readString(BufferEx buf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetInput() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDefaultClassLoader(ClassLoader defaultClassLoader) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInputMode(int mode) {
		// TODO Auto-generated method stub

	}

}
