@file:JvmName("Utils")

package com.vimeo.stag

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.util.ArrayList
import java.util.HashMap

inline fun <reified T : Any> assertThatClassIsNotInstantiable() {
    assertThatThrownBy {
        T::class.java.getDeclaredConstructor().apply {
            assertThat(isAccessible).isFalse()
            isAccessible = true
        }.newInstance()
    }.hasCauseInstanceOf(UnsupportedOperationException::class.java)
}

fun <K, V> assertMapsEqual(map1: Map<K, V>, map2: Map<K, V>) {
    for ((key, value) in map1) {
        assertThat(value).isEqualTo(map2[key])
    }

    for ((key, value) in map2) {
        assertThat(value).isEqualTo(map1[key])
    }
}

fun createStringDummyList() = ArrayList<String>().apply {
    add("abc")
    add("abc1")
    add("abc2")
    add("abc3")
    add("abc4")
    add("abc5")
}

fun createStringDummyMap() = HashMap<String, String>().apply {
    this["abc"] = "0"
    this["abc1"] = "1"
    this["abc2"] = "2"
    this["abc3"] = "3"
    this["abc4"] = "4"
    this["abc5"] = "5"
}

fun createIntegerDummyList() = ArrayList<Int>().apply {
    add(1)
    add(2)
    add(3)
    add(4)
    add(5)
    add(6)
}

fun createIntegerDummyMap() = HashMap<Int, Int>().apply {
    this[1] = 11
    this[2] = 22
    this[3] = 33
    this[4] = 44
    this[5] = 55
    this[6] = 66
}

fun createDummyJsonObject() = JsonObject().apply {
    addProperty("key", "value")
    addProperty("key1", "value1")
    addProperty("key2", "value2")
    addProperty("key3", "value3")
    addProperty("key4", "value4")
}

fun createDummyJsonArray() = JsonArray().apply {
    add("item1")
    add("item2")
    add("item3")
    add("item4")
}
