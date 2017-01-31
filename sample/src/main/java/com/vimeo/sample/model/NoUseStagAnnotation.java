package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

/**
 * This class should have a TypeAdapter created for it
 * for backwards compatibility.
 * <p>
 * Created by restainoa on 1/30/17.
 */
public class NoUseStagAnnotation {

    @GsonAdapterKey
    protected String field;

}
