package com.vimeo.sample.model1;

import android.support.annotation.NonNull;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.vimeo.sample.model.User;
import com.vimeo.stag.UseStag;

/**
 * This class has been added to reproduce the scenario where two classes with same
 * names exists in different packages. This will make sure that the getter type
 * adapter method for these classes are generated with the correct name (appending
 * package name) so that two getters with same name is not created.
 * <p>
 * This class has been referenced as a member variable in {@link com.vimeo.sample.model.Video} class
 */
@UseStag
public class Video {

    @NonNull
    @SerializedName("user")
    public User mUser;

    @NonNull
    @SerializedName("link")
    public String mLink;

    @NonNull
    @SerializedName("mData")
    @JsonAdapter(value = ReflectiveTypeAdapterFactory.class)
    public Data mData;
}