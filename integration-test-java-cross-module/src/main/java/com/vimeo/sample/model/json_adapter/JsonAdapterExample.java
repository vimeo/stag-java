package com.vimeo.sample.model.json_adapter;

import android.support.annotation.NonNull;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.vimeo.sample.model.basic.BasicModel2;
import com.vimeo.stag.UseStag;

import java.util.Date;

@UseStag
public class JsonAdapterExample {

    @SerializedName("created_time")
    @JsonAdapter(value = DateParser.class, nullSafe = true)
    @NonNull
    public Date mCreatedTime;

    @SerializedName("accessed_time")
    @JsonAdapter(value = DateParser.class, nullSafe = false)
    public Date mAccessedTime;


    @SerializedName("alternate_model")
    @JsonAdapter(value = com.vimeo.sample_java_model.stag.generated.Stag.Factory.class)
    public com.vimeo.sample_java_model.AlternateNameModel mRunTimeExample;


    @SerializedName("alternate_model1")
    @JsonAdapter(value = com.vimeo.sample_java_model.AlternateNameModel$TypeAdapter.class)
    public com.vimeo.sample_java_model.AlternateNameModel mRunTimeExample1;

    @SerializedName("user")
    @JsonAdapter(value = TestSerializer.class)
    public BasicModel2 user;

    @SerializedName("user1")
    @JsonAdapter(value = TestDeserializer.class)
    public BasicModel2 user1;

    @SerializedName("user2")
    @JsonAdapter(value = TestSerializerDeserializer.class)
    public BasicModel2 user2;
}
