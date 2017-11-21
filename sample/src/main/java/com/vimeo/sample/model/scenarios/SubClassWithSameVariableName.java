package com.vimeo.sample.model.scenarios;

import com.vimeo.sample.model.ClassWithArrayTypes;
import com.vimeo.stag.UseStag;

import java.util.ArrayList;

@UseStag
public class SubClassWithSameVariableName extends ClassWithArrayTypes {

    public ArrayList<String> stringList;
}
