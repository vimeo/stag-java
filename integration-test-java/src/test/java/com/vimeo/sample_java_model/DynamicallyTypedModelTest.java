package com.vimeo.sample_java_model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vimeo.sample_java_model.stag.generated.Stag;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link DynamicallyTypedWildcardReadModel}.
 */
public class DynamicallyTypedModelTest {

    @Test
    public void verifyTypeAdapterWasGenerated_DynamicallyTypedWildcardReadModel() throws Exception {
        Utils.verifyTypeAdapterGeneration(DynamicallyTypedWildcardReadModel.class);
    }

    @Test
    public void verifyTypeAdapterWasGenerated_DynamicallyTypedModel() throws Exception {
        Utils.verifyTypeAdapterGeneration(DynamicallyTypedModel.class);
    }

    @Test
    public void verifyUnmarshalWildcardedTypes() throws Exception {

        final Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new Stag.Factory())
                .registerTypeAdapterFactory(new DynamicallyTypedModelTypeAdapterFactory())
                .create();

        String json = "{\"models\":[" +
                        "{\"type\":\"string\",\"value\":\"value1\"}," +
                        "{\"type\":\"integer\",\"value\":42}" +
                        "]}";
        DynamicallyTypedWildcardReadModel dynamicModel = gson.fromJson(json, DynamicallyTypedWildcardReadModel.class);

        assertEquals(2, dynamicModel.models.size());

        DynamicallyTypedModel<?> stringModel = dynamicModel.models.get(0);
        assertEquals(DynamicallyTypedModel.Types.string, stringModel.type);
        assertEquals("value1", stringModel.value);

        DynamicallyTypedModel<?> integerModel = dynamicModel.models.get(1);
        assertEquals(DynamicallyTypedModel.Types.integer, integerModel.type);
        assertEquals(Integer.class, integerModel.value.getClass());
        assertEquals(42, integerModel.value);
    }

}
