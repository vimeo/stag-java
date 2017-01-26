package com.vimeo.sample.model;


import com.vimeo.stag.GsonAdapterKey;
import com.vimeo.stag.UseStag;
import com.vimeo.stag.UseStag.FieldOption;

/**
 * Model class to demonstrate the usage of FIELD_OPTION_SERIALIZED_NAME with GsonAdapterKey
 */
@UseStag(FieldOption.SERIALIZED_NAME)
public class FieldOptionsSerializedName3 {

    public int simpleInt;

    @GsonAdapterKey("simpleFloat")
    public float simpleFloat;

    @GsonAdapterKey("simpleShort")
    public short simpleShort;

    @GsonAdapterKey("simpleLong")
    public long simpleLong;

    @GsonAdapterKey("simpleDouble")
    public double simpleDouble;
}