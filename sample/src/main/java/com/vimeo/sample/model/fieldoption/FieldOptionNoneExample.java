package com.vimeo.sample.model.fieldoption;


import com.vimeo.stag.UseStag;
import com.vimeo.stag.UseStag.FieldOption;

/**
 * Model class to demonstrate the usage of FIELD_OPTION_NONE
 */
@UseStag(FieldOption.NONE)
public class FieldOptionNoneExample {

    public int simpleInt;

    public float simpleFloat;

    public short simpleShort;

    public long simpleLong;

    public double simpleDouble;
}