package com.vimeo.sample.model;


import com.vimeo.stag.UseStag;
import com.vimeo.stag.UseStag.FieldOption;

/**
 * Model class which uses FIELD_OPTION_ALL explicitly
 */
@UseStag(FieldOption.ALL)
public class FieldOptionAllExample {

    public int simpleInt;

    public float simpleFloat;

    public short simpleShort;

    public long simpleLong;

    public double simpleDouble;
}