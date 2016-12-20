package com.vimeo.stag.processor.utils;

import com.google.gson.JsonElement;
import com.vimeo.stag.KnownTypeAdapters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.lang.model.type.TypeMirror;

public class KnownTypeAdapterUtils {
    @NotNull
    private static final HashMap<Type, Class> mKnownCollectionTypeAdapters = new HashMap<>();
    @NotNull
    private static final HashMap<String, String> mKnownTypeAdapters = new HashMap<>();

    static {
        mKnownTypeAdapters.put(BitSet.class.getName(), "com.vimeo.stag.KnownTypeAdapters.BIT_SET");
        mKnownTypeAdapters.put(Boolean.class.getName(), "com.vimeo.stag.KnownTypeAdapters.BOOLEAN");
        mKnownTypeAdapters.put(byte.class.getName(), "com.vimeo.stag.KnownTypeAdapters.BYTE");
        mKnownTypeAdapters.put(short.class.getName(), "com.vimeo.stag.KnownTypeAdapters.SHORT");
        mKnownTypeAdapters.put(Integer.class.getName(), "com.vimeo.stag.KnownTypeAdapters.INTEGER");
        mKnownTypeAdapters.put(int.class.getName(), "com.vimeo.stag.KnownTypeAdapters.INTEGER");
        mKnownTypeAdapters.put(Long.class.getName(), "com.vimeo.stag.KnownTypeAdapters.LONG");
        mKnownTypeAdapters.put(long.class.getName(), "com.vimeo.stag.KnownTypeAdapters.LONG");
        mKnownTypeAdapters.put(Float.class.getName(), "com.vimeo.stag.KnownTypeAdapters.FLOAT");
        mKnownTypeAdapters.put(float.class.getName(), "com.vimeo.stag.KnownTypeAdapters.FLOAT");
        mKnownTypeAdapters.put(Double.class.getName(), "com.vimeo.stag.KnownTypeAdapters.DOUBLE");
        mKnownTypeAdapters.put(double.class.getName(), "com.vimeo.stag.KnownTypeAdapters.DOUBLE");
        mKnownTypeAdapters.put(Number.class.getName(), "com.vimeo.stag.KnownTypeAdapters.NUMBER");
        mKnownTypeAdapters.put(Character.class.getName(), "com.vimeo.stag.KnownTypeAdapters.CHARACTER");
        mKnownTypeAdapters.put(char.class.getName(), "com.vimeo.stag.KnownTypeAdapters.CHARACTER");
        mKnownTypeAdapters.put(String.class.getName(), "com.vimeo.stag.KnownTypeAdapters.STRING");
        mKnownTypeAdapters.put(BigDecimal.class.getName(), "com.vimeo.stag.KnownTypeAdapters.BIG_DECIMAL");
        mKnownTypeAdapters.put(BigInteger.class.getName(), "com.vimeo.stag.KnownTypeAdapters.BIG_INTEGER");
        mKnownTypeAdapters.put(Calendar.class.getName(), "com.vimeo.stag.KnownTypeAdapters.CALENDAR");
        mKnownTypeAdapters.put(JsonElement.class.getName(), "com.vimeo.stag.KnownTypeAdapters.JSON_ELEMENT");
    }

    @Nullable
    public static String getKnownTypeAdapterForType(String type) {
        return mKnownTypeAdapters.get(type);
    }

    @NotNull
    public static HashMap<Type, Class> getKnownCollectionTypeAdapters() {
        return mKnownCollectionTypeAdapters;
    }

    public static void initialize() {
        mKnownCollectionTypeAdapters.put(List.class, KnownTypeAdapters.ListTypeAdapter.class);
        mKnownCollectionTypeAdapters.put(Map.class, KnownTypeAdapters.MapTypeAdapter.class);
    }

    @NotNull
    public static String getListInstantiater(@NotNull TypeMirror typeMirror) {
        String outerClassType = TypeUtils.getOuterClassType(typeMirror);
        if (outerClassType.equals(ArrayList.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.ArrayListInstantiater";
        } else if (outerClassType.equals(List.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.ListInstantiater";
        } else if (outerClassType.equals(Collection.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.CollectionInstantiater";
        } else {
            return "com.vimeo.stag.KnownTypeAdapters.ArrayListInstantiater";
        }
    }

    @NotNull
    public static String getMapInstantiater(@NotNull TypeMirror typeMirror) {
        String outerClassType = TypeUtils.getOuterClassType(typeMirror);
        if (outerClassType.equals(Map.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.MapInstantiater";
        } else if (outerClassType.equals(HashMap.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.HashMapInstantiater";
        } else if (outerClassType.equals(LinkedHashMap.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.LinkedHashMapInstantiater";
        } else if (outerClassType.equals(ConcurrentHashMap.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.ConcurrentHashMapInstantiater";
        } else {
            return "com.vimeo.stag.KnownTypeAdapters.HashMapInstantiater";
        }
    }
}