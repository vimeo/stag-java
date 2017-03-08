package com.vimeo.sample.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by anshul.garg on 02/03/17.
 */

public class TestSerializer implements JsonSerializer<User> {

    @Override
    public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("location", context.serialize(src.mLocation));
        result.add("name", context.serialize(src.mName));
        return result;
    }
}