@file:JvmName("Utils")
package com.vimeo.stag

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Assert
import java.lang.reflect.InvocationTargetException
import java.util.ArrayList
import java.util.HashMap

fun <T> testZeroArgumentConstructorFinalClass(clazz: Class<T>) {
    var exceptionThrown = false
    try {
        val constructor = clazz.getDeclaredConstructor()
        constructor.isAccessible = true
        constructor.newInstance()
    } catch (e: InvocationTargetException) {
        if (e.cause is UnsupportedOperationException) {
            exceptionThrown = true
        }
    }

    Assert.assertTrue(exceptionThrown)
}

fun <K, V> assertMapsEqual(map1: Map<K, V>, map2: Map<K, V>) {
    for ((key, value) in map1) {
        Assert.assertEquals(value, map2[key])
    }

    for ((key, value) in map2) {
        Assert.assertEquals(value, map1[key])
    }
}

fun createStringDummyList(): ArrayList<String> {
    val stringList = ArrayList<String>()

    stringList.add("abc")
    stringList.add("abc1")
    stringList.add("abc2")
    stringList.add("abc3")
    stringList.add("abc4")
    stringList.add("abc5")

    return stringList
}

fun createStringDummyMap(): HashMap<String, String> {
    val stringMap = HashMap<String, String>()

    stringMap["abc"] = "0"
    stringMap["abc1"] = "1"
    stringMap["abc2"] = "2"
    stringMap["abc3"] = "3"
    stringMap["abc4"] = "4"
    stringMap["abc5"] = "5"

    return stringMap
}

fun createIntegerDummyList(): ArrayList<Int> {
    val integerArrayList = ArrayList<Int>()

    integerArrayList.add(1)
    integerArrayList.add(2)
    integerArrayList.add(3)
    integerArrayList.add(4)
    integerArrayList.add(5)
    integerArrayList.add(6)

    return integerArrayList
}

fun createIntegerDummyMap(): HashMap<Int, Int> {
    val integerMap = HashMap<Int, Int>()

    integerMap[1] = 11
    integerMap[2] = 22
    integerMap[3] = 33
    integerMap[4] = 44
    integerMap[5] = 55
    integerMap[6] = 66

    return integerMap
}

fun createDummyJsonObject(): JsonObject {
    val jsonObject = JsonObject()

    jsonObject.addProperty("key", "value")
    jsonObject.addProperty("key1", "value1")
    jsonObject.addProperty("key2", "value2")
    jsonObject.addProperty("key3", "value3")
    jsonObject.addProperty("key4", "value4")

    return jsonObject
}

fun createDummyJsonArray(): JsonArray {
    val jsonArray = JsonArray()

    jsonArray.add("item1")
    jsonArray.add("item2")
    jsonArray.add("item3")
    jsonArray.add("item4")

    return jsonArray
}
