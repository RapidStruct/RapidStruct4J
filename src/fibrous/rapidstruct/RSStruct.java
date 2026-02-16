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

import java.util.ArrayList;

/**
 * This is the primary class for the RapidStruct system.
 * This is merely a glorified array used to store fields.
 * By default, field capacity starts at 64, but will grow if needed.
 * The starting capacity can also be configured with the appropriate constructor.
 * Generally, I would only reduce the initial field capacity if I know something will only have a few fields.
 * Do your best to reuse an instance of this class if possible!
 */
public class RSStruct {
	RSSchema schema;
	
	int[] schemaKeys;
	RSField[] fields;
	int fieldCount = 0;
	
	int fieldCapacity = 64;
	
	/**
	 * Creates an RSStruct with a specified schema.
	 * The starting field capacity is 64.
	 * 
	 * @param schema
	 */
	public RSStruct(RSSchema schema) {
		this.schema = schema;
		
		schemaKeys = new int[fieldCapacity];
		fields = new RSField[fieldCapacity];
	}
	
	/**
	 * Creates a RSStruct with specified schema and configurable maximum fields and write write/serialization buffer size.
	 * The default starting field capacity is 64, but it is configurable here.
	 * 
	 * @param schema
	 * @param fieldCapacity
	 * @param writeBufferSize
	 */
	public RSStruct(RSSchema schema, int fieldCapacity) {
		this.schema = schema;
		
		this.fieldCapacity = fieldCapacity;
		schemaKeys = new int[fieldCapacity];
		fields = new RSField[fieldCapacity];
	}
	
	/**
	 * Resets this struct's fieldCount to 0, which functionally (not literally) removes all data from this struct.
	 */
	public void reset() {
		fieldCount = 0;
	}
	
	/**
	 * Puts the field into the field array and expands the array as needed
	 * @param field
	 * @param schemaKey
	 */
	void storeField(RSField field, int schemaKey) {
		if(fieldCount == fieldCapacity)
			expand();
		
		fields[fieldCount] = field;
		schemaKeys[fieldCount] = schemaKey;
		fieldCount++;
	}
	
	void expand() {	
		fieldCapacity *= 2;
		int[] newSchemaKeys = new int[fieldCapacity];
		RSField[] newFields = new RSField[fieldCapacity];
		
		System.arraycopy(schemaKeys, 0, newSchemaKeys, 0, schemaKeys.length);
		System.arraycopy(fields, 0, newFields, 0, fields.length);
		schemaKeys = newSchemaKeys;
		fields = newFields;
	}
	
	/**
	 * Takes the passed field and adds it into this struct with the given tag.
	 * Throws an exception if no definition for the tag is found in this struct's schema.
	 * 
	 * @param tag
	 * @param field
	 */
	public void add(String tag, RSField field) {
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			throw new RuntimeException("Schema definition for tag " + tag + " does not exist");

		if(schema.fieldTypes[schemaKey] != field.type)
			throw new AssertionError("RSField of type " + field.type + " was added under type " + schema.fieldTypes[schemaKey]);
		
		storeField(field, schemaKey);
	}

	/**
	 * Takes the passed field and adds it into this struct with the given schema key.
	 * Throws an exception if no definition for the schema key is found in this struct's schema.
	 * 
	 * @param schemaKey
	 * @param field
	 */
	public void add(int schemaKey, RSField field) {
		if(schemaKey == -1)
			throw new RuntimeException("Schema key is invalid");
		
		if(schema.fieldTypes[schemaKey] != field.type)
			throw new AssertionError("RSField of type " + field.type + " was added under type " + schema.fieldTypes[schemaKey]);

		storeField(field, schemaKey);
	}
	
	/**
	 * Takes the passed boolean and adds it into this struct with the given tag.
	 * Throws an exception if no definition for the tag is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the tag is not a bool.
	 * 
	 * @param tag
	 * @param value
	 */
	public void addBool(String tag, boolean value) {
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			throw new RuntimeException("Schema definition for tag " + tag + " does not exist");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putBool(value);
		
		storeField(field, schemaKey);
	}

	/**
	 * Takes the passed boolean and adds it into this struct with the given schema key.
	 * Throws an exception if no definition for the schema key is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the key is not a bool.
	 * 
	 * @param schemaKey
	 * @param value
	 */
	public void addBool(int schemaKey, boolean value) {
		if(schemaKey == -1)
			throw new RuntimeException("Schema key is invalid");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putBool(value);
		
		storeField(field, schemaKey);
	}
	
	/**
	 * Takes the passed byte and adds it into this struct with the given tag.
	 * Throws an exception if no definition for the tag is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the tag is not a byte.
	 * 
	 * @param tag
	 * @param value
	 */
	public void addByte(String tag, byte value) {
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			throw new RuntimeException("Schema definition for tag " + tag + " does not exist");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putByte(value);
		
		storeField(field, schemaKey);
	}

	/**
	 * Takes the passed byte and adds it into this struct with the given schema key.
	 * Throws an exception if no definition for the schema key is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the key is not a byte.
	 * 
	 * @param schemaKey
	 * @param value
	 */
	public void addByte(int schemaKey, byte value) {
		if(schemaKey == -1)
			throw new RuntimeException("Schema key is invalid");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putByte(value);
		
		storeField(field, schemaKey);
	}
	
	/**
	 * Takes the passed short and adds it into this struct with the given tag.
	 * Throws an exception if no definition for the tag is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the tag is not a short.
	 * 
	 * @param tag
	 * @param value
	 */
	public void addShort(String tag, short value) {
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			throw new RuntimeException("Schema definition for tag " + tag + " does not exist");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putShort(value);
		
		storeField(field, schemaKey);
	}

	/**
	 * Takes the passed short and adds it into this struct with the given schema key.
	 * Throws an exception if no definition for the schema key is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the key is not a short.
	 * 
	 * @param schemaKey
	 * @param value
	 */
	public void addShort(int schemaKey, short value) {
		if(schemaKey == -1)
			throw new RuntimeException("Schema key is invalid");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putShort(value);
		
		storeField(field, schemaKey);
	}
	
	/**
	 * Takes the passed int and adds it into this struct with the given tag.
	 * Throws an exception if no definition for the tag is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the tag is not a int.
	 * 
	 * @param tag
	 * @param value
	 */
	public void addInt(String tag, int value) {
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			throw new RuntimeException("Schema definition for tag " + tag + " does not exist");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putInt(value);
		
		storeField(field, schemaKey);
	}

	/**
	 * Takes the passed int and adds it into this struct with the given schema key.
	 * Throws an exception if no definition for the schema key is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the key is not a int.
	 * 
	 * @param schemaKey
	 * @param value
	 */
	public void addInt(int schemaKey, int value) {
		if(schemaKey == -1)
			throw new RuntimeException("Schema key is invalid");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putInt(value);
		
		storeField(field, schemaKey);
	}
	
	/**
	 * Takes the passed long and adds it into this struct with the given tag.
	 * Throws an exception if no definition for the tag is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the tag is not a long.
	 * 
	 * @param tag
	 * @param value
	 */
	public void addLong(String tag, long value) {
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			throw new RuntimeException("Schema definition for tag " + tag + " does not exist");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putLong(value);
		
		storeField(field, schemaKey);
	}

	/**
	 * Takes the passed long and adds it into this struct with the given schema key.
	 * Throws an exception if no definition for the schema key is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the key is not a long.
	 * 
	 * @param schemaKey
	 * @param value
	 */
	public void addLong(int schemaKey, long value) {
		if(schemaKey == -1)
			throw new RuntimeException("Schema key is invalid");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putLong(value);
		
		storeField(field, schemaKey);
	}
	
	/**
	 * Takes the passed float and adds it into this struct with the given tag.
	 * Throws an exception if no definition for the tag is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the tag is not a float.
	 * 
	 * @param tag
	 * @param value
	 */
	public void addFloat(String tag, float value) {
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			throw new RuntimeException("Schema definition for tag " + tag + " does not exist");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putFloat(value);
		
		storeField(field, schemaKey);
	}

	/**
	 * Takes the passed float and adds it into this struct with the given schema key.
	 * Throws an exception if no definition for the schema key is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the key is not a float.
	 * 
	 * @param schemaKey
	 * @param value
	 */
	public void addFloat(int schemaKey, float value) {
		if(schemaKey == -1)
			throw new RuntimeException("Schema key is invalid");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putFloat(value);
		
		storeField(field, schemaKey);
	}
	
	/**
	 * Takes the passed double and adds it into this struct with the given tag.
	 * Throws an exception if no definition for the tag is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the tag is not a double.
	 * 
	 * @param tag
	 * @param value
	 */
	public void addDouble(String tag, double value) {
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			throw new RuntimeException("Schema definition for tag " + tag + " does not exist");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putDouble(value);
		
		storeField(field, schemaKey);
	}

	/**
	 * Takes the passed double and adds it into this struct with the given schema key.
	 * Throws an exception if no definition for the schema key is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the key is not a double.
	 * 
	 * @param schemaKey
	 * @param value
	 */
	public void addDouble(int schemaKey, double value) {
		if(schemaKey == -1)
			throw new RuntimeException("Schema key is invalid");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putDouble(value);
		
		storeField(field, schemaKey);
	}
	
	/**
	 * Takes the passed bytes and adds it into this struct with the given tag.
	 * Throws an exception if no definition for the tag is found in this struct's schema.
	 * The primary usage for this method is to pass byte arrays and the type definition for specified tag should generally be RAW.
	 * However, this method DOES NOT throw an AssertionError regardless of the definition for the tag and assumes you know what you are doing.
	 * 
	 * @param tag
	 * @param value
	 */
	public void addBytes(String tag, byte[] value) {
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			throw new RuntimeException("Schema definition for tag " + tag + " does not exist");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putBytes(value);
		
		storeField(field, schemaKey);
	}

	/**
	 * Takes the passed bytes and adds it into this struct with the given schema key.
	 * Throws an exception if no definition for the schema key is found in this struct's schema.
	 * The primary usage for this method is to pass byte arrays and the type definition for specified key should generally be RAW.
	 * However, this method DOES NOT throw an AssertionError regardless of the definition for the tag and assumes you know what you are doing.
	 * 
	 * @param schemaKey
	 * @param value
	 */
	public void addBytes(int schemaKey, byte[] value) {
		if(schemaKey == -1)
			throw new RuntimeException("Schema key is invalid");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putBytes(value);
		
		storeField(field, schemaKey);
	}
	
	/**
	 * Takes the passed String and adds it into this struct with the given tag.
	 * Throws an exception if no definition for the tag is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the tag is not a String.
	 * 
	 * @param tag
	 * @param value
	 */
	public void addString(String tag, String value) {
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			throw new RuntimeException("Schema definition for tag " + tag + " does not exist");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putString(value);
		
		storeField(field, schemaKey);
	}

	/**
	 * Takes the passed String and adds it into this struct with the given schema key.
	 * Throws an exception if no definition for the schema key is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the key is not a String.
	 * 
	 * @param schemaKey
	 * @param value
	 */
	public void addString(int schemaKey, String value) {
		if(schemaKey == -1)
			throw new RuntimeException("Schema key is invalid");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putString(value);
		
		storeField(field, schemaKey);
	}
	
	/**
	 * Takes the passed struct and adds it into this struct with the given tag.
	 * Throws an exception if no definition for the tag is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the tag is not a struct.
	 * 
	 * @param tag
	 * @param value
	 */
	public void addStruct(String tag, RSStruct value) {
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			throw new RuntimeException("Schema definition for tag " + tag + " does not exist");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putStruct(value);
		
		storeField(field, schemaKey);
	}

	/**
	 * Takes the passed struct and adds it into this struct with the given schema key.
	 * Throws an exception if no definition for the schema key is found in this struct's schema.
	 * Throws an AssertionError if the type that is defined for the key is not a struct.
	 * 
	 * @param schemaKey
	 * @param value
	 */
	public void addStruct(int schemaKey, RSStruct value) {
		if(schemaKey == -1)
			throw new RuntimeException("Schema key is invalid");
		
		RSField field = new RSField(schema.fieldTypes[schemaKey]);
		field.putStruct(value);
		
		storeField(field, schemaKey);
	}
	
	/**
	 * Returns the first field that has a matching tag.
	 * Returns null if there are currently no fields in this struct with the given tag.
	 * @param tag
	 * @return
	 */
	public RSField get(String tag) {
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			return null;
		
		for(int i = 0; i < fieldCount; i++) {
			if(schemaKeys[i] == schemaKey)
				return fields[i];
		}
		
		return null;
	}
	
	/**
	 * Returns the first field that has a matching schema key.
	 * Returns null if there are currently no fields in this struct with the given key/index.
	 * @param tag
	 * @return
	 */
	public RSField get(int schemaKey) {
		for(int i = 0; i < fieldCount; i++) {
			if(schemaKeys[i] == schemaKey)
				return fields[i];
		}
		
		return null;
	}
	
	/**
	 * Returns an ArrayList containing all fields that match the passed tag.
	 * Returns an empty ArrayList if there are currently no fields with the given tag.
	 * @param tag
	 * @return
	 */
	public ArrayList<RSField> getAllFieldsWithTag(String tag) {
		ArrayList<RSField> fieldList = new ArrayList<>();
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			return fieldList;
		
		for(int i = 0; i < fieldCount; i++) {
			if(schemaKeys[i] == schemaKey)
				fieldList.add(fields[i]);
		}
		
		return fieldList;
	}
	
	/**
	 * Returns an ArrayList containing all fields that match the passed schema key.
	 * Returns an empty ArrayList if there are currently no fields with the given key.
	 * @param tag
	 * @return
	 */
	public ArrayList<RSField> getAllFieldsWithKey(int schemaKey) {
		ArrayList<RSField> fieldList = new ArrayList<>();
		for(int i = 0; i < fieldCount; i++) {
			if(schemaKeys[i] == schemaKey)
				fieldList.add(fields[i]);
		}
		
		return fieldList;
	}
	
	/**
	 * Returns true if this struct contains a field with the given tag.
	 * @param tag
	 * @return
	 */
	public boolean hasField(String tag) {
		int schemaKey = schema.getSchemaKey(tag);
		if(schemaKey == -1)
			return false;
		
		for(int i = 0; i < fieldCount; i++) {
			if(schemaKeys[i] == schemaKey)
				return true;
		}
		
		return false;
	}

	/**
	 * Returns true if this struct contains a field with the given schema key.
	 * @param schemaKey
	 * @return
	 */
	public boolean hasField(int schemaKey) {
		for(int i = 0; i < fieldCount; i++) {
			if(schemaKeys[i] == schemaKey)
				return true;
		}
		
		return false;
	}
}