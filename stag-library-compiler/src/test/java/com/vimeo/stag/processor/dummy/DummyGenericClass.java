package com.vimeo.stag.processor.dummy;

import com.vimeo.stag.processor.TypeUtilsUnitTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Do not change this class without sure the
 * {@link TypeUtilsUnitTest#getConcreteMembers_isCorrect()}
 * test still works correctly. All members of
 * this class should be tested by that test,
 * and any generic ones here should be explicitly
 * checked in the test to make sure they are
 * resolved correctly.
 *
 * @param <T> the type the the inheriting type
 *            should be of.
 */
public class DummyGenericClass<T> {

    String testString;

    T testObject;

    ArrayList<T> testList;

    HashMap<String, T> testMap;

    HashSet<T> testSet;

}
