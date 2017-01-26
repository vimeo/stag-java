package com.vimeo.sample.model;

import com.vimeo.stag.UseStag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@UseStag
public class ClassWithArrayTypes {

    public List<String> stringList;

    public Collection<Integer> integerCollection;

    public ArrayList<Long> longArrayList;
}
