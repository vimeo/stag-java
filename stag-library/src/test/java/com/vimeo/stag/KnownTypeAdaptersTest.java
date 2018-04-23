package com.vimeo.stag;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("MagicNumber")
public class KnownTypeAdaptersTest {

    /**
     * Test for {@link KnownTypeAdapters#INTEGER}
     */
    @Test
    public void testForIntegerTypeAdapter() throws Exception {
        Integer value = 250;

        TypeAdapter<Integer> integerTypeAdapter = KnownTypeAdapters.INTEGER;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        integerTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        Integer readValue = integerTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertEquals(value.intValue(), readValue.intValue());
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveIntTypeAdapter}
     */
    @Test
    public void testForPrimitiveIntegerTypeAdapter() throws Exception {
        int value = 250;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveIntTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        int readValue = KnownTypeAdapters.PrimitiveIntTypeAdapter.read(new JsonReader(new StringReader(jsonString)), 0);

        assertEquals(value, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveIntegerArrayAdapter}
     */
    @Test
    public void testForPrimitiveArrayIntegerTypeAdapter() throws Exception {
        int[] value = new int[5];

        value[0] = 0;
        value[1] = 1;
        value[2] = 2;
        value[3] = 3;
        value[4] = 4;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveIntegerArrayAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        int[] readValue = KnownTypeAdapters.PrimitiveIntegerArrayAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertArrayEquals(value, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters#BYTE}
     */
    @Test
    public void testForByteTypeAdapter() throws Exception {
        Byte value = 1;

        TypeAdapter<Byte> byteTypeAdapter = KnownTypeAdapters.BYTE;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        byteTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        Byte readValue = byteTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertEquals(value.intValue(), readValue.intValue());
        assertEquals(value.byteValue(), readValue.byteValue());
        assertEquals(value, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveByteTypeAdapter}
     */
    @Test
    public void testForPrimitiveByteTypeAdapter() throws Exception {
        byte value = 1;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveByteTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        byte readValue = KnownTypeAdapters.PrimitiveByteTypeAdapter.read(new JsonReader(new StringReader(jsonString)), (byte) 0);

        assertEquals(value, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveByteArrayAdapter}
     */
    @Test
    public void testForPrimitiveArrayByteTypeAdapter() throws Exception {
        byte[] value = new byte[5];

        value[0] = 0;
        value[1] = 1;
        value[2] = 2;
        value[3] = 3;
        value[4] = 4;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveByteArrayAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        byte[] readValue = KnownTypeAdapters.PrimitiveByteArrayAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertArrayEquals(value, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters#SHORT}
     */
    @Test
    public void testForShortTypeAdapter() throws Exception {
        Short value = 1;

        TypeAdapter<Short> shortTypeAdapter = KnownTypeAdapters.SHORT;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        shortTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        Short readValue = shortTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertEquals(value.intValue(), readValue.intValue());
        assertEquals(value.shortValue(), readValue.shortValue());
        assertEquals(value, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveShortTypeAdapter}
     */
    @Test
    public void testForPrimitiveShortTypeAdapter() throws Exception {
        short value = 1;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveShortTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        short readValue = KnownTypeAdapters.PrimitiveShortTypeAdapter.read(new JsonReader(new StringReader(jsonString)), (short) 0);

        assertEquals(value, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveShortArrayAdapter}
     */
    @Test
    public void testForPrimitiveArrayShortTypeAdapter() throws Exception {
        short[] value = new short[5];

        value[0] = 0;
        value[1] = 1;
        value[2] = 2;
        value[3] = 3;
        value[4] = 4;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveShortArrayAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        short[] readValue = KnownTypeAdapters.PrimitiveShortArrayAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertArrayEquals(value, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters#LONG}
     */
    @Test
    public void testForLongTypeAdapter() throws Exception {
        Long value = 121223444L;

        TypeAdapter<Long> longTypeAdapter = KnownTypeAdapters.LONG;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        longTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        Long readValue = longTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertEquals(value.intValue(), readValue.intValue());
        assertEquals(value.longValue(), readValue.longValue());
        assertEquals(value, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveLongTypeAdapter}
     */
    @Test
    public void testForPrimitiveLongTypeAdapter() throws Exception {
        long value = 121223444L;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveLongTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        long readValue = KnownTypeAdapters.PrimitiveLongTypeAdapter.read(new JsonReader(new StringReader(jsonString)), 0);

        assertEquals(value, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveLongArrayAdapter}
     */
    @Test
    public void testForPrimitiveArrayLongTypeAdapter() throws Exception {
        long[] value = new long[5];

        value[0] = 0;
        value[1] = 1;
        value[2] = 2;
        value[3] = 3;
        value[4] = 4;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveLongArrayAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        long[] readValue = KnownTypeAdapters.PrimitiveLongArrayAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertArrayEquals(value, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters#FLOAT}
     */
    @Test
    public void testForFloatTypeAdapter() throws Exception {
        Float value = 1234.1243F;

        TypeAdapter<Float> floatTypeAdapter = KnownTypeAdapters.FLOAT;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        floatTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        Float readValue = floatTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertEquals(value.intValue(), readValue.intValue());
        assertEquals(value.doubleValue(), readValue.doubleValue(), 0);
        assertEquals(value, readValue, 0);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveFloatTypeAdapter}
     */
    @Test
    public void testForPrimitiveFloatTypeAdapter() throws Exception {
        float value = 1234.1243F;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveFloatTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        float readValue = KnownTypeAdapters.PrimitiveFloatTypeAdapter.read(new JsonReader(new StringReader(jsonString)), 0f);

        assertEquals(value, readValue, 0);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveFloatArrayAdapter}
     */
    @Test
    public void testForPrimitiveArrayFloatTypeAdapter() throws Exception {
        float[] value = new float[5];

        value[0] = 0.0f;
        value[1] = 1.0f;
        value[2] = 2.0f;
        value[3] = 3.0f;
        value[4] = 4.0f;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveFloatArrayAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        float[] readValue = KnownTypeAdapters.PrimitiveFloatArrayAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertArrayEquals(value, readValue, 0);
    }

    /**
     * Test for {@link KnownTypeAdapters#DOUBLE}
     */
    @Test
    public void testForDoubleTypeAdapter() throws Exception {
        Double value = 1234.1243;

        TypeAdapter<Double> floatTypeAdapter = KnownTypeAdapters.DOUBLE;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        floatTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        Double readValue = floatTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertEquals(value.intValue(), readValue.intValue());
        assertEquals(value, readValue, 0);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveDoubleTypeAdapter}
     */
    @Test
    public void testForPrimitiveDoubleTypeAdapter() throws Exception {
        double value = 1234.1243;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveDoubleTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        double readValue = KnownTypeAdapters.PrimitiveDoubleTypeAdapter.read(new JsonReader(new StringReader(jsonString)), 0f);

        assertEquals(value, readValue, 0);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveDoubleArrayAdapter}
     */
    @Test
    public void testForPrimitiveArrayDoubleTypeAdapter() throws Exception {
        double[] value = new double[5];

        value[0] = 0.0;
        value[1] = 1.0;
        value[2] = 2.0;
        value[3] = 3.0;
        value[4] = 4.0;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveDoubleArrayAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        double[] readValue = KnownTypeAdapters.PrimitiveDoubleArrayAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertArrayEquals(value, readValue, 0);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveBooleanTypeAdapter}
     */
    @Test
    public void testForPrimitiveBooleanTypeAdapter() throws Exception {
        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveBooleanTypeAdapter.write(new JsonWriter(stringWriter), true);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        boolean readValue = KnownTypeAdapters.PrimitiveBooleanTypeAdapter.read(new JsonReader(new StringReader(jsonString)), false);

        assertEquals(true, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveBooleanArrayAdapter}
     */
    @Test
    public void testForPrimitiveArrayBooleanTypeAdapter() throws Exception {
        boolean[] value = new boolean[5];

        value[0] = true;
        value[1] = false;
        value[2] = true;
        value[3] = true;
        value[4] = false;

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveBooleanArrayAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        boolean[] readValue = KnownTypeAdapters.PrimitiveBooleanArrayAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertArrayEquals(value, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveCharTypeAdapter}
     */
    @Test
    public void testForPrimitiveCharacterTypeAdapter() throws Exception {
        char value = 'A';

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveCharTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        char readValue = KnownTypeAdapters.PrimitiveCharTypeAdapter.read(new JsonReader(new StringReader(jsonString)), 'B');

        assertEquals(value, readValue);
    }

    /**
     * Test for {@link KnownTypeAdapters.PrimitiveCharArrayAdapter}
     */
    @Test
    public void testForPrimitiveArrayCharacterTypeAdapter() throws Exception {
        // test an actual char array
        char[] value = new char[5];

        value[0] = 'a';
        value[1] = 'b';
        value[2] = 'c';
        value[3] = 'd';
        value[4] = 'e';

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        KnownTypeAdapters.PrimitiveCharArrayAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        // call the TypeAdapter#read method
        char[] readValue = KnownTypeAdapters.PrimitiveCharArrayAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertArrayEquals(value, readValue);

        // test a string as a char array
        char[] value1 = "abcde".toCharArray();

        // create a string writer, and write the value to it using adapter
        StringWriter stringWriter1 = new StringWriter();
        KnownTypeAdapters.PrimitiveCharArrayAdapter.write(new JsonWriter(stringWriter1), value1);
        String jsonString1 = stringWriter1.toString();

        // call the TypeAdapter#read method
        char[] readValue1 = KnownTypeAdapters.PrimitiveCharArrayAdapter.read(new JsonReader(new StringReader(jsonString1)));

        assertArrayEquals(value1, readValue1);
    }

    /**
     * Test for {@link KnownTypeAdapters.ListTypeAdapter}
     */
    @Test
    public void testForListTypeAdapter() throws Exception {

        // for string arrays
        ArrayList<String> dummyList = Utils.createStringDummyList();

        TypeAdapter<ArrayList<String>> listTypeAdapter = new KnownTypeAdapters.ListTypeAdapter<>(TypeAdapters.STRING,
                                                                                                 new KnownTypeAdapters.ArrayListInstantiator<String>());

        StringWriter stringWriter = new StringWriter();
        listTypeAdapter.write(new JsonWriter(stringWriter), dummyList);
        String jsonString = stringWriter.toString();

        ArrayList<String> readValue = listTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertThat(readValue).isNotNull();

        assertEquals(dummyList.size(), readValue.size());
        for (int i = 0; i < dummyList.size(); i++) {
            assertEquals(dummyList.get(i), readValue.get(i));
        }

        // for integer arrays
        ArrayList<Integer> intDummyList = Utils.createIntegerDummyList();

        TypeAdapter<ArrayList<Integer>> listTypeAdapter1 = new KnownTypeAdapters.ListTypeAdapter<>(KnownTypeAdapters.INTEGER,
                                                                                                   new KnownTypeAdapters.ArrayListInstantiator<Integer>());
        stringWriter = new StringWriter();
        listTypeAdapter1.write(new JsonWriter(stringWriter), intDummyList);
        jsonString = stringWriter.toString();

        ArrayList<Integer> readValue1 = listTypeAdapter1.read(new JsonReader(new StringReader(jsonString)));

        assertThat(readValue1).isNotNull();

        assertEquals(intDummyList.size(), readValue1.size());
        for (int i = 0; i < intDummyList.size(); i++) {
            assertEquals(intDummyList.get(i), readValue1.get(i));
        }
    }

    /**
     * Test for {@link KnownTypeAdapters.MapTypeAdapter}
     */
    @Test
    public void testForMapTypeAdapter() throws Exception {

        // for string arrays
        HashMap<String, String> dummyMap = Utils.createStringDummyMap();

        TypeAdapter<HashMap<String, String>> mapTypeAdapter = new KnownTypeAdapters.MapTypeAdapter<>(TypeAdapters.STRING, TypeAdapters.STRING,
                                                                                                     new KnownTypeAdapters.HashMapInstantiator<String, String>());

        StringWriter stringWriter = new StringWriter();
        mapTypeAdapter.write(new JsonWriter(stringWriter), dummyMap);
        String jsonString = stringWriter.toString();

        HashMap<String, String> readValue = mapTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertThat(readValue).isNotNull();

        assertEquals(dummyMap.size(), readValue.size());
        Utils.assertMapsEqual(dummyMap, readValue);

        // for integer arrays
        HashMap<Integer, Integer> intDummyMap = Utils.createIntegerDummyMap();

        TypeAdapter<HashMap<Integer, Integer>> mapTypeAdapter1 = new KnownTypeAdapters.MapTypeAdapter<>(KnownTypeAdapters.INTEGER, KnownTypeAdapters.INTEGER,
                                                                                                        new KnownTypeAdapters.HashMapInstantiator<Integer, Integer>());
        stringWriter = new StringWriter();
        mapTypeAdapter1.write(new JsonWriter(stringWriter), intDummyMap);
        jsonString = stringWriter.toString();

        HashMap<Integer, Integer> readValue1 = mapTypeAdapter1.read(new JsonReader(new StringReader(jsonString)));

        assertThat(readValue1).isNotNull();

        assertEquals(intDummyMap.size(), readValue1.size());
        Utils.assertMapsEqual(intDummyMap, readValue1);
    }

    /**
     * Test for {@link KnownTypeAdapters#JSON_OBJECT}
     */
    @Test
    public void testForJsonObjectTypeAdapter() throws Exception {
        JsonObject jsonObject = Utils.createDummyJsonObject();

        TypeAdapter<JsonObject> jsonObjectTypeAdapter = KnownTypeAdapters.JSON_OBJECT;
        StringWriter stringWriter = new StringWriter();
        jsonObjectTypeAdapter.write(new JsonWriter(stringWriter), jsonObject);
        String jsonString = stringWriter.toString();

        JsonObject readValue = jsonObjectTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertEquals(jsonObject.size(), readValue.size());
    }

    /**
     * Test for {@link KnownTypeAdapters#JSON_ARRAY}
     */
    @Test
    public void testForJsonArrayTypeAdapter() throws Exception {
        JsonArray jsonArray = Utils.createDummyJsonArray();

        TypeAdapter<JsonArray> jsonObjectTypeAdapter = KnownTypeAdapters.JSON_ARRAY;
        StringWriter stringWriter = new StringWriter();
        jsonObjectTypeAdapter.write(new JsonWriter(stringWriter), jsonArray);
        String jsonString = stringWriter.toString();

        JsonArray readValue = jsonObjectTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        assertEquals(jsonArray.size(), readValue.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            assertEquals(jsonArray.get(i), readValue.get(i));
        }
    }

}
