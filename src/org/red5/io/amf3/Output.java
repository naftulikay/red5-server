package org.red5.io.amf3;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright (c) 2006 by respective authors (see below). All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 2.1 of the License, or (at your option) any later 
 * version. 
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this library; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ByteBuffer;
import org.red5.io.amf.AMF;
import org.red5.io.object.Serializer;

/**
 * AMF3 output writer
 *
 * @see  org.red5.io.amf3.AMF3
 * @see  org.red5.io.amf3.Input
 * @author The Red5 Project (red5@osflash.org)
 * @author Joachim Bauch (jojo@struktur.de)
 */
public class Output extends org.red5.io.amf.Output implements org.red5.io.object.Output {

	protected static Log log = LogFactory.getLog(Output.class.getName());

	/**
	 * Set to a value above <tt>0</tt> to disable writing of the AMF3 object tag.
	 */
	private int amf3_mode;
	/**
	 * List of strings already written.
	 * */
	private List<String> stringReferences;
	
	/**
	 * Constructor of AMF3 output.
	 *
	 * @param buf
	 *            instance of ByteBuffer
	 * @see ByteBuffer
	 */
	public Output(ByteBuffer buf) {
		super(buf);
		amf3_mode = 0;
		stringReferences = new LinkedList<String>();
	}
	
    /** {@inheritDoc} */
	public boolean supportsDataType(byte type) {
		return true;
	}
	
	// Basic Data Types
	
	protected void writeAMF3() {
		if (amf3_mode == 0)
			buf.put(AMF.TYPE_AMF3_OBJECT);
	}
	
    /** {@inheritDoc} */
	public void writeBoolean(Boolean bol) {
		writeAMF3();
		buf.put(bol ? AMF3.TYPE_BOOLEAN_TRUE : AMF3.TYPE_BOOLEAN_FALSE);
	}
	
    /** {@inheritDoc} */
	public void writeNull() {
		writeAMF3();
		buf.put(AMF3.TYPE_NULL);
	}
	
    /** {@inheritDoc} */
	protected void putInteger(long value) {
		if (value < 0) {
			buf.put((byte) (0x80 | ((value >> 22) & 0xff)));
			buf.put((byte) (0x80 | ((value >> 15) & 0x7f)));
			buf.put((byte) (0x80 | ((value >> 8) & 0x7f)));
			buf.put((byte) (value & 0xff));
		} else if (value <= 0x7f) {
			buf.put((byte) value);
		} else if (value <= 0x3fff) {
			buf.put((byte) (0x80 | ((value >> 7) & 0x7f)));
			buf.put((byte) (value & 0x7f));
		} else if (value <= 0x1fffff) {
			buf.put((byte) (0x80 | ((value >> 14) & 0x7f)));
			buf.put((byte) (0x80 | ((value >> 7) & 0x7f)));
			buf.put((byte) (value & 0x7f));
		} else {
			buf.put((byte) (0x80 | ((value >> 22) & 0xff)));
			buf.put((byte) (0x80 | ((value >> 15) & 0x7f)));
			buf.put((byte) (0x80 | ((value >> 8) & 0x7f)));
			buf.put((byte) (value & 0xff));
		}
	}
	
	/** {@inheritDoc} */
	protected void putString(String str, java.nio.ByteBuffer string) {
		final int len = string.limit();
		int pos = stringReferences.indexOf(str);
		if (pos >= 0) {
			// Reference to existing string
			putInteger(pos << 1);
			return;
		}
		
		putInteger(len << 1 | 1);
		buf.put(string);
	}
	
    /** {@inheritDoc} */
	public void putString(String string) {
		if ("".equals(string)) {
			// Empty string;
			putInteger(1);
			return;
		}
		
		final java.nio.ByteBuffer strBuf = AMF3.CHARSET.encode(string);
		putString(string, strBuf);
	}
	
    /** {@inheritDoc} */
	public void writeNumber(Number num) {
		writeAMF3();
		if (num instanceof Long || num instanceof Integer || num instanceof Short || num instanceof Byte) {
			buf.put(AMF3.TYPE_INTEGER);
			putInteger(num.longValue());
		} else {
			buf.put(AMF3.TYPE_NUMBER);
			buf.putDouble(num.doubleValue());
		}
	}
	
    /** {@inheritDoc} */
	public void writeString(String string) {
		writeAMF3();
		buf.put(AMF3.TYPE_STRING);
		if ("".equals(string)) {
			putInteger(1);
		} else {
			final java.nio.ByteBuffer strBuf = AMF3.CHARSET.encode(string);
			putString(string, strBuf);
		}
	}
	
    /** {@inheritDoc} */
	public void writeDate(Date date) {
		writeAMF3();
		buf.put(AMF3.TYPE_DATE);
		if (hasReference(date)) {
			putInteger(getReferenceId(date) << 1);
			return;
		}
		
		storeReference(date);
		putInteger(1);
		buf.putDouble(date.getTime());
	}
	
    /** {@inheritDoc} */
    public void writeArray(Collection array, Serializer serializer) {
		writeAMF3();
		buf.put(AMF3.TYPE_ARRAY);
    	if (hasReference(array)) {
    		putInteger(getReferenceId(array) << 1);
    		return;
    	}
    	
    	storeReference(array);
		amf3_mode += 1;
		int count = array.size();
		putInteger(count << 1 | 1);
		putString("");
		for (Object item: array) {
			serializer.serialize(this, item);
		}
		amf3_mode -= 1;
    }

    /** {@inheritDoc} */
    public void writeArray(Object[] array, Serializer serializer) {
		writeAMF3();
		buf.put(AMF3.TYPE_ARRAY);
    	if (hasReference(array)) {
    		putInteger(getReferenceId(array) << 1);
    		return;
    	}
    	
    	storeReference(array);
		amf3_mode += 1;
		int count = array.length;
		putInteger(count << 1 | 1);
		putString("");
		for (Object item: array) {
			serializer.serialize(this, item);
		}
		amf3_mode -= 1;
    }

    /** {@inheritDoc} */
    public void writeArray(Object array, Serializer serializer) {
		writeAMF3();
		buf.put(AMF3.TYPE_ARRAY);
    	if (hasReference(array)) {
    		putInteger(getReferenceId(array) << 1);
    		return;
    	}
    	
    	storeReference(array);
		amf3_mode += 1;
		int count = Array.getLength(array);
		putInteger(count << 1 | 1);
		putString("");
		for (int i=0; i<count; i++) {
			serializer.serialize(this, Array.get(array, i));
		}
		amf3_mode -= 1;
    }

    /** {@inheritDoc} */
    public void writeMap(Map<Object, Object> map, Serializer serializer) {
    	if (hasReference(map)) {
    		writeAMF3();
    		buf.put(AMF3.TYPE_ARRAY);
    		putInteger(getReferenceId(map) << 1);
    		return;
    	}
    	
    	storeReference(map);
		// Search number of starting integer keys
		int count = 0;
		for (int i=0; i<map.size(); i++) {
			if (map.containsKey(i)) {
				count++;
			} else {
				break;
			}
		}
		
		writeAMF3();
		buf.put(AMF3.TYPE_ARRAY);
		amf3_mode += 1;
		if (count == map.size()) {
			// All integer keys starting from zero: serialize as regular array
			putInteger(count << 1 | 1);
			putString("");
			for (int i=0; i<count; i++) {
				serializer.serialize(this, map.get(i));
			}
			amf3_mode -= 1;
			return;
		}
		
		putInteger(count << 1 | 1);
		// Serialize key-value pairs first
		for (Map.Entry<Object, Object> entry: map.entrySet()) {
			Object key = entry.getKey();
			if ((key instanceof Number) && !(key instanceof Float) && !(key instanceof Double) &&
					((Number) key).longValue() >= 0 && ((Number) key).longValue() < count) {
				// Entry will be serialized later
				continue;
			}
			putString(key.toString());
			serializer.serialize(this, entry.getValue());
		}
		putString("");
		// Now serialize integer keys starting from zero
		for (int i=0; i<count; i++) {
			serializer.serialize(this, map.get(i));
		}
		amf3_mode -= 1;
    }

    /** {@inheritDoc} */
    public void writeMap(Collection array, Serializer serializer) {
    	writeString("Not implemented.");
    }

    /** {@inheritDoc} */
    public void writeObject(Object object, Serializer serializer) {
    	writeString("Not implemented.");
    }

    /** {@inheritDoc} */
    public void writeObject(Map<Object, Object> map, Serializer serializer) {
    	writeMap(map, serializer);
    }

    /** {@inheritDoc} */
	public void writeXML(String xml) {
		writeAMF3();
		buf.put(AMF3.TYPE_XML);
		putString(xml);
	}
}
