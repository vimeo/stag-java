package com.vimeo.sample.model;


import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

import java.util.Collection;
import java.util.List;

/**
 * This class simulates a scenario the class has a recursive loop.
 * This can be inside the same class or could be in a class referenced by
 * this class which again refers back to this class
 */
@UseStag
public class RecursiveClass {
    @SerializedName("checkSum")
    public String checkSum;

    @SerializedName("stringNativeArray")
    public String[] stringNativeArray;

    @SerializedName("integerObject")
    public Integer integerObject;

    @SerializedName("integerArray")
    public List<Integer> integerArray;

    @SerializedName("collection")
    public Collection<Integer> collection;

    @SerializedName("nativeIntegerArray")
    public int[] nativeIntegerArray;

    @SerializedName("innerSum")
    public RecursiveClass innerSum;
}