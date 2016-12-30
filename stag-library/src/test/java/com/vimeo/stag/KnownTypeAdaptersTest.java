package com.vimeo.stag;

import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

public class KnownTypeAdaptersTest {

    /**
     * Test for {@link KnownTypeAdapters#INTEGER}
     *
     * @throws Exception
     */
    @Test
    public void testForIntegerTypeAdapter() throws Exception {
        Integer value = 250;
        TypeAdapter<Integer> integerTypeAdapter = KnownTypeAdapters.INTEGER;
        //create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        integerTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        //call the typeadapter#read method
        Integer readValue = integerTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        Assert.assertEquals(value.intValue(), readValue.intValue());
    }

    /**
     * Test for {@link KnownTypeAdapters#BYTE}
     *
     * @throws Exception
     */
    @Test
    public void testForByteTypeAdapter() throws Exception {
        Byte value = 1;
        TypeAdapter<Byte> byteTypeAdapter = KnownTypeAdapters.BYTE;
        //create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        byteTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        //call the typeadapter#read method
        Byte readValue = byteTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        Assert.assertEquals(value.intValue(), readValue.intValue());
        Assert.assertEquals(value.byteValue(), readValue.byteValue());
    }

    /**
     * Test for {@link KnownTypeAdapters#SHORT}
     *
     * @throws Exception
     */
    @Test
    public void testForShortTypeAdapter() throws Exception {
        Short value = 1;
        TypeAdapter<Short> shortTypeAdapter = KnownTypeAdapters.SHORT;
        //create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        shortTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        //call the typeadapter#read method
        Short readValue = shortTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        Assert.assertEquals(value.intValue(), readValue.intValue());
        Assert.assertEquals(value.shortValue(), readValue.shortValue());
    }

    /**
     * Test for {@link KnownTypeAdapters#LONG}
     *
     * @throws Exception
     */
    @Test
    public void testForLongTypeAdapter() throws Exception {
        Long value = 121223444L;
        TypeAdapter<Long> longTypeAdapter = KnownTypeAdapters.LONG;
        //create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        longTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        //call the typeadapter#read method
        Long readValue = longTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        Assert.assertEquals(value.intValue(), readValue.intValue());
        Assert.assertEquals(value.longValue(), readValue.longValue());
    }


    /**
     * Test for {@link KnownTypeAdapters#FLOAT}
     *
     * @throws Exception
     */
    @Test
    public void testForFloatTypeAdapter() throws Exception {
        Float value = 1234.1243F;
        TypeAdapter<Float> floatTypeAdapter = KnownTypeAdapters.FLOAT;
        //create a string writer, and write the value to it using adapter
        StringWriter stringWriter = new StringWriter();
        floatTypeAdapter.write(new JsonWriter(stringWriter), value);
        String jsonString = stringWriter.toString();

        //call the typeadapter#read method
        Float readValue = floatTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        Assert.assertEquals(value.intValue(), readValue.intValue());
        Assert.assertEquals(value.doubleValue(), readValue.doubleValue(), 0);
        Assert.assertEquals(value, readValue, 0);
    }

    /**
     * Test for ListTypeAdapter
     *
     * @throws Exception
     */
    @Test
    public void testForListTypeAdapter() throws Exception {

        //for string arrays
        ArrayList<String> dummyList = Utils.createStringDummyList();

        TypeAdapter<ArrayList<String>> listTypeAdapter = new KnownTypeAdapters.ListTypeAdapter<>(TypeAdapters.STRING,
                new KnownTypeAdapters.ArrayListInstantiater<String>());

        StringWriter stringWriter = new StringWriter();
        listTypeAdapter.write(new JsonWriter(stringWriter), dummyList);
        String jsonString = stringWriter.toString();

        ArrayList<String> readValue = listTypeAdapter.read(new JsonReader(new StringReader(jsonString)));

        Assert.assertEquals(dummyList.size(), readValue.size());
        for (int i = 0; i < dummyList.size(); i++) {
            Assert.assertEquals(dummyList.get(i), readValue.get(i));
        }

        //for integer arrays
        ArrayList<Integer> intDummyList = Utils.createIntegerDummyList();

        TypeAdapter<ArrayList<Integer>> listTypeAdapter1 = new KnownTypeAdapters.ListTypeAdapter<>(KnownTypeAdapters.INTEGER,
                new KnownTypeAdapters.ArrayListInstantiater<Integer>());
        stringWriter = new StringWriter();
        listTypeAdapter1.write(new JsonWriter(stringWriter), intDummyList);
        jsonString = stringWriter.toString();

        ArrayList<Integer> readValue1 = listTypeAdapter1.read(new JsonReader(new StringReader(jsonString)));

        Assert.assertEquals(intDummyList.size(), readValue1.size());
        for (int i = 0; i < intDummyList.size(); i++) {
            Assert.assertEquals(intDummyList.get(i), readValue1.get(i));
        }
    }
}