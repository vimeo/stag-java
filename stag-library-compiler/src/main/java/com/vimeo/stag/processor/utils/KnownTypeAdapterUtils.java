package com.vimeo.stag.processor.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.bind.TypeAdapters;
import com.vimeo.stag.KnownTypeAdapters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
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

import static com.vimeo.stag.processor.utils.TypeUtils.className;

/**
 * This maintains a list of type vs the known type adapters.
 */
public final class KnownTypeAdapterUtils {

    @NotNull
    private static final HashMap<String, String> KNOWN_TYPE_ADAPTERS = new HashMap<>();

    @NotNull
    private static final HashMap<String, String> SUPPORTED_COLLECTION_INSTANTIATORS = new HashMap<>();

    @NotNull
    private static final HashMap<String, String> SUPPORTED_MAP_INSTANTIATORS = new HashMap<>();

    @NotNull
    private static final HashMap<String, String> KNOWN_PRIMITIVE_ARRAY_TYPE_ADAPTERS = new HashMap<>();

    static {
        KNOWN_TYPE_ADAPTERS.put(BitSet.class.getName(), typeAdapters(TypeAdapters.BIT_SET));
        KNOWN_TYPE_ADAPTERS.put(Boolean.class.getName(), typeAdapters(TypeAdapters.BOOLEAN));
        KNOWN_TYPE_ADAPTERS.put(boolean.class.getName(), typeAdapters(TypeAdapters.BOOLEAN));
        KNOWN_TYPE_ADAPTERS.put(Byte.class.getName(), knownTypeAdapters(KnownTypeAdapters.BYTE));
        KNOWN_TYPE_ADAPTERS.put(byte.class.getName(), knownTypeAdapters(KnownTypeAdapters.BYTE));
        KNOWN_TYPE_ADAPTERS.put(Short.class.getName(), knownTypeAdapters(KnownTypeAdapters.SHORT));
        KNOWN_TYPE_ADAPTERS.put(short.class.getName(), knownTypeAdapters(KnownTypeAdapters.SHORT));
        KNOWN_TYPE_ADAPTERS.put(Integer.class.getName(), knownTypeAdapters(KnownTypeAdapters.INTEGER));
        KNOWN_TYPE_ADAPTERS.put(int.class.getName(), knownTypeAdapters(KnownTypeAdapters.INTEGER));
        KNOWN_TYPE_ADAPTERS.put(Long.class.getName(), knownTypeAdapters(KnownTypeAdapters.LONG));
        KNOWN_TYPE_ADAPTERS.put(long.class.getName(), knownTypeAdapters(KnownTypeAdapters.LONG));
        KNOWN_TYPE_ADAPTERS.put(Float.class.getName(), knownTypeAdapters(KnownTypeAdapters.FLOAT));
        KNOWN_TYPE_ADAPTERS.put(float.class.getName(), knownTypeAdapters(KnownTypeAdapters.FLOAT));
        KNOWN_TYPE_ADAPTERS.put(Double.class.getName(), knownTypeAdapters(KnownTypeAdapters.DOUBLE));
        KNOWN_TYPE_ADAPTERS.put(double.class.getName(), knownTypeAdapters(KnownTypeAdapters.DOUBLE));
        KNOWN_TYPE_ADAPTERS.put(Number.class.getName(), typeAdapters(TypeAdapters.NUMBER));
        KNOWN_TYPE_ADAPTERS.put(Character.class.getName(), typeAdapters(TypeAdapters.CHARACTER));
        KNOWN_TYPE_ADAPTERS.put(char.class.getName(), typeAdapters(TypeAdapters.CHARACTER));
        KNOWN_TYPE_ADAPTERS.put(String.class.getName(), typeAdapters(TypeAdapters.STRING));
        KNOWN_TYPE_ADAPTERS.put(BigDecimal.class.getName(), typeAdapters(TypeAdapters.BIG_DECIMAL));
        KNOWN_TYPE_ADAPTERS.put(BigInteger.class.getName(), typeAdapters(TypeAdapters.BIG_INTEGER));
        KNOWN_TYPE_ADAPTERS.put(AtomicBoolean.class.getName(), typeAdapters(TypeAdapters.ATOMIC_BOOLEAN));
        KNOWN_TYPE_ADAPTERS.put(AtomicInteger.class.getName(), typeAdapters(TypeAdapters.ATOMIC_INTEGER));
        KNOWN_TYPE_ADAPTERS.put(AtomicIntegerArray.class.getName(), typeAdapters(TypeAdapters.ATOMIC_INTEGER_ARRAY));
        KNOWN_TYPE_ADAPTERS.put(Currency.class.getName(), typeAdapters(TypeAdapters.CURRENCY));
        KNOWN_TYPE_ADAPTERS.put(Calendar.class.getName(), typeAdapters(TypeAdapters.CALENDAR));
        KNOWN_TYPE_ADAPTERS.put(JsonElement.class.getName(), knownTypeAdapters(KnownTypeAdapters.JSON_ELEMENT));
        KNOWN_TYPE_ADAPTERS.put(JsonObject.class.getName(), knownTypeAdapters(KnownTypeAdapters.JSON_OBJECT));
        KNOWN_TYPE_ADAPTERS.put(JsonArray.class.getName(), knownTypeAdapters(KnownTypeAdapters.JSON_ARRAY));
        KNOWN_TYPE_ADAPTERS.put(JsonPrimitive.class.getName(), knownTypeAdapters(KnownTypeAdapters.JSON_PRIMITIVE));
        KNOWN_TYPE_ADAPTERS.put(JsonNull.class.getName(), knownTypeAdapters(KnownTypeAdapters.JSON_NULL));

        KNOWN_PRIMITIVE_ARRAY_TYPE_ADAPTERS.put(int[].class.getSimpleName(), className(KnownTypeAdapters.PrimitiveIntegerArrayAdapter.class));
        KNOWN_PRIMITIVE_ARRAY_TYPE_ADAPTERS.put(long[].class.getSimpleName(), className(KnownTypeAdapters.PrimitiveLongArrayAdapter.class));
        KNOWN_PRIMITIVE_ARRAY_TYPE_ADAPTERS.put(double[].class.getSimpleName(), className(KnownTypeAdapters.PrimitiveDoubleArrayAdapter.class));
        KNOWN_PRIMITIVE_ARRAY_TYPE_ADAPTERS.put(short[].class.getSimpleName(), className(KnownTypeAdapters.PrimitiveShortArrayAdapter.class));
        KNOWN_PRIMITIVE_ARRAY_TYPE_ADAPTERS.put(char[].class.getSimpleName(), className(KnownTypeAdapters.PrimitiveCharArrayAdapter.class));
        KNOWN_PRIMITIVE_ARRAY_TYPE_ADAPTERS.put(float[].class.getSimpleName(), className(KnownTypeAdapters.PrimitiveFloatArrayAdapter.class));
        KNOWN_PRIMITIVE_ARRAY_TYPE_ADAPTERS.put(boolean[].class.getSimpleName(), className(KnownTypeAdapters.PrimitiveBooleanArrayAdapter.class));
        KNOWN_PRIMITIVE_ARRAY_TYPE_ADAPTERS.put(byte[].class.getSimpleName(), className(KnownTypeAdapters.PrimitiveByteArrayAdapter.class));

        SUPPORTED_COLLECTION_INSTANTIATORS.put(ArrayList.class.getName(), className(KnownTypeAdapters.ArrayListInstantiator.class));
        SUPPORTED_COLLECTION_INSTANTIATORS.put(List.class.getName(), className(KnownTypeAdapters.ListInstantiator.class));
        SUPPORTED_COLLECTION_INSTANTIATORS.put(Collection.class.getName(), className(KnownTypeAdapters.CollectionInstantiator.class));

        SUPPORTED_MAP_INSTANTIATORS.put(Map.class.getName(), className(KnownTypeAdapters.MapInstantiator.class));
        SUPPORTED_MAP_INSTANTIATORS.put(HashMap.class.getName(), className(KnownTypeAdapters.HashMapInstantiator.class));
        SUPPORTED_MAP_INSTANTIATORS.put(LinkedHashMap.class.getName(), className(KnownTypeAdapters.LinkedHashMapInstantiator.class));
        SUPPORTED_MAP_INSTANTIATORS.put(ConcurrentHashMap.class.getName(), className(KnownTypeAdapters.ConcurrentHashMapInstantiator.class));
    }

    @NotNull
    private static String typeAdapters(@NotNull Object object) {
        return fieldToString(TypeAdapters.class, object);
    }

    @NotNull
    private static String knownTypeAdapters(@NotNull Object object) {
        return fieldToString(KnownTypeAdapters.class, object);
    }

    @NotNull
    private static String fieldToString(@NotNull Class clazz, @NotNull Object object) {
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            try {
                if (field.get(null) == object) {
                    return clazz.getName() + '.' + field.getName();
                }
            } catch (IllegalAccessException e) {
                DebugLog.log(e.getMessage());
            }
        }

        throw new IllegalStateException("Unable to find field: " + clazz.getName());
    }

    private KnownTypeAdapterUtils() {
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
        return "new " + SUPPORTED_COLLECTION_INSTANTIATORS.get(outerClassType) + postFix;
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

        String instantiator = SUPPORTED_MAP_INSTANTIATORS.get(outerClassType);

        if (instantiator != null) {
            return "new " + instantiator + postFix;
        } else {
            String params = keyType != null && paramType != null ?
                    "<" + keyType.toString() + ", " + paramType.toString() + ">" : "";
            return "new " + className(com.google.gson.internal.ObjectConstructor.class) + "<" +
                   outerClassType + params + ">() " +
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
        return "new " + className(com.vimeo.stag.KnownTypeAdapters.PrimitiveArrayConstructor.class) + "<" +
               outerClassType +
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
        return KNOWN_PRIMITIVE_ARRAY_TYPE_ADAPTERS.get(outerClassType);
    }
}