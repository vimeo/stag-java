package com.vimeo.sample.model;


import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public class PrimitiveTypesExample {

    @SerializedName("simpleInt")
    public int simpleInt;

    @SerializedName("simpleFlot")
    public float simpleFlot;


    @SerializedName("simpleShort")
    public short simpleShort;

    @SerializedName("simpleLong")
    public long simpleLong;

    @SerializedName("simpleDouble")
    public double simpleDouble;
}
