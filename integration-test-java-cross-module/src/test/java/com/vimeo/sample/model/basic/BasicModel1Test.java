package com.vimeo.sample.model.basic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.vimeo.sample.Utils;

import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import com.vimeo.sample.stag.generated.Stag;


import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link BasicModel1}.
 */
public class BasicModel1Test {

    @Test
    public void verifyTypeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(BasicModel1.class);
    }

    @Test
    public void verifyWrapperTypeAdaptersWorkCorrectly() {
        final AtomicInteger writeCounter = new AtomicInteger(0);
        final AtomicInteger readCounter = new AtomicInteger(0);
        final TypeAdapterFactory postParseFactory = new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
                return new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        writeCounter.incrementAndGet();
                        delegate.write(out, value);
                    }

                    @Override
                    public T read(JsonReader in) throws IOException {
                        readCounter.incrementAndGet();
                        return delegate.read(in);
                    }
                };
            }
        };

        final Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new Stag.Factory())
                .registerTypeAdapterFactory(postParseFactory)
                .create();

        final BasicModel1 data = new BasicModel1();
        data.setAge(2);
        data.setAwards(Collections.singletonList("hello"));
        data.setName("test");

        final String json = gson.toJson(data);
        final BasicModel1 deserializedData = gson.fromJson(json, BasicModel1.class);

        assertEquals(data, deserializedData);
        assertEquals(writeCounter.get(), 1);
        assertEquals(readCounter.get(), 1);
    }
}