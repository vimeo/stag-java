package com.vimeo.sample_java_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.vimeo.sample_java_model.SwappableParserExampleModel.TestObject;
import com.vimeo.sample_java_model.stag.generated.Stag;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Unit tests for {@link SwappableParserExampleModel}.
 */
public class SwappableParserExampleModelTest {

    /**
     * A type adapter that reads and writes the string field in forward order.
     */
    private static class TestObjectAdapter1 extends TypeAdapter<TestObject> {

        TestObjectAdapter1() {}

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
            Assert.assertTrue("testField".equals(in.nextName()));
            object.testField = in.nextString();
            in.endObject();

            return object;
        }
    }

    /**
     * A type adapter that reads and writes the string field in reverse order.
     */
    private static class TestObjectAdapter2 extends TypeAdapter<TestObject> {

        TestObjectAdapter2() {}

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
            Assert.assertTrue("testField".equals(in.nextName()));
            object.testField = new StringBuilder(in.nextString()).reverse().toString();
            in.endObject();

            return object;
        }
    }

    @Test
    public void verifyTypeAdapterWasGenerated_SwappableParserExampleModel() throws Exception {
        Utils.verifyTypeAdapterGeneration(SwappableParserExampleModel.class);
    }

    @Test
    public void verifyTypeAdapterWasNotGenerated_TestObject() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(SwappableParserExampleModel.TestObject.class);
    }

    @NotNull
    private static String swappableParserExampleJsonWithTestObjectField(@NotNull String testObjectField) {
        return "{\"testField1\":\"test\",\"testField2\":{\"testField\":\"" + testObjectField + "\"}}";
    }

    /**
     * This tests the Stag.Factory to ensure that it cannot
     * be used by multiple gson instances.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void test_SwappingTypeAdapters_ThrowsException() {
        // In a world where Stag supports reusing a single Stag.Factory among gson instances,
        // this test should assert that the factory handles the case where a type adapter
        // could be different between gson instances.

        // TypeAdapter1 reads the field normally
        String typeAdapter1Json = swappableParserExampleJsonWithTestObjectField("test");

        Stag.Factory factory = new Stag.Factory();

        Gson gson1 = new GsonBuilder().registerTypeAdapterFactory(factory).registerTypeAdapter(TestObject.class, new TestObjectAdapter1()).create();

        SwappableParserExampleModel model1 = gson1.fromJson(typeAdapter1Json, SwappableParserExampleModel.class);

        Assert.assertEquals(model1.testField2.testField, "test");

        // TypeAdapter2 assumes a reversed string
        String typeAdapter2Json = swappableParserExampleJsonWithTestObjectField("tset");

        Gson gson2 = new GsonBuilder().registerTypeAdapterFactory(factory).registerTypeAdapter(TestObject.class, new TestObjectAdapter2()).create();

        SwappableParserExampleModel model2 = gson2.fromJson(typeAdapter2Json, SwappableParserExampleModel.class);

        Assert.assertEquals(model2.testField2.testField, "test");
    }
}