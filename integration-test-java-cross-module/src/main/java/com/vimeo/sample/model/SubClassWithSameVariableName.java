package com.vimeo.sample.model;

import com.vimeo.stag.UseStag;

import java.util.ArrayList;

@SuppressWarnings("FieldNameHidesFieldInSuperclass")
@UseStag
public class SubClassWithSameVariableName extends ClassWithArrayTypes {

    public ArrayList<String> stringList;
}
