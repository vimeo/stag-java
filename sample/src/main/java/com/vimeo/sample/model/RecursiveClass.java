package com.vimeo.sample.model;

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
    public String checkSum;

    public String[] stringNativeArray;

    public Integer integerObject;

    public List<Integer> integerArray;

    public Collection<Integer> collection;

    public int[] nativeIntegerArray;

    public RecursiveClass innerSum;
}
