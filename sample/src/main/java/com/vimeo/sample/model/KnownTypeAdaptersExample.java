package com.vimeo.sample.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;


@UseStag
public class KnownTypeAdaptersExample {

    @SerializedName("jsonElement")
    public JsonElement mJsonElement;

    @SerializedName("jsonObject")
    public JsonObject mJsonObject;

    @SerializedName("jsonArray")
    public JsonArray mJsonArray;

    @SerializedName("jsonPrimitive")
    public JsonPrimitive mJsonPrimitive;
}
