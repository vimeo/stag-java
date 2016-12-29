package com.vimeo.stag;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;

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
}