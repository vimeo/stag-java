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
}