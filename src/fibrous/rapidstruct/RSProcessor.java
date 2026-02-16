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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class is how you ultimately write/read RSStructs to/from bytes.
 * While this class is thread-safe, it is recommended that you keep an instantiation's use limited to one thread, or possibly put into a pool.
 * This class uses an array as a buffer, so a lock has to be acquired before reading/writing to prevent buffer corruption from concurrent reads/writes.
 * You can instantiate multiple of these in order to process things in parallel.
 */
public class RSProcessor {
	
	RSByteStreamWriter writeStream;
	RSByteStreamReader readStream;
	
	public RSProcessor() {
		writeStream = new RSByteStreamWriter();
		readStream = new RSByteStreamReader();
	}
	
	public RSProcessor(int startingWriteBufferSize) {
		writeStream = new RSByteStreamWriter(startingWriteBufferSize);
		readStream = new RSByteStreamReader();
	}
	
	/**
	 * Takes this RSStruct and serializes it into a byte array.
	 * 
	 * @return
	 */
	public synchronized byte[] writeBytes(RSStruct struct) {
		writeStream.pushMark();
		
		for(int i = 0; i < struct.fieldCount; i++) {
			//Write the index/tag first
			int schemaKey = struct.schemaKeys[i];
			RSField field = struct.fields[i];
			writeStream.write(schemaKey);
			
			//Determine if it's a variable length field or not
			//Because if that's the case, we need to specify the length
			if(field.type == RSFieldType.STRING || field.type == RSFieldType.RAW || field.type == RSFieldType.STRUCT) {
				//Write the length of the data next
				if(field.type == RSFieldType.STRUCT ) {
					//We have to actually serialize to bytes first to get the length for a struct.
					byte[] structBytes = writeBytes(field.struct);
					field.putBytes(structBytes);
				}
				
				int dataLength = field.length;
				if(dataLength > 65535) {
					//throw something here because we are only sending 2-byte lengths
					throw new RuntimeException("Exceeded maximum RapidStruct field length");
				}
				writeStream.write((byte) ((dataLength & 0xFFFF) >>> 8));
				writeStream.write((byte) (dataLength & 0xFF));
			}
			
			//Write the actual data
			writeStream.write(field.asBytes());
		}
		
		byte[] bytes = writeStream.getTrimmedBufferCopy();
		writeStream.goToLastMark();
		writeStream.popMark();
		
		return bytes;
	}
	
	/**
	 * Takes a byte array and deserializes it into the passed RSStruct.
	 * 
	 * @param rawData
	 */	
	public synchronized void readBytes(byte[] rawData, RSStruct struct) {
		struct.reset();
		readStream.pushBytes(rawData);
		
		while(readStream.numBytesRemaining() > 0) {
			//Get schema key/index
			int schemaKey = readStream.read();
			RSField field = new RSField(struct.schema.fieldTypes[schemaKey]);
			struct.add(struct.schema.fieldTags[schemaKey], field);
			RSSchema nestedSchema = null;
			
			int length = 0;
			
			switch(field.type) {
			case BOOL:
			case BYTE:
				length = 1;
				break;
				
			case SHORT:
				length = 2;
				break;
				
			case INT:
			case FLOAT:
				length = 4;
				break;
				
			case LONG:
			case DOUBLE:
				length = 8;
				break;
				
			case STRING:
			case RAW:
			{
				//Get the length
				length = readStream.read() << 8;
				length += readStream.read();
				break;
			}
			
			case STRUCT:
			{
				//Get the length
				length = readStream.read() << 8;
				length += readStream.read();
				
				//Retrieve the nested schema
				nestedSchema = struct.schema.nestedSchemas[schemaKey];
			}
			}
			
			byte[] rawValue = new byte[length];
			int bytesRead = readStream.readBytes(rawValue);
			if(bytesRead < length) {
				throw new RuntimeException("Incomplete RapidStruct byte stream");
			}
			
			if(nestedSchema == null)
				field.putBytes(rawValue);
			else {
				RSStruct nestedStruct = new RSStruct(nestedSchema);
				readBytes(rawValue, nestedStruct);
				field.putStruct(nestedStruct);
			}
		}
		
		readStream.popBytes();
	}
	
	/**
	 * Convenience function to dump the contents of struct to a string.
	 */
	public static String dumpRSStruct(RSStruct struct, int nesting) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(int i = 0; i < struct.fieldCount; i++) {
			RSField field = struct.fields[i];
			int schemaKey = struct.schemaKeys[i];
			String tag = struct.schema.fieldTags[schemaKey];
			String type = field.type.name();
			
			String tabs = "";
			for(int t = 0; t < nesting; t++) {
				tabs += "\t";
			}
			
			try {
				
				baos.write((tabs + "Tag: " + tag).getBytes());
				baos.write((", SchemaKey: " + schemaKey).getBytes());
				baos.write((", Type: " + type).getBytes());
				baos.write((", Value: ").getBytes());
				switch(field.type) {
				case BOOL: {
					String v = String.valueOf(field.asBool());
					baos.write(v.getBytes());
					break;
				}
				
				case BYTE: {
					String v = String.valueOf(field.asByte());
					baos.write(v.getBytes());
					break;
				}
				
				case SHORT: {
					String v = String.valueOf(field.asShort());
					baos.write(v.getBytes());
					break;
				}
				
				case INT: {
					String v = String.valueOf(field.asInt());
					baos.write(v.getBytes());
					break;
				}
				
				case LONG: {
					String v = String.valueOf(field.asLong());
					baos.write(v.getBytes());
					break;
				}
				
				case FLOAT: {
					String v = String.valueOf(field.asFloat());
					baos.write(v.getBytes());
					break;
				}
				
				case DOUBLE: {
					String v = String.valueOf(field.asDouble());
					baos.write(v.getBytes());
					break;
				}
				
				case STRING: {
					baos.write(field.asString().getBytes());
					break;
				}
				
				case RAW: {
					baos.write(byteArrayToTextNumbers(field.asBytes()).getBytes());
					break;
				}
				
				case STRUCT: {
					baos.write("\n".getBytes());
					String structString = dumpRSStruct(field.asStruct(), nesting + 1);
					baos.write(structString.getBytes());
					break;
				}
				}
				
				baos.write("\n".getBytes());
			} catch (IOException e) {}
		}
		
		return baos.toString();
	}
	
	private static String byteArrayToTextNumbers(byte[] bytes) {
		String s = "";
		for(int i = 0; i < bytes.length; i++) {
			s += 0xFF & (int) bytes[i];
			if(i < bytes.length - 1)
				s += ",";
		}
		return s;
	}
}

class RSByteStreamWriter {

	byte[] buffer;
	int currentPos = 0;
	
	int[] marks;
	int currentMark = -1;
	
	final static int EXPANSION_INCREMENT = 4096;
	
	public RSByteStreamWriter() {
		buffer = new byte[EXPANSION_INCREMENT];
		marks = new int[1024];
	}
	
	/**
	 * Constructor that initializes the buffer with a specified size.
	 * That means you must know what your bounds are prior to using this or else you will throw exceptions.
	 * @param size
	 */
	public RSByteStreamWriter(int size) {
		buffer = new byte[size];
		marks = new int[1024];
	}
	
	/**
	 * Resets this objects array pointer/indexer to zero.
	 */
	public void reset() {
		currentPos = 0;
		currentMark = -1;
	}
	
	/**
	 * Returns how many bytes have been written to this ByteStream.
	 * The context of this method is if you are treating this object as an array you are writing to and want to know how many bytes you have written.
	 * @return
	 */
	public int bytesWritten() {
		return currentPos;
	}
	
	/**
	 * Returns how large the buffer is.
	 * This is not the same as how many bytes have been written.
	 * This is purely returns buffer.length
	 * @return
	 */
	public int getBufferSize() {
		return buffer.length;
	}
	
	/**
	 * Returns the number of bytes remaining if this ByteStream is treated like a read/input stream.
	 * @return
	 */
	public int numBytesRemaining() {
		return buffer.length - currentPos;
	}
	
	/**
	 * Adds a new position marker to this ByteStream.
	 */
	public void pushMark() {
		currentMark++;
		marks[currentMark] = currentPos;
	}
	
	/**
	 * Removes the last position marker from this ByteStream.
	 */
	public void popMark() {
		currentMark--;
	}
	
	/**
	 * Moves the read/write indexer to the last recorded mark.
	 */
	public void goToLastMark() {
		currentPos = marks[currentMark];
	}
	
	private void expandBuffer(int nextWriteSize) {
		int remainingCapacity = buffer.length - currentPos;
		int overflow = nextWriteSize - remainingCapacity;
		
		int expansionFactor = overflow / EXPANSION_INCREMENT;
		if(expansionFactor < 1)
			expansionFactor = 1;
		else {
			int carry = overflow % EXPANSION_INCREMENT;
			if(carry > 1)
				expansionFactor++;
		}
		
		byte[] newBuffer = new byte[buffer.length + (expansionFactor * EXPANSION_INCREMENT)];
		System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
		buffer = newBuffer;
	}
	
	/**
	 * Writes one byte to this ByteStream, expanding the internal buffer as necessary..
	 */
	public void write(int b) {
		if(currentPos + 1 >= buffer.length)
			expandBuffer(1);
		
		buffer[currentPos] = (byte) b;
		currentPos++;
	}
	
	/**
	 * Writes a byte array to this ByteStream by copying it (System.arraycopy) to this ByteStream's internal buffer, expanding the internal buffer as necessary.
	 * @param array
	 */
	public void write(byte[] array) {
		if(currentPos + array.length >= buffer.length)
			expandBuffer(array.length);
			
		System.arraycopy(array, 0, buffer, currentPos, array.length);
		currentPos += array.length;
	}
	
	/**
	 * Returns this internal buffer without copying it (the actual reference).
	 * @return
	 */
	public byte[] getBackingBuffer() {
		return buffer;
	}
	
	/**
	 * Returns a copy of the buffer from whatever has currently been written from the last mark.
	 * The length of the copy is trimmed to however many bytes have been written.
	 * @return
	 */
	public byte[] getTrimmedBufferCopy() {
		byte[] copy = new byte[currentPos - marks[currentMark]];
		System.arraycopy(buffer, marks[currentMark], copy, 0, copy.length);
		return copy;
	}
}

class RSByteStreamReader {

	byte[] buffer;
	int currentPos = 0;
	
	int[] marks;
	int currentMarkIndex = -1;
	
	int[] lengths;
	int currentLengthIndex = 0;
	
	final static int EXPANSION_INCREMENT = 4096;
	
	public RSByteStreamReader() {
		buffer = new byte[EXPANSION_INCREMENT];
		marks = new int[1024];
		lengths = new int[1024];
	}
	
	/**
	 * Resets this objects array pointer/indexer to zero.
	 */
	public void reset() {
		currentPos = 0;
		currentMarkIndex = -1;
		lengths[0] = 0;
		currentLengthIndex = 0;
	}
	
	/**
	 * Returns how many bytes have been written to this ByteStream.
	 * The context of this method is if you are treating this object as an array you are writing to and want to know how many bytes you have written.
	 * @return
	 */
	public int bytesWritten() {
		return currentPos;
	}
	
	/**
	 * Returns how large the buffer is.
	 * This is not the same as how many bytes have been written.
	 * This is purely returns buffer.length
	 * @return
	 */
	public int getBufferSize() {
		return buffer.length;
	}
	
	/**
	 * Returns the number of bytes remaining if this ByteStream is treated like a read/input stream.
	 * @return
	 */
	public int numBytesRemaining() {
		return lengths[currentLengthIndex] - currentPos;
	}
	
	private void expandBuffer(int nextWriteSize) {
		int remainingCapacity = buffer.length - currentPos;
		int overflow = nextWriteSize - remainingCapacity;
		
		int expansionFactor = overflow / EXPANSION_INCREMENT;
		if(expansionFactor < 1)
			expansionFactor = 1;
		else {
			int carry = overflow % EXPANSION_INCREMENT;
			if(carry > 1)
				expansionFactor++;
		}
		
		byte[] newBuffer = new byte[buffer.length + (expansionFactor * EXPANSION_INCREMENT)];
		System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
		buffer = newBuffer;
	}
	
	/**
	 * Pushes a byte array to the end of this ByteStream by copying it (System.arraycopy), expanding the internal buffer as necessary.
	 * Finally, the read indexer is moved to the beginning of the newly pushed byte array.
	 * @param array
	 */
	public void pushBytes(byte[] array) {
		if(currentPos + array.length >= buffer.length)
			expandBuffer(array.length);
		
		currentMarkIndex++;
		marks[currentMarkIndex] = currentPos;
		
		int oldLength = lengths[currentLengthIndex];
		System.arraycopy(array, 0, buffer, oldLength, array.length);
		currentPos = oldLength;
		
		currentLengthIndex++;
		lengths[currentLengthIndex] = oldLength + array.length;
	}
	
	/**
	 * Restores the read indexer to the last read mark and restores the total length to be it's previous value.
	 */
	public void popBytes() {
		currentPos = marks[currentMarkIndex];
		currentMarkIndex--;
		currentLengthIndex--;
	}
	
	/**
	 * Reads the next byte from this ByteStream.
	 * WARNING! This method does not perform any bounds checking and may throw an exception for overflow or out-of-bounds indexing.
	 * @return
	 */
	public int read() {
		int i = buffer[currentPos] & 0xff;
		currentPos++;
		return i;
	}
	
	/**
	 * Fills the passed byte array with the next set of bytes that are available to read.
	 * This method DOES perform bounds checking and will only copy data that is available to read.
	 * @return The amount of bytes read
	 */
	public int readBytes(byte[] array) {
		int bytesLeft = numBytesRemaining();
		int bytesRead = array.length < bytesLeft ? array.length : bytesLeft;
		System.arraycopy(buffer, currentPos, array, 0, bytesRead);
		currentPos += bytesRead;
		return bytesRead;
	}
	
	/**
	 * Returns a new byte array that contains the remaining bytes from this ByteStream.
	 * This will return an array of length 0 if there are no bytes left to read.
	 * @return
	 */
	public byte[] readRemainingBytes() {
		int bytesToRead = numBytesRemaining();
		byte[] remainingBytes = new byte[bytesToRead];
		System.arraycopy(buffer, currentPos, remainingBytes, 0, bytesToRead);
		currentPos += bytesToRead;
		return remainingBytes;
	}
	
	/**
	 * Returns a byte array up to the length passed from the amount of bytes left in this ByteStream.
	 * @param count
	 * @return
	 */
	public byte[] readNBytes(int count) {
		int bytesLeft = numBytesRemaining();
		int bytesRead = count < bytesLeft ? count : bytesLeft;
		byte[] array = new byte[bytesRead];
		System.arraycopy(buffer, currentPos, array, 0, bytesRead);
		currentPos += bytesRead;
		return array;
	}
}
