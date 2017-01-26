package com.vimeo.sample.model;


import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.GsonAdapterKey;
import com.vimeo.stag.UseStag;
import com.vimeo.stag.UseStag.FieldOption;

/**
 * Model class to demonstrate the usage of FIELD_OPTION_SERIALIZED_NAME with SerializedName & GsonAdapterKey
 */
@UseStag(FieldOption.SERIALIZED_NAME)
public class FieldOptionsSerializedName2 {

    public int simpleInt;

    @SerializedName("simpleFloat")
    public float simpleFloat;

    @SerializedName("simpleShort")
    public short simpleShort;

    @GsonAdapterKey("simpleLong")
    public long simpleLong;

    @GsonAdapterKey("simpleDouble")
    public double simpleDouble;
}