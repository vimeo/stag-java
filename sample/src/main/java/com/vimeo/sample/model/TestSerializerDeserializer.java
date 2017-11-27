package com.vimeo.sample.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.vimeo.sample.model.root.User;

import java.lang.reflect.Type;

/**
 * Created by anshul.garg on 08/03/17.
 */

public class TestSerializerDeserializer implements JsonDeserializer<User>, JsonSerializer<User> {
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(json, User.class);
    }

    @Override
    public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("location", context.serialize(src.mLocation));
        result.add("name", context.serialize(src.mName));
        return result;
    }
}