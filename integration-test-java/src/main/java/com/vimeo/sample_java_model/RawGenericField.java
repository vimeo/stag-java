package com.vimeo.sample_java_model;

import com.vimeo.stag.UseStag;

import java.util.Map;

/**
 * Model which references a generically typed object without generic bounds (i.e., as a raw
 * type).
 */
@UseStag
public class RawGenericField {

    @SuppressWarnings("rawtypes")
    public ExternalModelGeneric rawTypedField;

    /**
     * Tests that map keys and values are handled correctly.
     */
    @SuppressWarnings("rawtypes")
    public Map rawMap;

}
