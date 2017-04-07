package com.vimeo.stag;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Utils {

    public static <K, V> void assertMapsEqual(Map<K, V> map1, Map<K, V> map2) throws Exception {
        for (Entry<K, V> kvEntry : map1.entrySet()) {
            Assert.assertEquals(kvEntry.getValue(), map2.get(kvEntry.getKey()));
        }

        for (Entry<K, V> kvEntry : map2.entrySet()) {
            Assert.assertEquals(kvEntry.getValue(), map1.get(kvEntry.getKey()));
        }
    }

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

    public static HashMap<String, String> createStringDummyMap() {
        HashMap<String, String> stringMap = new HashMap<>();

        stringMap.put("abc", "0");
        stringMap.put("abc1", "1");
        stringMap.put("abc2", "2");
        stringMap.put("abc3", "3");
        stringMap.put("abc4", "4");
        stringMap.put("abc5", "5");

        return stringMap;
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

    public static HashMap<Integer, Integer> createIntegerDummyMap() {
        HashMap<Integer, Integer> integerMap = new HashMap<>();

        integerMap.put(1, 11);
        integerMap.put(2, 22);
        integerMap.put(3, 33);
        integerMap.put(4, 44);
        integerMap.put(5, 55);
        integerMap.put(6, 66);

        return integerMap;
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