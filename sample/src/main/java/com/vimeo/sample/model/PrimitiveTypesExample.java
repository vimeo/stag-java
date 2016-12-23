package com.vimeo.sample.model;


import com.vimeo.stag.GsonAdapterKey;
import com.vimeo.stag.UseStag;

/**
 * Model class which contains different types of primitive member variables
 */
@UseStag
public class PrimitiveTypesExample {

    @GsonAdapterKey("simpleInt")
    public int simpleInt;

    @GsonAdapterKey("simpleFlot")
    public float simpleFlot;

    @GsonAdapterKey("simpleShort")
    public short simpleShort;

    @GsonAdapterKey("simpleLong")
    public long simpleLong;

    @GsonAdapterKey("simpleDouble")
    public double simpleDouble;
}
