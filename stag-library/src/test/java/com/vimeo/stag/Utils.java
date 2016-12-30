package com.vimeo.stag;

import java.util.ArrayList;

public class Utils {

    public static ArrayList<String> createStringDummyList() {
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("abc");
        stringList.add("abc1");
        stringList.add("abc2");
        stringList.add("abc3");
        stringList.add("abc4");
        stringList.add("abc5");
        return stringList;
    }

    public static ArrayList<Integer> createIntegerDummyList() {
        ArrayList<Integer> integerArrayList = new ArrayList<>();
        integerArrayList.add(1);
        integerArrayList.add(2);
        integerArrayList.add(3);
        integerArrayList.add(4);
        integerArrayList.add(5);
        integerArrayList.add(6);
        return integerArrayList;
    }
}