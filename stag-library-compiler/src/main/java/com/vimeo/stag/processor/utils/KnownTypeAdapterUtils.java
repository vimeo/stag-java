package com.vimeo.stag.processor.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
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

/**
 * This maintains a list of type vs the known type adapters.
 */
public final class KnownTypeAdapterUtils {

    @NotNull
    private static final HashMap<String, String> KNOWN_TYPE_ADAPTERS = new HashMap<>();

    @NotNull
    private static final HashMap<String, String> SUPPORTED_COLLECTION_INFO = new HashMap<>();

    @NotNull
    private static final HashMap<String, String> SUPPORTED_PRIMITIVE_ARRAY = new HashMap<>();

    static {
        KNOWN_TYPE_ADAPTERS.put(BitSet.class.getName(), "com.google.gson.internal.bind.TypeAdapters.BIT_SET");
        KNOWN_TYPE_ADAPTERS.put(Boolean.class.getName(), "com.google.gson.internal.bind.TypeAdapters.BOOLEAN");
        KNOWN_TYPE_ADAPTERS.put(boolean.class.getName(), "com.google.gson.internal.bind.TypeAdapters.BOOLEAN");
        KNOWN_TYPE_ADAPTERS.put(Byte.class.getName(), "com.vimeo.stag.KnownTypeAdapters.BYTE");
        KNOWN_TYPE_ADAPTERS.put(byte.class.getName(), "com.vimeo.stag.KnownTypeAdapters.BYTE");
        KNOWN_TYPE_ADAPTERS.put(Short.class.getName(), "com.vimeo.stag.KnownTypeAdapters.SHORT");
        KNOWN_TYPE_ADAPTERS.put(short.class.getName(), "com.vimeo.stag.KnownTypeAdapters.SHORT");
        KNOWN_TYPE_ADAPTERS.put(Integer.class.getName(), "com.vimeo.stag.KnownTypeAdapters.INTEGER");
        KNOWN_TYPE_ADAPTERS.put(int.class.getName(), "com.vimeo.stag.KnownTypeAdapters.INTEGER");
        KNOWN_TYPE_ADAPTERS.put(Long.class.getName(), "com.vimeo.stag.KnownTypeAdapters.LONG");
        KNOWN_TYPE_ADAPTERS.put(long.class.getName(), "com.vimeo.stag.KnownTypeAdapters.LONG");
        KNOWN_TYPE_ADAPTERS.put(Float.class.getName(), "com.vimeo.stag.KnownTypeAdapters.FLOAT");
        KNOWN_TYPE_ADAPTERS.put(float.class.getName(), "com.vimeo.stag.KnownTypeAdapters.FLOAT");
        KNOWN_TYPE_ADAPTERS.put(Double.class.getName(), "com.vimeo.stag.KnownTypeAdapters.DOUBLE");
        KNOWN_TYPE_ADAPTERS.put(double.class.getName(), "com.vimeo.stag.KnownTypeAdapters.DOUBLE");
        KNOWN_TYPE_ADAPTERS.put(Number.class.getName(), "com.google.gson.internal.bind.TypeAdapters.NUMBER");
        KNOWN_TYPE_ADAPTERS.put(Character.class.getName(), "com.google.gson.internal.bind.TypeAdapters.CHARACTER");
        KNOWN_TYPE_ADAPTERS.put(char.class.getName(), "com.google.gson.internal.bind.TypeAdapters.CHARACTER");
        KNOWN_TYPE_ADAPTERS.put(String.class.getName(), "com.google.gson.internal.bind.TypeAdapters.STRING");
        KNOWN_TYPE_ADAPTERS.put(BigDecimal.class.getName(), "com.google.gson.internal.bind.TypeAdapters.BIG_DECIMAL");
        KNOWN_TYPE_ADAPTERS.put(BigInteger.class.getName(), "com.google.gson.internal.bind.TypeAdapters.BIG_INTEGER");
        KNOWN_TYPE_ADAPTERS.put(AtomicBoolean.class.getName(), "com.google.gson.internal.bind.TypeAdapters.ATOMIC_BOOLEAN");
        KNOWN_TYPE_ADAPTERS.put(AtomicInteger.class.getName(), "com.google.gson.internal.bind.TypeAdapters.ATOMIC_INTEGER");
        KNOWN_TYPE_ADAPTERS.put(AtomicIntegerArray.class.getName(), "com.google.gson.internal.bind.TypeAdapters.ATOMIC_INTEGER_ARRAY");
        KNOWN_TYPE_ADAPTERS.put(Currency.class.getName(), "com.google.gson.internal.bind.TypeAdapters.CURRENCY");
        KNOWN_TYPE_ADAPTERS.put(Calendar.class.getName(), "com.google.gson.internal.bind.TypeAdapters.CALENDAR");
        KNOWN_TYPE_ADAPTERS.put(Number.class.getName(), "com.google.gson.internal.bind.TypeAdapters.NUMBER");
        KNOWN_TYPE_ADAPTERS.put(JsonElement.class.getName(), "com.vimeo.stag.KnownTypeAdapters.JSON_ELEMENT_TYPE_ADAPTER");
        KNOWN_TYPE_ADAPTERS.put(JsonObject.class.getName(), "com.vimeo.stag.KnownTypeAdapters.JSON_OBJECT_TYPE_ADAPTER");
        KNOWN_TYPE_ADAPTERS.put(JsonArray.class.getName(), "com.vimeo.stag.KnownTypeAdapters.JSON_ARRAY_TYPE_ADAPTER");
        KNOWN_TYPE_ADAPTERS.put(JsonPrimitive.class.getName(), "com.vimeo.stag.KnownTypeAdapters.JSON_PRIMITIVE_TYPE_ADAPTER");
        KNOWN_TYPE_ADAPTERS.put(JsonNull.class.getName(), "com.vimeo.stag.KnownTypeAdapters.JSON_NULL_TYPE_ADAPTER");

        SUPPORTED_COLLECTION_INFO.put(ArrayList.class.getName(), "com.vimeo.stag.KnownTypeAdapters.ArrayListInstantiator");
        SUPPORTED_COLLECTION_INFO.put(List.class.getName(), "com.vimeo.stag.KnownTypeAdapters.ListInstantiator");
        SUPPORTED_COLLECTION_INFO.put(Collection.class.getName(), "com.vimeo.stag.KnownTypeAdapters.CollectionInstantiator");

        SUPPORTED_PRIMITIVE_ARRAY.put(int[].class.getSimpleName(), "com.vimeo.stag.KnownTypeAdapters.PrimitiveIntegerArrayAdapter");
        SUPPORTED_PRIMITIVE_ARRAY.put(long[].class.getSimpleName(), "com.vimeo.stag.KnownTypeAdapters.PrimitiveLongArrayAdapter");
        SUPPORTED_PRIMITIVE_ARRAY.put(double[].class.getSimpleName(), "com.vimeo.stag.KnownTypeAdapters.PrimitiveDoubleArrayAdapter");
        SUPPORTED_PRIMITIVE_ARRAY.put(short[].class.getSimpleName(), "com.vimeo.stag.KnownTypeAdapters.PrimitiveShortArrayAdapter");
        SUPPORTED_PRIMITIVE_ARRAY.put(char[].class.getSimpleName(), "com.vimeo.stag.KnownTypeAdapters.PrimitiveCharArrayAdapter");
        SUPPORTED_PRIMITIVE_ARRAY.put(float[].class.getSimpleName(), "com.vimeo.stag.KnownTypeAdapters.PrimitiveFloatArrayAdapter");
        SUPPORTED_PRIMITIVE_ARRAY.put(boolean[].class.getSimpleName(), "com.vimeo.stag.KnownTypeAdapters.PrimitiveBooleanArrayAdapter");
        SUPPORTED_PRIMITIVE_ARRAY.put(byte[].class.getSimpleName(), "com.vimeo.stag.KnownTypeAdapters.PrimitiveByteArrayAdapter");
    }

    @Nullable
    public static String getKnownTypeAdapterForType(@NotNull TypeMirror typeMirror) {
        return KNOWN_TYPE_ADAPTERS.get(typeMirror.toString());
    }

    /**
     * Get the instantiator for {@link List} types
     *
     * @param typeMirror TypeMirror typeMirror
     * @return instantiator
     */
    @NotNull
    public static String getListInstantiator(@NotNull TypeMirror typeMirror) {
        String outerClassType = TypeUtils.getOuterClassType(typeMirror);
        DeclaredType declaredType = typeMirror instanceof DeclaredType ? (DeclaredType) typeMirror : null;
        TypeMirror valueType = declaredType != null && declaredType.getTypeArguments() != null &&
                               !declaredType.getTypeArguments().isEmpty() ? declaredType.getTypeArguments()
                .get(0) : null;
        String postFix = valueType != null ? "<" + valueType.toString() + ">()" : "()";
        return "new " + SUPPORTED_COLLECTION_INFO.get(outerClassType) + postFix;
    }

    /**
     * Get the instantiator for {@link Map} types
     *
     * @param typeMirror TypeMirror typeMirror
     * @return instantiator
     */
    @NotNull
    public static String getMapInstantiator(@NotNull TypeMirror typeMirror) {
        String outerClassType = TypeUtils.getOuterClassType(typeMirror);
        DeclaredType declaredType = typeMirror instanceof DeclaredType ? (DeclaredType) typeMirror : null;
        TypeMirror keyType = declaredType != null && declaredType.getTypeArguments() != null &&
                             declaredType.getTypeArguments().size() == 2 ? declaredType.getTypeArguments()
                .get(0) : null;
        TypeMirror paramType = declaredType != null && declaredType.getTypeArguments() != null &&
                               declaredType.getTypeArguments().size() == 2 ? declaredType.getTypeArguments()
                .get(1) : null;
        String postFix = keyType != null && paramType != null ?
                "<" + keyType.toString() + ", " + paramType.toString() + ">()" : "()";

        if (outerClassType.equals(Map.class.getName())) {
            return "new com.vimeo.stag.KnownTypeAdapters.MapInstantiator" + postFix;
        } else if (outerClassType.equals(HashMap.class.getName())) {
            return "new com.vimeo.stag.KnownTypeAdapters.HashMapInstantiator" + postFix;
        } else if (outerClassType.equals(LinkedHashMap.class.getName())) {
            return "new com.vimeo.stag.KnownTypeAdapters.LinkedHashMapInstantiator" + postFix;
        } else if (outerClassType.equals(ConcurrentHashMap.class.getName())) {
            return "new com.vimeo.stag.KnownTypeAdapters.ConcurrentHashMapInstantiator" + postFix;
        } else {
            String params = keyType != null && paramType != null ?
                    "<" + keyType.toString() + ", " + paramType.toString() + ">" : "";
            return "new com.google.gson.internal.ObjectConstructor<" + outerClassType + params + ">() " +
                   "{ " +
                   "\n@Override " +
                   "\npublic " + outerClassType + params + " construct() {" +
                   "\n\treturn new " + outerClassType + params + "();" +
                   "\n}" +
                   "}";
        }
    }

    /**
     * Get the instantiator for native array types
     *
     * @param typeMirror TypeMirror typeMirror
     * @return instantiator
     */
    @Nullable
    public static String getNativeArrayInstantiator(@NotNull TypeMirror typeMirror) {
        String outerClassType = TypeUtils.getOuterClassType(typeMirror);
        return "new com.vimeo.stag.KnownTypeAdapters.PrimitiveArrayConstructor<" + outerClassType +
               ">(){ @Override public " + outerClassType + "[] construct(int size){ return new " +
               outerClassType + "[size]; } }";
    }

    /**
     * Get the type adapter for primitive array types
     *
     * @param typeMirror TypeMirror typeMirror
     * @return adapterName
     */
    @Nullable
    public static String getNativePrimitiveArrayTypeAdapter(@NotNull TypeMirror typeMirror) {
        String outerClassType = TypeUtils.getOuterClassType(typeMirror);
        return SUPPORTED_PRIMITIVE_ARRAY.get(outerClassType);
    }
}