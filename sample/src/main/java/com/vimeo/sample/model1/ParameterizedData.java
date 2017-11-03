package com.vimeo.sample.model1;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Since this class is not annotated with @UseStag, this will use TypeToken for its adapter generation.
 * As it is of paramterized type, this will use TypeToken.getParameterized() instead of TypeToken.get()
 * @param <T>
 */
public class ParameterizedData<T> {

    @SerializedName("list")
    public List<T> list;
}