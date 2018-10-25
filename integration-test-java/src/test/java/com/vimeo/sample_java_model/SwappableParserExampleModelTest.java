/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2017 Vimeo
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.vimeo.sample_java_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.vimeo.sample_java_model.SwappableParserExampleModel.TestObject;
import com.vimeo.sample_java_model.stag.generated.Stag;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;

import verification.Utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link SwappableParserExampleModel}.
 */
public class SwappableParserExampleModelTest {

    @Test
    public void verifyTypeAdapterWasGenerated_SwappableParserExampleModel() throws Exception {
        Utils.verifyTypeAdapterGeneration(SwappableParserExampleModel.class);
    }

    @Test
    public void verifyTypeAdapterWasNotGenerated_TestObject() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(SwappableParserExampleModel.TestObject.class);
    }

    /**
     * Stag does support being used across Gson instances now
     */
    @Test
    public void test_SwappingTypeAdapters() {

        final Stag.Factory factory = new Stag.Factory();

        // TypeAdapter1 reads the field normally
        final String typeAdapter1Json = swappableParserExampleJsonWithTestObjectField("test");
        final Gson gson1 = new GsonBuilder()
                .registerTypeAdapterFactory(factory)
                .registerTypeAdapter(TestObject.class, new TestObjectAdapter1())
                .create();
        final SwappableParserExampleModel model1 = gson1.fromJson(typeAdapter1Json, SwappableParserExampleModel.class);
        assertEquals(model1.testField2.testField, "test");

        // TypeAdapter2 assumes a reversed string
        final String typeAdapter2Json = swappableParserExampleJsonWithTestObjectField("tset");
        final Gson gson2 = new GsonBuilder()
                .registerTypeAdapterFactory(factory)
                .registerTypeAdapter(TestObject.class, new TestObjectAdapter2())
                .create();
        final SwappableParserExampleModel model2 = gson2.fromJson(typeAdapter2Json, SwappableParserExampleModel.class);
        assertEquals(model2.testField2.testField, "test");
    }

    /**
     * Creates JSON representing a {@link SwappableParserExampleModel}
     * with a nested {@link TestObject} field that contains a string
     * field provided by the caller.
     *
     * @param testObjectField the field to set on the test object.
     * @return valid JSON representing a {@link SwappableParserExampleModel}.
     */
    @NotNull
    private static String swappableParserExampleJsonWithTestObjectField(@NotNull String testObjectField) {
        return "{\"testField1\":\"test\",\"testField2\":{\"testField\":\"" + testObjectField + "\"}}";
    }

    /**
     * A type adapter that reads and writes the string field in forward order.
     */
    private static class TestObjectAdapter1 extends TypeAdapter<TestObject> {

        TestObjectAdapter1() {
        }

        @Override
        public void write(JsonWriter out, TestObject value) throws IOException {
            out.beginObject();
            out.name("testField");
            out.value(value.testField);
            out.endObject();
        }

        @Override
        public TestObject read(JsonReader in) throws IOException {
            TestObject object = new TestObject();

            in.beginObject();
            assertTrue("testField".equals(in.nextName()));
            object.testField = in.nextString();
            in.endObject();

            return object;
        }
    }

    /**
     * A type adapter that reads and writes the string field in reverse order.
     */
    private static class TestObjectAdapter2 extends TypeAdapter<TestObject> {

        TestObjectAdapter2() {
        }

        @Override
        public void write(JsonWriter out, TestObject value) throws IOException {
            out.beginObject();
            out.name("testField");
            out.value(new StringBuilder(value.testField).reverse().toString());
            out.endObject();
        }

        @Override
        public TestObject read(JsonReader in) throws IOException {
            TestObject object = new TestObject();

            in.beginObject();
            assertTrue("testField".equals(in.nextName()));
            object.testField = new StringBuilder(in.nextString()).reverse().toString();
            in.endObject();

            return object;
        }
    }

}
