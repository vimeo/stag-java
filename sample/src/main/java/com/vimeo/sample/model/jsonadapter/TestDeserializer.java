package com.vimeo.sample.model.jsonadapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.vimeo.sample.model.User;

import java.lang.reflect.Type;

/**
 * Created by anshul.garg on 08/03/17.
 */

public class TestDeserializer implements JsonDeserializer<User> {
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(json, User.class);
    }
}
