package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

import org.jetbrains.annotations.Nullable;

/**
 * A test case for an object containing null fields.
 * <p>
 * Created by restainoa on 10/20/17.
 */
@UseStag
public class NullFields {

    @Nullable
    @SerializedName("hello")
    public Object hello;


    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        NullFields that = (NullFields) o;

        return hello != null ? hello.equals(that.hello) : that.hello == null;

    }

    @Override
    public int hashCode() {
        return hello != null ? hello.hashCode() : 0;
    }
}
