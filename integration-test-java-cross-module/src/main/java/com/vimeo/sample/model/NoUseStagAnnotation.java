package com.vimeo.sample.model;

import com.google.gson.annotations.SerializedName;

/**
 * This class should not have a TypeAdapter created
 * for it since it is missing a UseStag annotation.
 * <p>
 * Created by restainoa on 1/30/17.
 */
public class NoUseStagAnnotation {

    @SerializedName("field")
    protected String field;

}
