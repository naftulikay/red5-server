package org.red5.server.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;

/**
 * <p>
 * A byte buffer implementation that supports auto-expansion.
 * <p>
 * This implementation references ByteBuffer in MINA code but much
 * simpler.
 * @author Steven Gong (steven.gong@gmail.com)
 *
 */
public class ExByteBuffer implements Comparable<ExByteBuffer> {
	protected ByteBuffer wrapped;
	private boolean autoExpand;
	
	public static ExByteBuffer wrap(ByteBuffer buf) {
		return new ExByteBuffer(buf);
	}
	
	public static ExByteBuffer wrap(byte[] buf) {
		return new ExByteBuffer(ByteBuffer.wrap(buf));
	}
	
	public static ExByteBuffer wrap(byte[] buf, int offset, int length) {
		return new ExByteBuffer(ByteBuffer.wrap(buf, offset, length));
	}
	
	public static ExByteBuffer allocate(int capacity) {
		return new ExByteBuffer(ByteBuffer.allocate(capacity));
	}
	
	public static ExByteBuffer allocateDirect(int capacity) {
		return new ExByteBuffer(ByteBuffer.allocateDirect(capacity));
	}

	public ExByteBuffer(ByteBuffer wrapped) {
		this.wrapped = wrapped;
		this.autoExpand = false;
	}
		
	public boolean isAutoExpand() {
		return autoExpand;
	}

	public void setAutoExpand(boolean autoExpand) {
		this.autoExpand = autoExpand;
	}
	
	public ByteBuffer buf() {
		return wrapped;
	}

	public final byte[] array() {
		return wrapped.array();
	}

	public final int arrayOffset() {
		return wrapped.arrayOffset();
	}

	public ExByteBuffer asReadOnlyBuffer() {
		ExByteBuffer readonly = new ExByteBuffer(wrapped.asReadOnlyBuffer());
		return readonly;
	}

	public final int capacity() {
		return wrapped.capacity();
	}

	public final ExByteBuffer clear() {
		wrapped.clear();
		return this;
	}

	public ExByteBuffer compact() {
		wrapped.compact();
		return this;
	}

	public int compareTo(ExByteBuffer that) {
		return wrapped.compareTo(that.wrapped);
	}

	public ExByteBuffer duplicate() {
		ExByteBuffer duplicated = new ExByteBuffer(wrapped.duplicate());
		return duplicated;
	}

	public boolean equals(Object ob) {
		ExByteBuffer other = (ExByteBuffer) ob;
		return wrapped.equals(other.wrapped);
	}

	public final ExByteBuffer flip() {
		wrapped.flip();
		return this;
	}

	public byte get() {
		return wrapped.get();
	}

	public ExByteBuffer get(byte[] dst, int offset, int length) {
		wrapped.get(dst, offset, length);
		return this;
	}

	public ExByteBuffer get(byte[] dst) {
		wrapped.get(dst);
		return this;
	}

	public byte get(int index) {
		return wrapped.get(index);
	}

	public char getChar() {
		return wrapped.getChar();
	}

	public char getChar(int index) {
		return wrapped.getChar(index);
	}

	public double getDouble() {
		return wrapped.getDouble();
	}

	public double getDouble(int index) {
		return wrapped.getDouble(index);
	}

	public float getFloat() {
		return wrapped.getFloat();
	}

	public float getFloat(int index) {
		return wrapped.getFloat(index);
	}

	public int getInt() {
		return wrapped.getInt();
	}

	public int getInt(int index) {
		return wrapped.getInt(index);
	}

	public long getLong() {
		return wrapped.getLong();
	}

	public long getLong(int index) {
		return wrapped.getLong(index);
	}

	public short getShort() {
		return wrapped.getShort();
	}

	public short getShort(int index) {
		return wrapped.getShort(index);
	}

	public final boolean hasArray() {
		return wrapped.hasArray();
	}

	public int hashCode() {
		return wrapped.hashCode();
	}

	public final boolean hasRemaining() {
		return wrapped.hasRemaining();
	}

	public boolean isDirect() {
		return wrapped.isDirect();
	}

	public boolean isReadOnly() {
		return wrapped.isReadOnly();
	}

	public final int limit() {
		return wrapped.limit();
	}

	public final ExByteBuffer limit(int newLimit) {
		wrapped.limit(newLimit);
		return this;
	}

	public final ExByteBuffer mark() {
		wrapped.mark();
		return this;
	}

	public final ByteOrder order() {
		return wrapped.order();
	}

	public final ExByteBuffer order(ByteOrder bo) {
		wrapped.order(bo);
		return this;
	}

	public final int position() {
		return wrapped.position();
	}

	public final ExByteBuffer position(int newPosition) {
		wrapped.position(newPosition);
		return this;
	}

	public ExByteBuffer put(byte b) {
		checkExpand(1);
		wrapped.put(b);
		return this;
	}

	public ExByteBuffer put(byte[] src, int offset, int length) {
		checkExpand(length);
		wrapped.put(src, offset, length);
		return this;
	}

	public final ExByteBuffer put(byte[] src) {
		checkExpand(src.length);
		wrapped.put(src);
		return this;
	}

	public ExByteBuffer put(ExByteBuffer src) {
		checkExpand(src.remaining());
		wrapped.put(src.wrapped);
		return this;
	}

	public ExByteBuffer put(int index, byte b) {
		// do not support expansion for indexed operation
		wrapped.put(index, b);
		return this;
	}

	public ExByteBuffer putChar(char value) {
		checkExpand(2);
		wrapped.putChar(value);
		return this;
	}

	public ExByteBuffer putChar(int index, char value) {
		// do not support expansion for indexed operation
		wrapped.putChar(index, value);
		return this;
	}

	public ExByteBuffer putDouble(double value) {
		checkExpand(8);
		wrapped.putDouble(value);
		return this;
	}

	public ExByteBuffer putDouble(int index, double value) {
		// do not support expansion for indexed operation
		wrapped.putDouble(index, value);
		return this;
	}

	public ExByteBuffer putFloat(float value) {
		checkExpand(4);
		wrapped.putFloat(value);
		return this;
	}

	public ExByteBuffer putFloat(int index, float value) {
		// do not support expansion for indexed operation
		wrapped.putFloat(index, value);
		return this;
	}

	public ExByteBuffer putInt(int index, int value) {
		// do not support expansion for indexed operation
		wrapped.putInt(index, value);
		return this;
	}

	public ExByteBuffer putInt(int value) {
		checkExpand(4);
		wrapped.putInt(value);
		return this;
	}

	public ExByteBuffer putLong(int index, long value) {
		// do not support expansion for indexed operation
		wrapped.putLong(index, value);
		return this;
	}

	public ExByteBuffer putLong(long value) {
		checkExpand(8);
		wrapped.putLong(value);
		return this;
	}

	public ExByteBuffer putShort(int index, short value) {
		// do not support expansion for indexed operation
		wrapped.putShort(index, value);
		return this;
	}

	public ExByteBuffer putShort(short value) {
		checkExpand(2);
		wrapped.putShort(value);
		return this;
	}

	public final int remaining() {
		return wrapped.remaining();
	}

	public final ExByteBuffer reset() {
		wrapped.reset();
		return this;
	}

	public final ExByteBuffer rewind() {
		wrapped.rewind();
		return this;
	}

	public ExByteBuffer slice() {
		wrapped.slice();
		return this;
	}

	public String toString() {
		return wrapped.toString();
	}
	
	protected void checkExpand(int sizeToPut) {
		if (isAutoExpand() && !isReadOnly()) {
			final int limit = wrapped.limit();
			final int position = wrapped.position();
			final int capacity = wrapped.capacity();
			int mark = -1;
			try {
				wrapped.reset();
				mark = wrapped.position();
				wrapped.position(position);
			} catch (InvalidMarkException e) {}
			if (limit - position < sizeToPut) {
				if (capacity - position >= sizeToPut) {
					wrapped.limit(capacity);
				} else {
					ByteBuffer newBuffer = null;
					int newCapacity;
					if (capacity * 2 >= position + sizeToPut) {
						newCapacity = capacity * 2;
					} else {
						newCapacity = position + sizeToPut;
					}
					if (wrapped.isDirect()) {
						newBuffer = ByteBuffer.allocateDirect(newCapacity);
					} else {
						newBuffer = ByteBuffer.allocate(newCapacity);
					}
					wrapped.flip();
					newBuffer.put(wrapped);
					if (mark >= 0) {
						newBuffer.position(mark);
						newBuffer.mark();
						newBuffer.position(position);
					}
					wrapped = newBuffer;
				}
			}
		}
	}
}
