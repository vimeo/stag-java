package com.vimeo.sample.model;


import android.support.annotation.NonNull;

import com.vimeo.stag.UseStag;

/**
 * Model class which contains different types of primitive member variables
 */
@UseStag
public class PrimitiveTypesExample {

    /**
     * Test that non null check ignores primitive types
     */
    @SuppressWarnings("NullableProblems")
    @NonNull
    public int simpleInt;

    public float simpleFloat;

    public short simpleShort;

    public long simpleLong;

    public double simpleDouble;
}