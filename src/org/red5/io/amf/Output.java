package org.red5.io.amf;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 *
 * Copyright (c) 2006-2009 by respective authors (see below). All rights reserved.
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

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.beanutils.BeanMap;
import org.red5.annotations.Anonymous;
import org.red5.io.amf3.ByteArray;
import org.red5.io.object.BaseOutput;
import org.red5.io.object.ICustomSerializable;
import org.red5.io.object.RecordSet;
import org.red5.io.object.Serializer;
import org.red5.io.utils.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 *
 * @author The Red5 Project (red5@osflash.org)
 * @author Luke Hubbard, Codegent Ltd (luke@codegent.com)
 * @author Paul Gregoire (mondain@gmail.com)
 * @author Harald Radi (harald.radi@nme.at)
 * @author Tyler Kocheran
 */
public class Output extends BaseOutput implements org.red5.io.object.Output {

	protected static Logger log = LoggerFactory.getLogger(Output.class);

	private static Cache stringCache;

	private static Cache serializeCache;

	private static Cache fieldCache;

	private static Cache getterCache;

	private static CacheManager cacheManager;

	private static CacheManager getCacheManager() {
		if (cacheManager == null) {
			String red5Root = System.getProperty("red5.root");
			if (red5Root != null) {
				// we're running Red5 as a server.
				try {
					cacheManager = new CacheManager(String.format("%s%sconf%sehcache.xml", red5Root, File.separator, File.separator));
				} catch (CacheException e) {
					cacheManager = constructDefault();
				}
			} else {
				// not a server, maybe running tests? 
				cacheManager = constructDefault();
			}
		}
		return cacheManager;
	}

	private final static CacheManager constructDefault() {
		CacheManager manager = new CacheManager();
		if (!manager.cacheExists("org.red5.io.amf.Output.stringCache")) {
			manager.addCache("org.red5.io.amf.Output.stringCache");
		}
		if (!manager.cacheExists("org.red5.io.amf.Output.getterCache")) {
			manager.addCache("org.red5.io.amf.Output.getterCache");
		}
		if (!manager.cacheExists("org.red5.io.amf.Output.fieldCache")) {
			manager.addCache("org.red5.io.amf.Output.fieldCache");
		}
		if (!manager.cacheExists("org.red5.io.amf.Output.serializeCache")) {
			manager.addCache("org.red5.io.amf.Output.serializeCache");
		}
		return manager;
	}

	/**
	 * Output buffer
	 */
	protected ByteArrayDataOutput output;

	/**
	 * Creates output with given byte buffer
	 * @param buf         Bute buffer
	 */
	public Output(ByteBuffer buf) {
		super();
		int len = buf.limit();
		if (len > 0) {
			output = ByteStreams.newDataOutput(len);
		} else {
			output = ByteStreams.newDataOutput();
		}
	}

	/** {@inheritDoc} */
	public boolean isCustom(Object custom) {
		return false;
	}

	protected boolean checkWriteReference(Object obj) {
		if (hasReference(obj)) {
			writeReference(obj);
			return true;
		} else
			return false;
	}

	/** {@inheritDoc} */
	public void writeArray(Collection<?> array) {
		if (checkWriteReference(array)) {
			return;
		}
		storeReference(array);
		output.write(AMF.TYPE_ARRAY);
		output.writeInt(array.size());
		for (Object item : array) {
			Serializer.serialize(this, item);
		}
	}

	/** {@inheritDoc} */
	public void writeArray(Object[] array) {
		log.debug("writeArray - array: {}", array);
		if (array != null) {
			if (checkWriteReference(array)) {
				return;
			}
			storeReference(array);
			output.write(AMF.TYPE_ARRAY);
			output.writeInt(array.length);
			for (Object item : array) {
				Serializer.serialize(this, item);
			}
		} else {
			writeNull();
		}
	}

	/** {@inheritDoc} */
	public void writeArray(Object array) {
		if (array != null) {
			if (checkWriteReference(array)) {
				return;
			}
			storeReference(array);
			output.write(AMF.TYPE_ARRAY);
			output.writeInt(Array.getLength(array));
			for (int i = 0; i < Array.getLength(array); i++) {
				Serializer.serialize(this, Array.get(array, i));
			}
		} else {
			writeNull();
		}
	}

	/** {@inheritDoc} */
	public void writeMap(Map<Object, Object> map) {
		if (checkWriteReference(map)) {
			return;
		}
		storeReference(map);
		output.write(AMF.TYPE_MIXED_ARRAY);
		int maxInt = -1;
		for (int i = 0; i < map.size(); i++) {
			try {
				if (!map.containsKey(i))
					break;
			} catch (ClassCastException err) {
				// Map has non-number keys.
				break;
			}

			maxInt = i;
		}
		output.writeInt(maxInt + 1);
		// TODO: Need to support an incoming key named length
		for (Map.Entry<Object, Object> entry : map.entrySet()) {
			final String key = entry.getKey().toString();
			if ("length".equals(key)) {
				continue;
			}
			putString(key);
			Serializer.serialize(this, entry.getValue());
		}
		if (maxInt >= 0) {
			putString("length");
			Serializer.serialize(this, maxInt + 1);
		}
		output.write((byte) 0x00);
		output.write((byte) 0x00);
		output.write(AMF.TYPE_END_OF_OBJECT);
	}

	/** {@inheritDoc} */
	public void writeMap(Collection<?> array) {
		if (checkWriteReference(array)) {
			return;
		}
		storeReference(array);
		output.write(AMF.TYPE_MIXED_ARRAY);
		output.writeInt(array.size() + 1);
		int idx = 0;
		for (Object item : array) {
			if (item != null) {
				putString(String.valueOf(idx++));
				Serializer.serialize(this, item);
			} else {
				idx++;
			}
		}
		putString("length");
		Serializer.serialize(this, array.size() + 1);
		output.write((byte) 0x00);
		output.write((byte) 0x00);
		output.write(AMF.TYPE_END_OF_OBJECT);
	}

	/** {@inheritDoc} */
	public void writeRecordSet(RecordSet recordset) {
		if (checkWriteReference(recordset)) {
			return;
		}
		storeReference(recordset);
		// Write out start of object marker
		output.write(AMF.TYPE_CLASS_OBJECT);
		putString("RecordSet");
		// Serialize
		Map<String, Object> info = recordset.serialize();
		// Write out serverInfo key
		putString("serverInfo");
		// Serialize
		Serializer.serialize(this, info);
		// Write out end of object marker
		output.write((byte) 0x00);
		output.write((byte) 0x00);
		output.write(AMF.TYPE_END_OF_OBJECT);
	}

	/** {@inheritDoc} */
	public boolean supportsDataType(byte type) {
		return false;
	}

	/** {@inheritDoc} */
	public void writeBoolean(Boolean bol) {
		output.write(AMF.TYPE_BOOLEAN);
		output.write(bol ? AMF.VALUE_TRUE : AMF.VALUE_FALSE);
	}

	/** {@inheritDoc} */
	public void writeCustom(Object custom) {

	}

	/** {@inheritDoc} */
	public void writeDate(Date date) {
		output.write(AMF.TYPE_DATE);
		output.writeDouble(date.getTime());
		output.writeShort((short) (TimeZone.getDefault().getRawOffset() / 60 / 1000));
	}

	/** {@inheritDoc} */
	public void writeNull() {
		// System.err.println("Write null");
		output.write(AMF.TYPE_NULL);
	}

	/** {@inheritDoc} */
	public void writeNumber(Number num) {
		output.write(AMF.TYPE_NUMBER);
		output.writeDouble(num.doubleValue());
	}

	/** {@inheritDoc} */
	public void writeReference(Object obj) {
		log.debug("Write reference");
		output.write(AMF.TYPE_REFERENCE);
		output.writeShort(getReferenceId(obj));
	}

	/** {@inheritDoc} */
	@SuppressWarnings({ "rawtypes" })
	public void writeObject(Object object) {
		if (checkWriteReference(object)) {
			return;
		}
		storeReference(object);
		// Create new map out of bean properties
		BeanMap beanMap = new BeanMap(object);
		// Set of bean attributes
		Set set = beanMap.keySet();
		if ((set.size() == 0) || (set.size() == 1 && beanMap.containsKey("class"))) {
			// BeanMap is empty or can only access "class" attribute, skip it
			writeArbitraryObject(object);
			return;
		}

		// Write out either start of object marker for class name or "empty" start of object marker
		Class<?> objectClass = object.getClass();
		if (!objectClass.isAnnotationPresent(Anonymous.class)) {
			output.write(AMF.TYPE_CLASS_OBJECT);
			putString(output, Serializer.getClassName(objectClass));
		} else {
			output.write(AMF.TYPE_OBJECT);
		}

		if (object instanceof ICustomSerializable) {
			((ICustomSerializable) object).serialize(this);
			output.write((byte) 0x00);
			output.write((byte) 0x00);
			output.write(AMF.TYPE_END_OF_OBJECT);
			return;
		}

		// Iterate thru entries and write out property names with separators
		for (Object key : set) {
			String fieldName = key.toString();
			log.debug("Field name: {} class: {}", fieldName, objectClass);
			Field field = getField(objectClass, fieldName);
			Method getter = getGetter(objectClass, beanMap, fieldName);
			// Check if the Field corresponding to the getter/setter pair is transient
			if (!serializeField(objectClass, fieldName, field, getter)) {
				continue;
			}
			putString(output, fieldName);
			Serializer.serialize(this, field, getter, object, beanMap.get(key));
		}
		// Write out end of object mark
		output.write((byte) 0x00);
		output.write((byte) 0x00);
		output.write(AMF.TYPE_END_OF_OBJECT);
	}

	@SuppressWarnings("unchecked")
	protected boolean serializeField(Class<?> objectClass, String keyName, Field field,
			Method getter) {
		//		to prevent, NullPointerExceptions, get the element first and check if it's null. 
		Element element = getSerializeCache().get(objectClass);
		Map<String, Boolean> serializeMap = (element == null ? null : (Map<String, Boolean>) element.getObjectValue());
		if (serializeMap == null) {
			serializeMap = new HashMap<String, Boolean>();
			getSerializeCache().put(new Element(objectClass, serializeMap));
		}
		Boolean serialize;
		if (getSerializeCache().isKeyInCache(keyName)) {
			serialize = serializeMap.get(keyName);
		} else {
			serialize = Serializer.serializeField(keyName, field, getter);
			serializeMap.put(keyName, serialize);
		}
		return serialize;
	}

	@SuppressWarnings("unchecked")
	protected Field getField(Class<?> objectClass, String keyName) {
		//again, to prevent null pointers, check if the element exists first.
		Element element = getFieldCache().get(objectClass);
		Map<String, Field> fieldMap = (element == null ? null : (Map<String, Field>) element.getObjectValue());
		if (fieldMap == null) {
			fieldMap = new HashMap<String, Field>();
			getFieldCache().put(new Element(objectClass, fieldMap));
		}
		Field field = null;
		if (fieldMap.containsKey(keyName)) {
			field = fieldMap.get(keyName);
		} else {
			for (Class<?> clazz = objectClass; !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
				Field[] fields = clazz.getDeclaredFields();
				if (fields.length > 0) {
					for (Field fld : fields) {
						if (fld.getName().equals(keyName)) {
							field = fld;
							break;
						}
					}
				}
			}
			fieldMap.put(keyName, field);
		}
		return field;
	}

	@SuppressWarnings("unchecked")
	protected Method getGetter(Class<?> objectClass, BeanMap beanMap, String keyName) {
		//check element to prevent null pointer
		Element element = getGetterCache().get(objectClass);
		Map<String, Method> getterMap = (element == null ? null : (Map<String, Method>) element.getObjectValue());
		if (getterMap == null) {
			getterMap = new HashMap<String, Method>();
			getGetterCache().put(new Element(objectClass, getterMap));
		}
		Method getter;
		if (getterMap.containsKey(keyName)) {
			getter = getterMap.get(keyName);
		} else {
			getter = beanMap.getReadMethod(keyName);
			getterMap.put(keyName, getter);
		}
		return getter;
	}

	/** {@inheritDoc} */
	public void writeObject(Map<Object, Object> map) {
		if (checkWriteReference(map)) {
			return;
		}
		storeReference(map);
		output.write(AMF.TYPE_OBJECT);
		boolean isBeanMap = (map instanceof BeanMap);
		for (Map.Entry<Object, Object> entry : map.entrySet()) {
			if (isBeanMap && "class".equals(entry.getKey())) {
				continue;
			}
			putString(entry.getKey().toString());
			Serializer.serialize(this, entry.getValue());
		}
		output.write((byte) 0x00);
		output.write((byte) 0x00);
		output.write(AMF.TYPE_END_OF_OBJECT);
	}

	/**
	 * Writes an arbitrary object to the output.
	 *
	 * @param serializer    Output writer
	 * @param object        Object to write
	 */
	protected void writeArbitraryObject(Object object) {
		log.debug("writeObject");
		// If we need to serialize class information...
		Class<?> objectClass = object.getClass();
		if (!objectClass.isAnnotationPresent(Anonymous.class)) {
			// Write out start object marker for class name
			output.write(AMF.TYPE_CLASS_OBJECT);
			putString(output, Serializer.getClassName(objectClass));
		} else {
			// Write out start object marker without class name
			output.write(AMF.TYPE_OBJECT);
		}
		// Iterate thru fields of an object to build "name-value" map from it
		for (Field field : objectClass.getFields()) {
			String fieldName = field.getName();
			log.debug("Field: {} class: {}", field, objectClass);
			// Check if the Field corresponding to the getter/setter pair is transient
			if (!serializeField(objectClass, fieldName, field, null)) {
				continue;
			}
			Object value;
			try {
				// Get field value
				value = field.get(object);
			} catch (IllegalAccessException err) {
				// Swallow on private and protected properties access exception
				continue;
			}
			// Write out prop name
			putString(output, fieldName);
			// Write out
			Serializer.serialize(this, field, null, object, value);
		}
		// Write out end of object marker
		output.write((byte) 0x00);
		output.write((byte) 0x00);
		output.write(AMF.TYPE_END_OF_OBJECT);
	}

	/** {@inheritDoc} */
	public void writeString(String string) {
		final byte[] encoded = encodeString(string);
		final int len = encoded.length;
		if (len < AMF.LONG_STRING_LENGTH) {
			output.write(AMF.TYPE_STRING);
			output.writeShort((short) len);
		} else {
			output.write(AMF.TYPE_LONG_STRING);
			output.writeInt(len);
		}
		output.write(encoded);
	}

	/** {@inheritDoc} */
	public void writeByteArray(ByteArray array) {
		throw new RuntimeException("ByteArray objects not supported with AMF0");
	}

	/**
	 * Encode string.
	 *
	 * @param string
	 * @return encoded string
	 */
	protected static byte[] encodeString(String string) {
		Element element = getStringCache().get(string);
		byte[] encoded = (element == null ? null : (byte[]) element.getObjectValue());
		if (encoded == null) {
			ByteBuffer buf = AMF.CHARSET.encode(string);
			encoded = new byte[buf.limit()];
			buf.get(encoded);
			getStringCache().put(new Element(string, encoded));
		}
		return encoded;
	}

	/**
	 * Write out string
	 * @param buf         Byte buffer to write to
	 * @param string      String to write
	 */
	public static void putString(ByteArrayDataOutput buf, String string) {
		final byte[] encoded = encodeString(string);
		buf.writeShort((short) encoded.length);
		buf.write(encoded);
	}

	/** {@inheritDoc} */
	public void putString(String string) {
		putString(output, string);
	}

	/** {@inheritDoc} */
	public void writeXML(Document xml) {
		output.write(AMF.TYPE_XML);
		putString(XMLUtils.docToString(xml));
	}

	/**
	 * Convenience method to allow XML text to be used, instead
	 * of requiring an XML Document.
	 * 
	 * @param xml xml to write
	 */
	public void writeXML(String xml) {
		output.write(AMF.TYPE_XML);
		putString(xml);
	}

	/**
	 * Return buffer of this Output object
	 * @return        Byte buffer of this Output object
	 */
//	public ByteBuffer buf() {
//		return this.buf;
//	}

	public void reset() {
		clearReferences();
	}

	protected static Cache getStringCache() {
		if (stringCache == null) {
			stringCache = getCacheManager().getCache("org.red5.io.amf.Output.stringCache");
		}
		return stringCache;
	}

	protected static Cache getSerializeCache() {
		if (serializeCache == null) {
			serializeCache = getCacheManager().getCache("org.red5.io.amf.Output.serializeCache");
		}
		return serializeCache;
	}

	protected static Cache getFieldCache() {
		if (fieldCache == null) {
			fieldCache = getCacheManager().getCache("org.red5.io.amf.Output.fieldCache");
		}
		return fieldCache;
	}

	protected static Cache getGetterCache() {
		if (getterCache == null) {
			getterCache = getCacheManager().getCache("org.red5.io.amf.Output.getterCache");
		}
		return getterCache;
	}
	
}