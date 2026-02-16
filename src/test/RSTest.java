package test;

import java.util.ArrayList;

import fibrous.rapidstruct.*;

public class RSTest {
    public static void main(String[]args) {
        e2eTestTag();
    }

    static void e2eTestTag() {
        boolean origBoolVal = false;
        byte origByteVal = 123;
        short origShortVal = 1234;
        int origIntVal = 123456;
        long origLongVal = 123456789012L;
        float origFloatVal = 123.456f;
        double origDoubleVal = 12345.6789;
        String origString = "Test string";
        byte[] origByteArray = {1, 2, 3, 4, 5};

        RSSchema nestedSchema = new RSSchema();;
        nestedSchema.addFieldToSchema("Bool", RSFieldType.BOOL);
        nestedSchema.addFieldToSchema("Byte", RSFieldType.BYTE);
        nestedSchema.addFieldToSchema("Short", RSFieldType.SHORT);
        nestedSchema.addFieldToSchema("Int", RSFieldType.INT);
        nestedSchema.addFieldToSchema("Long", RSFieldType.LONG);
        nestedSchema.addFieldToSchema("Float", RSFieldType.FLOAT);
        nestedSchema.addFieldToSchema("Double", RSFieldType.DOUBLE);
        nestedSchema.addFieldToSchema("String", RSFieldType.STRING);
        nestedSchema.addFieldToSchema("ByteArray", RSFieldType.RAW);

        RSSchema mainSchema = new RSSchema();
        mainSchema.addFieldToSchema("Bool", RSFieldType.BOOL);
        mainSchema.addFieldToSchema("Byte", RSFieldType.BYTE);
        mainSchema.addFieldToSchema("Short", RSFieldType.SHORT);
        mainSchema.addFieldToSchema("Int", RSFieldType.INT);
        mainSchema.addFieldToSchema("Long", RSFieldType.LONG);
        mainSchema.addFieldToSchema("Float", RSFieldType.FLOAT);
        mainSchema.addFieldToSchema("Double", RSFieldType.DOUBLE);
        mainSchema.addFieldToSchema("String", RSFieldType.STRING);
        mainSchema.addFieldToSchema("ByteArray", RSFieldType.RAW);
        mainSchema.addStructToSchema("Struct", nestedSchema);

        RSProcessor proc = new RSProcessor();
        
        RSStruct mainStruct = new RSStruct(mainSchema);

        int iterations = 10;
        for(int i = 0; i < iterations; i++) {
            RSStruct nestedStruct = new RSStruct(nestedSchema);

            nestedStruct.addBool("Bool", origBoolVal);
            nestedStruct.addByte("Byte", origByteVal);
            nestedStruct.addShort("Short", origShortVal);
            nestedStruct.addInt("Int", origIntVal);
            nestedStruct.addLong("Long", origLongVal);
            nestedStruct.addFloat("Float", origFloatVal);
            nestedStruct.addDouble("Double", origDoubleVal);
            nestedStruct.addString("String", origString);
            nestedStruct.addBytes("ByteArray", origByteArray);

            mainStruct.addBool("Bool", origBoolVal);
            mainStruct.addByte("Byte", origByteVal);
            mainStruct.addShort("Short", origShortVal);
            mainStruct.addInt("Int", origIntVal);
            mainStruct.addLong("Long", origLongVal);
            mainStruct.addFloat("Float", origFloatVal);
            mainStruct.addDouble("Double", origDoubleVal);
            mainStruct.addString("String", origString);
            mainStruct.addBytes("ByteArray", origByteArray);
            mainStruct.addStruct("Struct", nestedStruct);
        }

        byte[] bytes = proc.writeBytes(mainStruct);

        RSStruct dupMainStruct = new RSStruct(mainSchema);
        proc.readBytes(bytes, dupMainStruct);

        ArrayList<RSField> dupNestedStructFields = dupMainStruct.getAllFieldsWithTag("Struct");

        ArrayList<RSField> dupBoolFields = dupMainStruct.getAllFieldsWithTag("Bool");
        ArrayList<RSField> dupByteFields = dupMainStruct.getAllFieldsWithTag("Byte");
        ArrayList<RSField> dupShortFields = dupMainStruct.getAllFieldsWithTag("Short");
        ArrayList<RSField> dupIntFields = dupMainStruct.getAllFieldsWithTag("Int");
        ArrayList<RSField> dupLongFields = dupMainStruct.getAllFieldsWithTag("Long");
        ArrayList<RSField> dupFloatFields = dupMainStruct.getAllFieldsWithTag("Float");
        ArrayList<RSField> dupDoubleFields = dupMainStruct.getAllFieldsWithTag("Double");
        ArrayList<RSField> dupStringFields = dupMainStruct.getAllFieldsWithTag("String");
        ArrayList<RSField> dupByteArrayFields = dupMainStruct.getAllFieldsWithTag("ByteArray");

        for(int i = 0; i < iterations; i++) {
            RSStruct dupNestedStruct = dupNestedStructFields.get(i).asStruct();

            boolean dupBoolVal = dupNestedStruct.get("Bool").asBool();
            byte dupByteVal = dupNestedStruct.get("Byte").asByte();
            short dupShortVal = dupNestedStruct.get("Short").asShort();
            int dupIntVal = dupNestedStruct.get("Int").asInt();
            long dupLongVal = dupNestedStruct.get("Long").asLong();
            float dupFloatVal = dupNestedStruct.get("Float").asFloat();
            double dupDoubleVal = dupNestedStruct.get("Double").asDouble();
            String dupString = dupNestedStruct.get("String").asString();
            byte[] dupByteArray = dupNestedStruct.get("ByteArray").asBytes();

            assertC(dupBoolVal == origBoolVal);
            assertC(dupByteVal == origByteVal);
            assertC(dupShortVal == origShortVal);
            assertC(dupIntVal == origIntVal);
            assertC(dupLongVal == origLongVal);
            assertC(dupFloatVal == origFloatVal);
            assertC(dupDoubleVal == origDoubleVal);
            assertC(dupString.equals(origString));
            assertC(compareBytes(dupByteArray, origByteArray));

            dupBoolVal = dupBoolFields.get(i).asBool();
            dupByteVal = dupByteFields.get(i).asByte();
            dupShortVal = dupShortFields.get(i).asShort();
            dupIntVal = dupIntFields.get(i).asInt();
            dupLongVal = dupLongFields.get(i).asLong();
            dupFloatVal = dupFloatFields.get(i).asFloat();
            dupDoubleVal = dupDoubleFields.get(i).asDouble();
            dupString = dupStringFields.get(i).asString();
            dupByteArray = dupByteArrayFields.get(i).asBytes();

            assertC(dupBoolVal == origBoolVal);
            assertC(dupByteVal == origByteVal);
            assertC(dupShortVal == origShortVal);
            assertC(dupIntVal == origIntVal);
            assertC(dupLongVal == origLongVal);
            assertC(dupFloatVal == origFloatVal);
            assertC(dupDoubleVal == origDoubleVal);
            assertC(dupString.equals(origString));
            assertC(compareBytes(dupByteArray, origByteArray));
        }
    }

    static void e2eTestKey() {
        boolean origBoolVal = false;
        byte origByteVal = 123;
        short origShortVal = 1234;
        int origIntVal = 123456;
        long origLongVal = 123456789012L;
        float origFloatVal = 123.456f;
        double origDoubleVal = 12345.6789;
        String origString = "Test string";
        byte[] origByteArray = {1, 2, 3, 4, 5};

        RSSchema nestedSchema = new RSSchema();;
        int nestedBoolKey = nestedSchema.addFieldToSchema("Bool", RSFieldType.BOOL);
        int nestedByteKey = nestedSchema.addFieldToSchema("Byte", RSFieldType.BYTE);
        int nestedShortKey = nestedSchema.addFieldToSchema("Short", RSFieldType.SHORT);
        int nestedIntKey = nestedSchema.addFieldToSchema("Int", RSFieldType.INT);
        int nestedLongKey = nestedSchema.addFieldToSchema("Long", RSFieldType.LONG);
        int nestedFloatKey = nestedSchema.addFieldToSchema("Float", RSFieldType.FLOAT);
        int nestedDoubleKey = nestedSchema.addFieldToSchema("Double", RSFieldType.DOUBLE);
        int nestedStringKey = nestedSchema.addFieldToSchema("String", RSFieldType.STRING);
        int nestedByteArrayKey = nestedSchema.addFieldToSchema("ByteArray", RSFieldType.RAW);

        RSSchema mainSchema = new RSSchema();
        int boolKey = mainSchema.addFieldToSchema("Bool", RSFieldType.BOOL);
        int byteKey = mainSchema.addFieldToSchema("Byte", RSFieldType.BYTE);
        int shortKey = mainSchema.addFieldToSchema("Short", RSFieldType.SHORT);
        int intKey = mainSchema.addFieldToSchema("Int", RSFieldType.INT);
        int longKey = mainSchema.addFieldToSchema("Long", RSFieldType.LONG);
        int floatKey = mainSchema.addFieldToSchema("Float", RSFieldType.FLOAT);
        int doubleKey = mainSchema.addFieldToSchema("Double", RSFieldType.DOUBLE);
        int stringKey = mainSchema.addFieldToSchema("String", RSFieldType.STRING);
        int byteArrayKey = mainSchema.addFieldToSchema("ByteArray", RSFieldType.RAW);
        int structKey = mainSchema.addStructToSchema("Struct", nestedSchema);

        RSProcessor proc = new RSProcessor();
        
        RSStruct mainStruct = new RSStruct(mainSchema);

        int iterations = 10;
        for(int i = 0; i < iterations; i++) {
            RSStruct nestedStruct = new RSStruct(nestedSchema);

            nestedStruct.addBool(nestedBoolKey, origBoolVal);
            nestedStruct.addByte(nestedByteKey, origByteVal);
            nestedStruct.addShort(nestedShortKey, origShortVal);
            nestedStruct.addInt(nestedIntKey, origIntVal);
            nestedStruct.addLong(nestedLongKey, origLongVal);
            nestedStruct.addFloat(nestedFloatKey, origFloatVal);
            nestedStruct.addDouble(nestedDoubleKey, origDoubleVal);
            nestedStruct.addString(nestedStringKey, origString);
            nestedStruct.addBytes(nestedByteArrayKey, origByteArray);

            mainStruct.addBool(boolKey, origBoolVal);
            mainStruct.addByte(byteKey, origByteVal);
            mainStruct.addShort(shortKey, origShortVal);
            mainStruct.addInt(intKey, origIntVal);
            mainStruct.addLong(longKey, origLongVal);
            mainStruct.addFloat(floatKey, origFloatVal);
            mainStruct.addDouble(doubleKey, origDoubleVal);
            mainStruct.addString(stringKey, origString);
            mainStruct.addBytes(byteArrayKey, origByteArray);
            mainStruct.addStruct(structKey, nestedStruct);
        }

        byte[] bytes = proc.writeBytes(mainStruct);

        RSStruct dupMainStruct = new RSStruct(mainSchema);
        proc.readBytes(bytes, dupMainStruct);

        ArrayList<RSField> dupNestedStructFields = dupMainStruct.getAllFieldsWithKey(structKey);

        ArrayList<RSField> dupBoolFields = dupMainStruct.getAllFieldsWithKey(byteArrayKey);
        ArrayList<RSField> dupByteFields = dupMainStruct.getAllFieldsWithKey(boolKey);
        ArrayList<RSField> dupShortFields = dupMainStruct.getAllFieldsWithKey(shortKey);
        ArrayList<RSField> dupIntFields = dupMainStruct.getAllFieldsWithKey(intKey);
        ArrayList<RSField> dupLongFields = dupMainStruct.getAllFieldsWithKey(longKey);
        ArrayList<RSField> dupFloatFields = dupMainStruct.getAllFieldsWithKey(floatKey);
        ArrayList<RSField> dupDoubleFields = dupMainStruct.getAllFieldsWithKey(doubleKey);
        ArrayList<RSField> dupStringFields = dupMainStruct.getAllFieldsWithKey(stringKey);
        ArrayList<RSField> dupByteArrayFields = dupMainStruct.getAllFieldsWithKey(byteArrayKey);

        for(int i = 0; i < iterations; i++) {
            RSStruct dupNestedStruct = dupNestedStructFields.get(i).asStruct();

            boolean dupBoolVal = dupNestedStruct.get(nestedBoolKey).asBool();
            byte dupByteVal = dupNestedStruct.get(nestedByteKey).asByte();
            short dupShortVal = dupNestedStruct.get(nestedShortKey).asShort();
            int dupIntVal = dupNestedStruct.get(nestedIntKey).asInt();
            long dupLongVal = dupNestedStruct.get(nestedLongKey).asLong();
            float dupFloatVal = dupNestedStruct.get(nestedFloatKey).asFloat();
            double dupDoubleVal = dupNestedStruct.get(nestedDoubleKey).asDouble();
            String dupString = dupNestedStruct.get(nestedStringKey).asString();
            byte[] dupByteArray = dupNestedStruct.get(nestedByteArrayKey).asBytes();

            assertC(dupBoolVal == origBoolVal);
            assertC(dupByteVal == origByteVal);
            assertC(dupShortVal == origShortVal);
            assertC(dupIntVal == origIntVal);
            assertC(dupLongVal == origLongVal);
            assertC(dupFloatVal == origFloatVal);
            assertC(dupDoubleVal == origDoubleVal);
            assertC(dupString.equals(origString));
            assertC(compareBytes(dupByteArray, origByteArray));

            dupBoolVal = dupBoolFields.get(i).asBool();
            dupByteVal = dupByteFields.get(i).asByte();
            dupShortVal = dupShortFields.get(i).asShort();
            dupIntVal = dupIntFields.get(i).asInt();
            dupLongVal = dupLongFields.get(i).asLong();
            dupFloatVal = dupFloatFields.get(i).asFloat();
            dupDoubleVal = dupDoubleFields.get(i).asDouble();
            dupString = dupStringFields.get(i).asString();
            dupByteArray = dupByteArrayFields.get(i).asBytes();

            assertC(dupBoolVal == origBoolVal);
            assertC(dupByteVal == origByteVal);
            assertC(dupShortVal == origShortVal);
            assertC(dupIntVal == origIntVal);
            assertC(dupLongVal == origLongVal);
            assertC(dupFloatVal == origFloatVal);
            assertC(dupDoubleVal == origDoubleVal);
            assertC(dupString.equals(origString));
            assertC(compareBytes(dupByteArray, origByteArray));
        }
    }

    static boolean compareBytes(byte[] a, byte[] b) {
        if(a.length != b.length)
            return false;

        for(int i = 0; i < a.length; i++) {
            if(a[i] != b[i])
                return false;
        }

        return true;
    }

    static void assertC(boolean condition) {
        if(!condition)
            throw new AssertionError();
    }
}
