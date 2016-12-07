package com.vimeo.sample.model;


import com.vimeo.stag.GsonAdapterKey;

import java.util.Collection;
import java.util.List;

/**
 * This class simulates a scenario the class has a recursive loop.
 * This can be inside the same class or could be in a class referenced by
 * this class which again refers back to this class
 */
public class RecursiveClass {
    @GsonAdapterKey
    public String checkSum;

    @GsonAdapterKey
    public String[] stringNativeArray;

    @GsonAdapterKey
    public Integer integerObject;

    @GsonAdapterKey
    public List<Integer> integerArray;

    @GsonAdapterKey
    public Collection<Integer> collection;

    @GsonAdapterKey
    public int[] nativeIntegerArray;

    @GsonAdapterKey
    public RecursiveClass innerSum;
}
