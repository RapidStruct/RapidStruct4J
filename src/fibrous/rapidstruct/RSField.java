/*
 * MIT License
 *
 * Copyright (c) 2026 Noah McLean
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fibrous.rapidstruct;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * This is the value holder for a RSStruct.
 * This can be a variety of types, such as primitives, strings, and other RSStructs.
 */
public class RSField {
	public ByteBuffer buffer;
	public RSFieldType type;
	public int length = 0;
	
	//This one is only used when this RSField represents an RSStruct
	RSStruct struct;
	
	public RSField(RSFieldType type) {
		this.type = type;
		
		setInitialAllocation(type);
	}
	
	/*
	 * This only used by the constructor.
	 * I initially wrote multiple constructors
	 * That would call this, but as of now
	 * there is only one.  Might just inline
	 * this back into the constructor
	 */
	private void setInitialAllocation(RSFieldType type) {
		switch(type) {
		case BOOL:
		case BYTE:
			buffer = ByteBuffer.allocate(1);
			break;
		case SHORT:
			buffer = ByteBuffer.allocate(2);
			break;
		case INT:
			buffer = ByteBuffer.allocate(4);
			break;
		case LONG:
			buffer = ByteBuffer.allocate(8);
			break;
		case FLOAT:
			buffer = ByteBuffer.allocate(4);
			break;
		case DOUBLE:
			buffer = ByteBuffer.allocate(8);
			break;
		case STRING:
		case RAW:
		case STRUCT:
			buffer = ByteBuffer.allocate(256);
			break;
		}
	}
	
	/**
	 * This treats this RSField as a bool and places the value of a bool into this RSField's buffer.
	 * Throws an AssertionError if this field is not a bool.
	 * @param value
	 */
	public void putBool(boolean value) {
		if(type != RSFieldType.BOOL)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.BOOL);
		
		buffer.clear();
		if(value)
			buffer.put((byte) 1);
		else
			buffer.put((byte) 0);
		
		length = 1;
	}
	
	/**
	 * This treats this RSField as a byte and fills this RSField's buffer with the value of a byte.
	 * Throws an AssertionError if this field is not a byte.
	 * @param value
	 */
	public void putByte(byte value) {
		if(type != RSFieldType.BYTE)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.BYTE);
		
		buffer.clear();
		buffer.put(value);
		length = 1;
	}
	
	/**
	 * This treats this RSField as a short and fills this RSField's buffer with the value of a short.
	 * Throws an AssertionError if this field is not a short.
	 * @param value
	 */
	public void putShort(short value) {
		if(type != RSFieldType.SHORT)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.SHORT);
		
		buffer.clear();
		buffer.putShort(value);
		length = 2;
	}
	
	/**
	 * This treats this RSField as an int and fills this RSField's buffer with the value of an int.
	 * Throws an AssertionError if this field is not an int.
	 * @param value
	 */
	public void putInt(int value) {
		if(type != RSFieldType.INT)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.INT);
		
		buffer.clear();
		buffer.putInt(value);
		length = 4;
	}
	
	/**
	 * This treats this RSField as a long and fills this RSField's buffer with the value of a long.
	 * Throws an AssertionError if this field is not a long.
	 * @param value
	 */
	public void putLong(long value) {
		if(type != RSFieldType.LONG)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.LONG);
		
		buffer.clear();
		buffer.putLong(value);
		length = 8;
	}
	
	/**
	 * This treats this RSField as a float and fills this RSField's buffer with the value of a float.
	 * Throws an AssertionError if this field is not a float.
	 * @param value
	 */
	public void putFloat(float value) {
		if(type != RSFieldType.FLOAT)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.FLOAT);
		
		buffer.clear();
		buffer.putFloat(value);
		length = 4;
	}
	
	/**
	 * This treats this RSField as a double and fills this RSField's buffer with the value of a double.
	 * Throws an AssertionError if this field is not a double.
	 * @param value
	 */
	public void putDouble(double value) {
		if(type != RSFieldType.DOUBLE)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.DOUBLE);
		
		buffer.clear();
		buffer.putDouble(value);
		length = 8;
	}
	
	/**
	 * This treats this RSField as a String and fills this RSField's buffer with the value of a String.
	 * Throws an AssertionError if this field is not a String.
	 * @param value
	 */
	public void putString(String value) {
		if(type != RSFieldType.STRING)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.STRING);
		
		buffer.clear();
		if(value.length() > buffer.capacity())
			buffer = ByteBuffer.allocate(buffer.capacity() * 2);
			
		buffer.put(value.getBytes(StandardCharsets.UTF_8));
		length = buffer.position();
	}
	
	/**
	 * This treats this RSField as a byte array and fills this RSField's buffer with the byte array.
	 * This is the only method that WILL NEVER throw an AssertionError.
	 * It assumes that you know what you are doing if you call this.
	 * It will also increase it's buffer size to accommodate whatever array you pass if needed, but when read back will only return as many bytes as was written.
	 * Of course, most of this length/sizing stuff goes out the window when the RSField is serialized.
	 * @param value
	 */
	public void putBytes(byte[] value) {
		buffer.clear();
		if(value.length > buffer.capacity())
			buffer = ByteBuffer.allocate(value.length);
			
		buffer.put(value);
		length = buffer.position();
	}
	
	/**
	 * This treats this RSField as a struct and fills this RSField's buffer with the data from a struct.
	 * Throws an AssertionError if this field is not a struct.
	 * @param value
	 */
	public void putStruct(RSStruct value) {
		if(type != RSFieldType.STRUCT)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.STRUCT);
		
		buffer.clear();
		struct = value;
	}
	
	/**
	 * Returns the value from the buffer representing a bool.
	 * Throws an AssertionError if this field is not a bool.
	 * @return
	 */
	public boolean asBool() {
		if(type != RSFieldType.BOOL)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.BOOL);
		
		buffer.rewind();
		byte value = buffer.get();
		
		return value == 1;
	}
	
	/**
	 * Returns the value from the buffer representing as a byte.
	 * Throws an AssertionError if this field is not a byte.
	 * @return
	 */
	public byte asByte() {
		if(type != RSFieldType.BYTE)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.BYTE);
		
		buffer.rewind();
		byte value = buffer.get();
		
		return value;
	}
	
	/**
	 * Returns the value from the buffer representing a short.
	 * Throws an AssertionError if this field is not a short.
	 * @return
	 */
	public short asShort() {
		if(type != RSFieldType.SHORT)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.SHORT);
		
		buffer.rewind();
		short value = buffer.getShort();
		
		return value;
	}
	
	/**
	 * Returns the value from the buffer representing an int.
	 * Throws an AssertionError if this field is not an int.
	 * @return
	 */
	public int asInt() {
		if(type != RSFieldType.INT)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.INT);
		
		buffer.rewind();
		int value = buffer.getInt();
		
		return value;
	}
	
	/**
	 * Returns the value from the buffer representing a long.
	 * Throws an AssertionError if this field is not a long.
	 * @return
	 */
	public long asLong() {
		if(type != RSFieldType.LONG)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.LONG);
		
		buffer.rewind();
		long value = buffer.getLong();
		
		return value;
	}
	
	/**
	 * Returns the value from the buffer representing a float.
	 * Throws an AssertionError if this field is not a float.
	 * @return
	 */
	public float asFloat() {
		if(type != RSFieldType.FLOAT)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.FLOAT);
		
		buffer.rewind();
		float value = buffer.getFloat();
		
		return value;
	}
	
	/**
	 * Returns the value from the buffer representing a double.
	 * Throws an AssertionError if this field is not a double.
	 * @return
	 */
	public double asDouble() {
		if(type != RSFieldType.DOUBLE)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.DOUBLE);
		
		buffer.rewind();
		double value = buffer.getDouble();
		
		return value;
	}
	
	/**
	 * Returns the value from the buffer representing a String.
	 * Throws an AssertionError if this field is not a String.
	 * @return
	 */
	public String asString() {
		if(type != RSFieldType.STRING)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.STRING);
		
		buffer.rewind();
		byte[] bytes = new byte[length];
		buffer.get(bytes);
		String value = new String(bytes, StandardCharsets.UTF_8);
		return value;
	}
	
	/**
	 * Returns the value from the buffer as a byte array.
	 * This is the only getter method that works with every type and WILL NOT throw an AssertionError.
	 * @return
	 */
	public byte[] asBytes() {
		buffer.rewind();
		byte[] bytes = new byte[length];
		buffer.get(bytes);
		return bytes;
	}
	
	/**
	 * Returns the value from the buffer representing a struct.
	 * Throws an AssertionError if this field is not a struct.
	 * @return
	 */
	public RSStruct asStruct() {
		if(type != RSFieldType.STRUCT)
			throw new AssertionError("RSField of type " + type.name() + " was treated as a " + RSFieldType.STRUCT);
		
		return struct;
	}
}