package org.red5.server.common.amf.impl;

import java.util.ArrayList;
import java.util.List;

import org.red5.server.common.BufferEx;
import org.red5.server.common.amf.AMFConstants;
import org.red5.server.common.amf.AMFInput;
import org.red5.server.common.amf.AMFInputOutputException;

public class AMFInputLegacyWrapper
implements AMFInput, AMFConstants {
	private int defaultInputMode;
	private int inputMode;
	private ClassLoader defaultClassLoader;
	private org.red5.io.amf.Input amf0Input;
	private org.red5.io.amf3.Input amf3Input;
	private BufferEx workingBuf;
	
	public AMFInputLegacyWrapper(int defaultInputMode) {
		this.defaultInputMode = defaultInputMode;
		this.inputMode = defaultInputMode;
		amf0Input = new org.red5.io.amf.Input(null);
		amf3Input = new org.red5.io.amf3.Input(null);
		workingBuf = BufferEx.allocate(512);
	}

	@Override
	public int getDefaultInputMode() {
		return defaultInputMode;
	}

	@Override
	public int getInputMode() {
		return inputMode;
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
		return read(buf, objectClass, defaultClassLoader);
	}

	@Override
	public Object[] readAll(BufferEx buf, ClassLoader classLoader)
	throws AMFInputOutputException,ClassCastException {
		List<Object> objectList = new ArrayList<Object>();
		Object object;
		while (true) {
			object = read(buf, null, classLoader);
			if (object != null) {
				objectList.add(object);
			} else {
				break;
			}
		}
		return objectList.toArray();
	}

	@Override
	public Object[] readAll(BufferEx buf) throws AMFInputOutputException, ClassCastException {
		return readAll(buf, defaultClassLoader);
	}

	@Override
	public String readString(BufferEx buf) {
		if (inputMode == AMF_MODE_0) {
			amf0Input.wrapBuffer(buf);
			return amf0Input.readString();
		} else {
			amf3Input.wrapBuffer(buf);
			return amf3Input.readString();
		}
	}

	@Override
	public void resetInput() {
		amf0Input.reset();
		amf3Input.reset();
		inputMode = defaultInputMode;
	}

	@Override
	public void setDefaultClassLoader(ClassLoader defaultClassLoader) {
		this.defaultClassLoader = defaultClassLoader;
	}

	@Override
	public void setInputMode(int mode) {
		amf0Input.reset();
		amf3Input.reset();
		inputMode = mode;
	}

}
