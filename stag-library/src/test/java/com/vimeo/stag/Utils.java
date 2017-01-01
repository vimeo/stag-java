package com.vimeo.stag;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

    public static JsonObject createDummyJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key", "value");
        jsonObject.addProperty("key1", "value1");
        jsonObject.addProperty("key2", "value2");
        jsonObject.addProperty("key3", "value3");
        jsonObject.addProperty("key4", "value4");
        return jsonObject;
    }

    public static JsonArray createDummyJsonArray() {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("item1");
        jsonArray.add("item2");
        jsonArray.add("item3");
        jsonArray.add("item4");
        return jsonArray;
    }
}