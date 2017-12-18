package com.vimeo.sample_java_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.vimeo.sample_java_model.WrapperTypeAdapterModel.InnerType;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.vimeo.sample_java_model.stag.generated.Stag;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link WrapperTypeAdapterModel}.
 * <p>
 * Created by restainoa on 12/18/17.
 */
public class WrapperTypeAdapterModelTest {

    @Test
    public void verifyTypeAdapterWasGenerated_WrapperTypeAdapterModel() throws Exception {
        Utils.verifyTypeAdapterGeneration(WrapperTypeAdapterModel.class);
    }

    @Test
    public void verifyTypeAdapterWasGenerated_InnerType() throws Exception {
        Utils.verifyTypeAdapterGeneration(InnerType.class);
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

        final WrapperTypeAdapterModel data = new WrapperTypeAdapterModel();
        final InnerType innerType = new InnerType();
        innerType.setHello("test");
        data.setInnerType(innerType);

        final String json = gson.toJson(data);
        final WrapperTypeAdapterModel deserializedData = gson.fromJson(json, WrapperTypeAdapterModel.class);

        assertEquals(data, deserializedData);
        assertEquals(writeCounter.get(), 2);
        assertEquals(readCounter.get(), 2);
    }

}