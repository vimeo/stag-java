package com.vimeo.stag.processor.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class KnownTypeAdapterUtils {
    @NotNull
    private static final HashMap<String, String> mKnownTypeAdapters = new HashMap<>();

    static {
        mKnownTypeAdapters.put(BitSet.class.getName(), "com.google.gson.internal.bind.TypeAdapters.BIT_SET");
        mKnownTypeAdapters.put(Boolean.class.getName(), "com.google.gson.internal.bind.TypeAdapters.BOOLEAN");
        mKnownTypeAdapters.put(boolean.class.getName(), "com.google.gson.internal.bind.TypeAdapters.BOOLEAN");
        mKnownTypeAdapters.put(Byte.class.getName(), "com.vimeo.stag.KnownTypeAdapters.BYTE");
        mKnownTypeAdapters.put(byte.class.getName(), "com.vimeo.stag.KnownTypeAdapters.BYTE");
        mKnownTypeAdapters.put(Short.class.getName(), "com.vimeo.stag.KnownTypeAdapters.SHORT");
        mKnownTypeAdapters.put(short.class.getName(), "com.vimeo.stag.KnownTypeAdapters.SHORT");
        mKnownTypeAdapters.put(Integer.class.getName(), "com.vimeo.stag.KnownTypeAdapters.INTEGER");
        mKnownTypeAdapters.put(int.class.getName(), "com.vimeo.stag.KnownTypeAdapters.INTEGER");
        mKnownTypeAdapters.put(Long.class.getName(), "com.vimeo.stag.KnownTypeAdapters.LONG");
        mKnownTypeAdapters.put(long.class.getName(), "com.vimeo.stag.KnownTypeAdapters.LONG");
        mKnownTypeAdapters.put(Float.class.getName(), "com.vimeo.stag.KnownTypeAdapters.FLOAT");
        mKnownTypeAdapters.put(float.class.getName(), "com.vimeo.stag.KnownTypeAdapters.FLOAT");
        mKnownTypeAdapters.put(Double.class.getName(), "com.vimeo.stag.KnownTypeAdapters.DOUBLE");
        mKnownTypeAdapters.put(double.class.getName(), "com.vimeo.stag.KnownTypeAdapters.DOUBLE");
        mKnownTypeAdapters.put(Number.class.getName(), "com.google.gson.internal.bind.TypeAdapters.NUMBER");
        mKnownTypeAdapters.put(Character.class.getName(), "com.google.gson.internal.bind.TypeAdapters.CHARACTER");
        mKnownTypeAdapters.put(char.class.getName(), "com.google.gson.internal.bind.TypeAdapters.CHARACTER");
        mKnownTypeAdapters.put(String.class.getName(), "com.google.gson.internal.bind.TypeAdapters.STRING");
        mKnownTypeAdapters.put(BigDecimal.class.getName(), "com.google.gson.internal.bind.TypeAdapters.BIG_DECIMAL");
        mKnownTypeAdapters.put(BigInteger.class.getName(), "com.google.gson.internal.bind.TypeAdapters.BIG_INTEGER");
        mKnownTypeAdapters.put(AtomicBoolean.class.getName(), "com.google.gson.internal.bind.TypeAdapters.ATOMIC_BOOLEAN");
        mKnownTypeAdapters.put(AtomicInteger.class.getName(), "com.google.gson.internal.bind.TypeAdapters.ATOMIC_INTEGER");
        mKnownTypeAdapters.put(AtomicIntegerArray.class.getName(), "com.google.gson.internal.bind.TypeAdapters.ATOMIC_INTEGER_ARRAY");
        mKnownTypeAdapters.put(Currency.class.getName(), "com.google.gson.internal.bind.TypeAdapters.CURRENCY");
        mKnownTypeAdapters.put(Calendar.class.getName(), "com.google.gson.internal.bind.TypeAdapters.CALENDAR");
        mKnownTypeAdapters.put(Number.class.getName(), "com.google.gson.internal.bind.TypeAdapters.NUMBER");
        mKnownTypeAdapters.put(JsonElement.class.getName(), "com.google.gson.internal.bind.TypeAdapters.JSON_ELEMENT");
        mKnownTypeAdapters.put(Date.class.getName(), "com.vimeo.stag.KnownTypeAdapters.DATE_TYPE_ADAPTER");
    }

    @Nullable
    public static String getKnownTypeAdapterForType(String type) {
        return mKnownTypeAdapters.get(type);
    }

    @NotNull
    public static String getListInstantiater(@NotNull TypeMirror typeMirror) {
        String outerClassType = TypeUtils.getOuterClassType(typeMirror);
        DeclaredType declaredType = typeMirror instanceof DeclaredType ? (DeclaredType) typeMirror : null;
        TypeMirror valueType = declaredType != null && declaredType.getTypeArguments() != null && !declaredType.getTypeArguments().isEmpty() ? declaredType.getTypeArguments().get(0) : null;
        String postFix = valueType != null ? "<" + valueType.toString() + ">()" : "()";
        if (outerClassType.equals(ArrayList.class.getName())) {
            return "new com.vimeo.stag.KnownTypeAdapters.ArrayListInstantiater" + postFix;
        } else if (outerClassType.equals(List.class.getName())) {
            return "new com.vimeo.stag.KnownTypeAdapters.ListInstantiater" + postFix;
        } else if (outerClassType.equals(Collection.class.getName())) {
            return "new com.vimeo.stag.KnownTypeAdapters.CollectionInstantiater" + postFix;
        } else {
            return "new com.vimeo.stag.KnownTypeAdapters.ArrayListInstantiater" + postFix;
        }
    }

    @NotNull
    public static String getMapInstantiater(@NotNull TypeMirror typeMirror) {
        String outerClassType = TypeUtils.getOuterClassType(typeMirror);
        DeclaredType declaredType = typeMirror instanceof DeclaredType ? (DeclaredType) typeMirror : null;
        TypeMirror keyType = declaredType != null && declaredType.getTypeArguments() != null && declaredType.getTypeArguments().size() == 2 ? declaredType.getTypeArguments().get(0) : null;
        TypeMirror paramType = declaredType != null && declaredType.getTypeArguments() != null && declaredType.getTypeArguments().size() == 2 ? declaredType.getTypeArguments().get(1) : null;
        String postFix = keyType != null ? "<" + keyType.toString() + ", " + paramType.toString() + ">()" : "()";

        if (outerClassType.equals(Map.class.getName())) {
            return "new com.vimeo.stag.KnownTypeAdapters.MapInstantiater" + postFix;
        } else if (outerClassType.equals(HashMap.class.getName())) {
            return "new com.vimeo.stag.KnownTypeAdapters.HashMapInstantiater" + postFix;
        } else if (outerClassType.equals(LinkedHashMap.class.getName())) {
            return "new com.vimeo.stag.KnownTypeAdapters.LinkedHashMapInstantiater" + postFix;
        } else if (outerClassType.equals(ConcurrentHashMap.class.getName())) {
            return "new com.vimeo.stag.KnownTypeAdapters.ConcurrentHashMapInstantiater" + postFix;
        } else {
            return "new com.vimeo.stag.KnownTypeAdapters.HashMapInstantiater" + postFix;
        }
    }

    @Nullable
    public static String getNativeArrayInstantiater(@NotNull TypeMirror typeMirror) {
        String outerClassType = TypeUtils.getOuterClassType(typeMirror);
        return "new com.vimeo.stag.KnownTypeAdapters.PrimitiveArrayConstructor<" + outerClassType + ">(){ @Override public " + outerClassType + "[] construct(int size){ return new " + outerClassType + "[size]; } }";
    }

    @Nullable
    public static String getNativePrimitiveArrayTypeAdapter(@NotNull TypeMirror typeMirror) {
        String outerClassType = TypeUtils.getOuterClassType(typeMirror);
        if (outerClassType.equals(int[].class.getSimpleName())) {
            return "com.vimeo.stag.KnownTypeAdapters.PrimitiveIntegerArrayAdapter";
        } else if (outerClassType.equals(long[].class.getSimpleName())) {
            return "com.vimeo.stag.KnownTypeAdapters.PrimitiveLongArrayAdapter";
        } else if (outerClassType.equals(double[].class.getSimpleName())) {
            return "com.vimeo.stag.KnownTypeAdapters.PrimitiveDoubleArrayAdapter";
        } else if (outerClassType.equals(short[].class.getSimpleName())) {
            return "com.vimeo.stag.KnownTypeAdapters.PrimitiveShortArrayAdapter";
        }
        return null;
    }

    @Nullable
    public static String getJsonElementTypeAdapter(@NotNull TypeMirror typeMirror) {
        String outerClassType = TypeUtils.getOuterClassType(typeMirror);
        System.out.println(outerClassType);
        if (outerClassType.equals(JsonElement.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.JsonElementTypeAdapter";
        } else if (outerClassType.equals(JsonArray.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.JsonArrayTypeAdapter";
        } else if (outerClassType.equals(JsonObject.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.JsonObjectTypeAdapter";
        }
        return null;
    }
}