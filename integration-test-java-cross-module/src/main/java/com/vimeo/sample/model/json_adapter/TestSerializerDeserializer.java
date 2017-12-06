package com.vimeo.sample.model.json_adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.vimeo.sample.model.basic.BasicModel2;

import java.lang.reflect.Type;

/**
 * Created by anshul.garg on 08/03/17.
 */

public class TestSerializerDeserializer implements JsonDeserializer<BasicModel2>, JsonSerializer<BasicModel2> {

    @Override
    public BasicModel2 deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(json, BasicModel2.class);
    }

    @Override
    public JsonElement serialize(BasicModel2 src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("duration", context.serialize(src.getDuration()));
        result.add("name", context.serialize(src.getTitle()));
        return result;
    }
}