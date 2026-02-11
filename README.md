# RapidStruct4J

RapidStruct is a bare-bones, schema-based binary serialization format. Read up on the specs for more info on RapidStruct itself. This guide is only concerned with the usage of the Java language library implementation.  For more info on the standard, [go here](https://github.com/RapidStruct/).  To start using this library, I recommend simply copying the files to your source folder.  Below is a quick rundown of the basics.

## Schemas

Because RapidStruct is schema-based, you must have schema's defined. This is done by using an `RSSchema`. A `RSSchema` is simply a collection of types associated to tags and ultimately defines how the data will be represented when serialized. After creation, simply add fields to it:  

~~~
RSSchema subnetSchema = new RSSchema();
subnetSchema.addFieldToSchema("IPV6", RSFieldType.BOOL);
subnetSchema.addFieldToSchema("IPAddress", RSFieldType.RAW);
subnetSchema.addFieldToSchema("CIDR", RSFieldType.BYTE);
subnetSchema.addFieldToSchema("Name", RSFieldType.STRING);
~~~

In this example we created a schema to describe a subnet.  First, we use a `BOOL` type to indicate if this is an IPv4 or IPv6 network. Then we are using a `RAW` type to represent the bytes of the IP address (`RAW` just indicates a byte array). We used the `BYTE` type to indicate the CIDR/mask length. And finally, a `STRING` to represent the name of the subnet. `RAW` is obviously the most flexible type and can essentially be used to represent anything. `STRING` may be platform dependant, but generally speaking should at least support ASCII (the 1st seven bytes of UTF-8 codes is ASCII, for instance.)  **A word of caution:** The RapidStruct standard defines that all integer types are unsigned, and that you do a conversion to get signed values. However, Java does not have unsigned primitives natively. Surprisingly, this problem usually solves itself, but just be aware of what the values are *actually* representing and convert accordingly. Also, depending on the charset, it is possible that Java Strings are not using 1-byte characters.  The RapidStruct standard requires a maximum of 1-byte characters, and that at a minimum they support ASCII.  So if you are exchanging data between different systems, be aware of what both sides are doing. **You have been warned!**

## RSProcessor

The `RSProcessor` is what performs the serialization/deserialization.  You can have many `RSProcessor`s, and I typically recommend that you have one per serialization/deserialization sequence.  That doesn't mean that you can't reuse them (you should), but they are not thread-safe by themselves.  In other words, if there's a specific serialization event that happens repeatedly, that should be handled by a dedicated `RSProcessor`, but don't use a single `RSProcessor` to handle many different serialization events unless the system is very simple and all the events happen in serial.  Here we create an `RSProcessor`:

~~~
RSProcessor proc = new RSProcessor();
//Alternatively, you can call 'new RSProcessor(int startingWriteBufferSize)'
~~~

Not much to it.  The alternative constructor allows you to specificy the initial buffer size that is used during serialization.  However, this is not needed and the buffer will grow as required, but it is there if you need to decrease the amount of buffer resizing and copying events.

## RSStruct

Next is the `RSStruct`.  This is the core of the library and simply holds a common grouping of data.  In this example, I will simply continue to reuse the same `RSStruct` over and over.  I recommend that you do the same unless you have a specific reason not to (E.g., multiple nested `RSStruct`s of an unknown quantity may be difficult to repeatedly reuse).

~~~
RSStruct rsStruct = new RSStruct(subnetSchema);
//Alternatively, call 'new RSStruct(RSSchema scheam, int fieldCapacity);
~~~

Again, this is standard POJO behavior. You just have to specify the schema that will be associated to this `RSStruct`. By default, the struct can hold 64 `RSFields` before having to resize. And again, the alternate constructor allows you to change that if you know you'll be wasting time/memory or know exactly the amount of `RSField`s you'll be storing every time.

## Filling with Data

Now is about the time we can start to do something useful with the `RSStruct`. So let's start filling it with data:
~~~
RSStruct rsStruct = new RSStruct(subnetSchema);
rsStruct.addBool("IPV6", false);
byte[] address = {(byte) 192, (byte) 168, 0, 1};
rsStruct.addBytes("IPAddress", address);
rsStruct.addByte("CIDR", (byte) 24);
rsStruct.addString("Name", "Home network");
~~~

This should be mostly self explanatory. **FYI**, you can add more than one piece of data under the same tag as long as it is the same type. I.e., you could add another raw byte array with the tag "IPAddress", but it would be up to the receiver of the serialized bytes to know to look for an additional byte array with the tag "IPAddress".  Now let's serialize some data!

## Serialization

Now we can take the `RSStruct` and turn it into some raw bytes! To do that, simply call the following:

~~~
byte[] serialBytes = proc.writeBytes(rsStruct);
~~~

That's basically it.  You now have an `RSStruct` in a byte array. After you do something with the bytes (copy, send over the network, etc), you can then clear the RSStruct `with RSStruct.reset()` which removes all its fields and then you can start refilling it, then serializing it, etc...

## Deserialization

Alright, you're on the receiving end of those bytes. What now? the setup is exactly the same as before.  So create and initialize your `RSProcessor` and `RSStruct`. After that is complete, call the following:  

~~~
...
RSProcessor proc = new RSProcessor();
RSStruct rsStruct = new RSStruct(subnetSchema);

byte[] serialBytes = ...(from disk, network, etc);
proc.readBytes(serialBytes, rsStruct);
~~~

That uses the `RSProcessor` to deserialize the bytes passed and fill the passed `RSStruct` with the deserialized data. You can then grab the data:  

~~~
proc.readBytes(serialBytes, rsStruct);
boolean ipv6 = rsStruct.get("IPV6").asBool();
byte[] addresBytes = rsStruct.get("IPAddress").asBytes();
byte cidr = rsStruct.get("CIDR").asByte();
String subnetName = rsStruct.get("Name").asString();
~~~

Now you have some useful data to do things with!

## Nesting an RSStruct

You can also nest an `RSStruct` inside of another `RSStruct`.  That nested `RSStruct` also has to have a schema.  You add it with following method: `RSStruct.addStruct(String tag, RSStruct nestedStruct)`:

~~~
//Create the nested RS_Struct...
RSStruct nestedStruct = new RSStruct(nestedSchema);
rsStruct.addStruct("NestedStructTag", nestedrsStruct);
~~~

And to grab the nested `RSStruct` after deserialization:  

~~~
//Retrieving the nested RSStruct is like any other field
RSStruct nestedrsStruct = outerStruct.get("NestedStructTag").asStruct();
~~~

## Quick Facts

- The maximum number of defined tags in a schema is 256, as they are represented with 1-byte keys.
- You can have an essentially unlimited amount of fields in one RSStruct if they share tags.
- The maximum length of the variable-length field types (`RAW`, `STRING`, and `STRUCT`-which is a nested `RSStruct`) is 65535, as they are prepended with a 2-byte length when serialized.


## Full Serialization Example

Below is a full example of how one might serialize data.

~~~
import fibrous.rapidstruct.*;

public class BirthdaySerializer {
	public static void main(String[]args) {
		RSSchema birthdaySchema = new RSSchema();
		birthdaySchema.addFieldToSchema("Day", RSFieldType.BYTE);
		birthdaySchema.addFieldToSchema("Month", RSFieldType.RAW);
		birthdaySchema.addFieldToSchema("Year", RSFieldType.SHORT);
		birthdaySchema.addFieldToSchema("Name", RSFieldType.STRING);
		
		RSProcessor proc = new RSProcessor();
		RSStruct rs_birthday = new RSStruct(birthdaySchema);
		
		while(hasMoreWorkToDo()) {
			rs_birthday.reset();
			
			//Hypothetical object that holds birthdays
			Birthday bDay = getNextBirthday();
			byte day = bDay.day;
			byte month = bDay.month;
			short year = bDay.year;
			String name = bDay.name;
			
			rs_birthday.addByte("Day", day);
			rs_birthday.addByte("Month", month);
			rs_birthday.addShort("Year", year);
			rs_birthday.addString("Name", name);
			
			byte[] birthdayBytes = proc.writeBytes(rs_birthday);
			functionThatSendsBytesOverNetwork(birthdayBytes);
		}
	}
}
~~~

## Full Deseralization Example

Below is a full example of how one might deserialize data.

~~~
import fibrous.rapidstruct.*;

public class RSTest {
	public static void main(String[]args) {
		RSSchema birthdaySchema = new RSSchema();
		birthdaySchema.addFieldToSchema("Day", RSFieldType.BYTE);
		birthdaySchema.addFieldToSchema("Month", RSFieldType.RAW);
		birthdaySchema.addFieldToSchema("Year", RSFieldType.SHORT);
		birthdaySchema.addFieldToSchema("Name", RSFieldType.STRING);
		
		RSProcessor proc = new RSProcessor();
		RSStruct rs_birthday = new RSStruct(birthdaySchema);
		
		while(hasMoreWorkToDo()) {
			byte[] birthdayBytes = functionThatReceivesBytesOverTheNetwork();
			proc.readBytes(birthdayBytes, rs_birthday);
			
			byte day = rs_birthday.get("Day").asByte();
			byte month = rs_birthday.get("Day").asByte();
			short year = rs_birthday.get("Day").asShort();
			String name = rs_birthday.get("Day").asString();
			
			functionThatDisplaysBirthdays(day, month, year, name);
		}
	}
}
~~~

Copyright (c) 2026, Noah McLean

