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

/**
 * This class is used to describe the types of fields that can be stored within a {@link RSStruct}.
 * There can be a maximum of 256 unique field tags as they are all represented as 1-byte keys.
 * The type can be a range of primitives, Strings, raw bytes, or other RSStructs.
 * The tag is merely a textual key that can be used as a lookup to find a particular field.
 * <p>
 * NOTE: While a RSSchema can only have one definition for a specific tag, an RSStruct may have multiple entries for that field, meaning that a search for that tag may give multiple results, but they will all be the same type (int, double, raw bytes, etc...)
 */
public class RSSchema {
	//These are all accessed via a numerical key which is simply an index into the array.
	//A textual tag is also associated with the keys in order to provide human-friendly means of access.
	
	RSFieldType[] fieldTypes;
	String[] fieldTags;
	RSSchema[] nestedSchemas;
	int fieldCount = 0;
	
	public RSSchema() {
		fieldTypes = new RSFieldType[256];
		fieldTags = new String[256];
		nestedSchemas = new RSSchema[256];
	}
	
	/**
	 * Adds a field to the schema with the given type and tag.
	 * Returns the schema key/index for this field.
	 * Throws an exception if the passed tag has already been defined.
	 * @param type
	 * @param tag
	 * @return
	 */
	public int addFieldToSchema(String tag, RSFieldType type) {
		if(type == RSFieldType.STRUCT)
			throw new Error("A type of " + type.name() + " must be added with it's schema.  Use addStructToSchema(String, Schema) instead");
		
		for(int i = 0; i < fieldCount; i++) {
			if(tag.equals(fieldTags[i]))
				throw new Error("RSSchema tag " + tag + " has already been defined");
		}
		
		fieldTypes[fieldCount] = type;
		fieldTags[fieldCount] = tag;
		return fieldCount++;
	}
	
	/**
	 * Adds a struct to the schema with the given tag.
	 * Returns the schema key/index for this field.
	 * Throws an exception if the passed tag has already been defined.
	 * @param type
	 * @param tag
	 * @return
	 */
	public int addStructToSchema(String tag, RSSchema schema) {
		for(int i = 0; i < fieldCount; i++) {
			if(tag.equals(fieldTags[i]))
				throw new Error("RSSchema tag " + tag + " has already been defined");
		}
		
		fieldTypes[fieldCount] = RSFieldType.STRUCT;
		fieldTags[fieldCount] = tag;
		nestedSchemas[fieldCount] = schema;
		return fieldCount++;
	}
	
	/**
	 * Returns the schema key/index for a given tag.
	 * Returns -1 if that tag does not exist.
	 * @param tag
	 * @return
	 */
	public int getSchemaKey(String tag) {
		for(int i = 0; i < fieldCount; i++) {
			if(tag.equals(fieldTags[i]))
				return i;
		}
		
		return -1;
	}
}