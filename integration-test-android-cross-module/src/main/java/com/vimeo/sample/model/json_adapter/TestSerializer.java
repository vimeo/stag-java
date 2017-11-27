package com.vimeo.sample.model.json_adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.vimeo.sample.model.basic.BasicModel2;

import java.lang.reflect.Type;

/**
 * Created by anshul.garg on 02/03/17.
 */

public class TestSerializer implements JsonSerializer<BasicModel2> {

    @Override
    public JsonElement serialize(BasicModel2 src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("duration", context.serialize(src.getDuration()));
        result.add("name", context.serialize(src.getTitle()));
        return result;
    }
}