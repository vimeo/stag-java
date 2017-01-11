package com.vimeo.sample.model;


import com.vimeo.stag.UseStag;

/**
 * Model class which uses FIELD_OPTION_ALL explicitly
 */
@UseStag(UseStag.FIELD_OPTION_ALL)
public class FieldOptionAllExample {

    public int simpleInt;

    public float simpleFloat;

    public short simpleShort;

    public long simpleLong;

    public double simpleDouble;
}