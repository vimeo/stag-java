/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vimeo.stag;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains a list of KnownTypeAdapters such as {@link MapTypeAdapter}, {@link ListTypeAdapter} and more.
 * These type adapters are referenced in the Stag Compiler library, where we generate the code for the type adapters.
 * If the type is known to us, such as in case of {@link Integer} or {@link JsonArray} or {@link Double} etc, we
 * directly access the corresponding type adapters and call {@link TypeAdapter#read(JsonReader)} and {@link TypeAdapter#write(JsonWriter, Object)}
 * respectively.
 * <p>
 * --------------------- {@link MapTypeAdapter} -------------------
 * <p>
 * In case of {@link MapTypeAdapter}, we need to pass the keyTypeAdapter, valueTypeAdapter and the ObjectConstructor. The keyTypeAdapter, and valueTypeAdapter
 * are used to read and write keys and values respectively, and the ObjectConstructor tells the type of Map to be instantiated. This will also support the scenario
 * where we have a nested maps. In that case the valueTypeAdapter will be again a {@link MapTypeAdapter} with its key and value TypeAdapters
 * <p>
 * --------------------- {@link ListTypeAdapter} -------------------
 * <p>
 * In case of {@link ListTypeAdapter}, we need to pass the valueTypeAdapter and the ObjectConstructor. The valueTypeAdapter
 * is used to read and write the values, and the ObjectConstructor tells the type of List to be instantiated. This will also support the scenario
 * where we have a nested list. In that case the valueTypeAdapter will be again a {@link ListTypeAdapter} with its value TypeAdapter
 */
public final class KnownTypeAdapters {

    private KnownTypeAdapters() {
        throw new IllegalStateException("KnownTypeAdapters cannot be instantiated");
    }

    /**
     * Type Adapter for {@link Byte}.
     */
    public static final TypeAdapter<Byte> BYTE = new TypeAdapter<Byte>() {
        @Override
        public Byte read(JsonReader in) throws IOException {
            try {
                int intValue = in.nextInt();
                return (byte) intValue;
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public void write(JsonWriter out, Byte value) throws IOException {
            out.value(value);
        }
    }.nullSafe();

    /**
     * Type Adapter for {@link Short}.
     */
    public static final TypeAdapter<Short> SHORT = new TypeAdapter<Short>() {
        @Override
        public Short read(JsonReader in) throws IOException {
            try {
                return (short) in.nextInt();
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public void write(JsonWriter out, Short value) throws IOException {
            out.value(value);
        }
    }.nullSafe();

    /**
     * Type Adapter for {@link Integer}.
     */
    public static final TypeAdapter<Integer> INTEGER = new TypeAdapter<Integer>() {
        @Override
        public Integer read(JsonReader in) throws IOException {
            try {
                return in.nextInt();
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public void write(JsonWriter out, Integer value) throws IOException {
            out.value(value);
        }
    }.nullSafe();

    /**
     * Type Adapter for {@link Long}.
     */
    public static final TypeAdapter<Long> LONG = new TypeAdapter<Long>() {
        @Override
        public Long read(JsonReader in) throws IOException {
            try {
                return in.nextLong();
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public void write(JsonWriter out, Long value) throws IOException {
            out.value(value);
        }
    }.nullSafe();

    /**
     * Type Adapter for {@link Float}.
     */
    public static final TypeAdapter<Float> FLOAT = new TypeAdapter<Float>() {
        @Override
        public Float read(JsonReader in) throws IOException {
            return (float) in.nextDouble();
        }

        @Override
        public void write(JsonWriter out, Float value) throws IOException {
            out.value(value);
        }
    }.nullSafe();

    /**
     * Type Adapter for {@link Double}.
     */
    public static final TypeAdapter<Double> DOUBLE = new TypeAdapter<Double>() {
        @Override
        public Double read(JsonReader in) throws IOException {
            return in.nextDouble();
        }

        @Override
        public void write(JsonWriter out, Double value) throws IOException {
            out.value(value);
        }
    }.nullSafe();

    public static final TypeAdapter<ArrayList<Integer>> INTEGER_ARRAY_LIST_ADAPTER = new ListTypeAdapter<>(INTEGER, new ArrayListInstantiater<Integer>());
    public static final TypeAdapter<ArrayList<Long>> LONG_ARRAY_LIST_ADAPTER = new ListTypeAdapter<>(LONG, new ArrayListInstantiater<Long>());
    public static final TypeAdapter<ArrayList<Double>> DOUBLE_ARRAY_LIST_ADAPTER = new ListTypeAdapter<>(DOUBLE, new ArrayListInstantiater<Double>());
    public static final TypeAdapter<ArrayList<Short>> SHORT_ARRAY_LIST_ADAPTER = new ListTypeAdapter<>(SHORT, new ArrayListInstantiater<Short>());
    public static final TypeAdapter<ArrayList<Float>> FLOAT_ARRAY_LIST_ADAPTER = new ListTypeAdapter<>(FLOAT, new ArrayListInstantiater<Float>());
    public static final TypeAdapter<ArrayList<Boolean>> BOOLEAN_ARRAY_LIST_ADAPTER = new ListTypeAdapter<>(TypeAdapters.BOOLEAN, new ArrayListInstantiater<Boolean>());
    public static final TypeAdapter<ArrayList<Byte>> BYTE_ARRAY_LIST_ADAPTER = new ListTypeAdapter<>(BYTE, new ArrayListInstantiater<Byte>());

    public interface PrimitiveArrayConstructor<T> {
        T[] construct(int size);
    }

    /**
     * Type Adapter for [] types. eg String[] or Integer[].
     * For primitive array types such as int[], long[] etc, use the next set of adapters
     * given below
     */
    public static final class ArrayTypeAdapter<T> extends TypeAdapter<T[]> {
        TypeAdapter<T> mValueTypeAdapter;
        PrimitiveArrayConstructor<T> mObjectCreator;

        public ArrayTypeAdapter(TypeAdapter<T> valueTypeAdapter, PrimitiveArrayConstructor<T> instanceCreator) {
            this.mValueTypeAdapter = valueTypeAdapter;
            this.mObjectCreator = instanceCreator;
        }

        @Override
        public void write(JsonWriter writer, T[] value) throws IOException {
            writer.beginArray();
            if(null != value) {
                for (T item : value) {
                    mValueTypeAdapter.write(writer, item);
                }
            }
            writer.endArray();
        }

        @Override
        public T[] read(JsonReader reader) throws IOException {
            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
                reader.nextNull();
                return null;
            }
            if (reader.peek() != JsonToken.BEGIN_ARRAY) {
                reader.skipValue();
                return null;
            }
            reader.beginArray();

            ArrayList<T> object = new ArrayList<>();
            while (reader.hasNext()) {
                object.add(mValueTypeAdapter.read(reader));
            }

            reader.endArray();

            T[] result = this.mObjectCreator.construct(object.size());
            return object.toArray(result);
        }
    }

    /**
     * Type Adapter for Integer[] type. This can be directly accessed to read and write
     */
    public static final class PrimitiveIntegerArrayAdapter {
        public static void write(@NotNull JsonWriter writer, @Nullable int[] value) throws IOException {
            if (null != value) {
                writer.beginArray();
                for (int item : value) {
                    writer.value(item);
                }
                writer.endArray();
            }
        }

        @Nullable
        public static int[] read(@NotNull JsonReader reader) throws IOException {
            ArrayList<Integer> arrayList = INTEGER_ARRAY_LIST_ADAPTER.read(reader);
            int[] result = null;
            if (null != arrayList) {
                result = new int[arrayList.size()];
                for (int idx = 0; idx < arrayList.size(); idx++) {
                    result[idx] = arrayList.get(idx);
                }
            }
            return result;
        }
    }

    /**
     * Type Adapter for long[] type. This can be directly accessed to read and write
     */
    public static final class PrimitiveLongArrayAdapter {
        public static void write(@NotNull JsonWriter writer, @Nullable long[] value) throws IOException {
            if (null != value) {
                writer.beginArray();
                for (long item : value) {
                    writer.value(item);
                }
                writer.endArray();
            }
        }

        @Nullable
        public static long[] read(@NotNull JsonReader reader) throws IOException {
            ArrayList<Long> arrayList = LONG_ARRAY_LIST_ADAPTER.read(reader);
            long[] result = null;
            if (null != arrayList) {
                result = new long[arrayList.size()];
                for (int idx = 0; idx < arrayList.size(); idx++) {
                    result[idx] = arrayList.get(idx);
                }
            }
            return result;
        }
    }

    /**
     * Type Adapter for double[] type. This can be directly accessed to read and write
     */
    public static final class PrimitiveDoubleArrayAdapter {
        public static void write(@NotNull JsonWriter writer, @Nullable double[] value) throws IOException {
            if (null != value) {
                writer.beginArray();
                for (double item : value) {
                    writer.value(item);
                }
                writer.endArray();
            }
        }

        @Nullable
        public static double[] read(@NotNull JsonReader reader) throws IOException {
            ArrayList<Double> arrayList = DOUBLE_ARRAY_LIST_ADAPTER.read(reader);
            double[] result = null;
            if (null != arrayList) {
                result = new double[arrayList.size()];
                for (int idx = 0; idx < arrayList.size(); idx++) {
                    result[idx] = arrayList.get(idx);
                }
            }
            return result;
        }
    }

    /**
     * Type Adapter for short[] type. This can be directly accessed to read and write
     */
    public static final class PrimitiveShortArrayAdapter {
        public static void write(@NotNull JsonWriter writer, @Nullable short[] value) throws IOException {
            if (null != value) {
                writer.beginArray();
                for (short item : value) {
                    writer.value(item);
                }
                writer.endArray();
            }
        }

        @Nullable
        public static short[] read(@NotNull JsonReader reader) throws IOException {
            ArrayList<Short> arrayList = SHORT_ARRAY_LIST_ADAPTER.read(reader);
            short[] result = null;
            if (null != arrayList) {
                result = new short[arrayList.size()];
                for (int idx = 0; idx < arrayList.size(); idx++) {
                    result[idx] = arrayList.get(idx);
                }
            }
            return result;
        }
    }
    /**
     * Type Adapter for float[] type. This can be directly accessed to read and write
     */
    public static final class PrimitiveFloatArrayAdapter {
        public static void write(@NotNull JsonWriter writer, @Nullable float[] value) throws IOException {
            if (null != value) {
                writer.beginArray();
                for (float item : value) {
                    writer.value(item);
                }
                writer.endArray();
            }
        }

        @Nullable
        public static float[] read(@NotNull JsonReader reader) throws IOException {
            ArrayList<Float> arrayList = FLOAT_ARRAY_LIST_ADAPTER.read(reader);
            float[] result = null;
            if (null != arrayList) {
                result = new float[arrayList.size()];
                for (int idx = 0; idx < arrayList.size(); idx++) {
                    result[idx] = arrayList.get(idx);
                }
            }
            return result;
        }
    }

    /**
     * Type Adapter for boolean[] type. This can be directly accessed to read and write
     */
    public static final class PrimitiveBooleanArrayAdapter {
        public static void write(@NotNull JsonWriter writer, @Nullable boolean[] value) throws IOException {
            if (null != value) {
                writer.beginArray();
                for (boolean item : value) {
                    writer.value(item);
                }
                writer.endArray();
            }
        }

        @Nullable
        public static boolean[] read(@NotNull JsonReader reader) throws IOException {
            ArrayList<Boolean> arrayList = BOOLEAN_ARRAY_LIST_ADAPTER.read(reader);
            boolean[] result = null;
            if (null != arrayList) {
                result = new boolean[arrayList.size()];
                for (int idx = 0; idx < arrayList.size(); idx++) {
                    result[idx] = arrayList.get(idx);
                }
            }
            return result;
        }
    }

    /**
     * Type Adapter for byte[] type. This can be directly accessed to read and write
     */
    public static final class PrimitiveByteArrayAdapter {
        public static void write(@NotNull JsonWriter writer, @Nullable byte[] value) throws IOException {
            if (null != value) {
                writer.beginArray();
                for (byte item : value) {
                    writer.value(item);
                }
                writer.endArray();
            }
        }

        @Nullable
        public static byte[] read(@NotNull JsonReader reader) throws IOException {
            ArrayList<Byte> byteArrayList = BYTE_ARRAY_LIST_ADAPTER.read(reader);
            byte[] result = null;
            if (null != byteArrayList) {
                result = new byte[byteArrayList.size()];
                for (int idx = 0; idx < byteArrayList.size(); idx++) {
                    result[idx] = byteArrayList.get(idx);
                }
            }
            return result;
        }
    }

    /**
     * Type Adapter for char[] type. This can be directly accessed to read and write
     */
    public static final class PrimitiveCharArrayAdapter {
        public static void write(@NotNull JsonWriter writer, @Nullable char[] value) throws IOException {
            if (null != value) {
                writer.beginArray();
                for (char item : value) {
                    writer.value(item);
                }
                writer.endArray();
            }
        }

        @Nullable
        public static char[] read(@NotNull JsonReader reader) throws IOException {
            String string = com.google.gson.internal.bind.TypeAdapters.STRING.nullSafe().read(reader);
            return null != string ? string.toCharArray() : null;
        }
    }

    /**
     * Default Instantiater for List, by default it will create the Map of {@link ArrayList} type
     */
    public static final class ListInstantiater<V> implements ObjectConstructor<List<V>> {
        @Override
        public List<V> construct() {
            return new ArrayList<>();
        }
    }

    /**
     * Instantiater for {@link Collection}
     */
    public static final class CollectionInstantiater<V> implements ObjectConstructor<Collection<V>> {
        @Override
        public Collection<V> construct() {
            return new ArrayList<>();
        }
    }

    /**
     * Instantiater for {@link ArrayList}
     */
    public static final class ArrayListInstantiater<V> implements ObjectConstructor<ArrayList<V>> {
        @Override
        public ArrayList<V> construct() {
            return new ArrayList<>();
        }
    }

    /**
     * Instantiater for {@link HashMap}
     */
    public static final class HashMapInstantiater<K, V> implements ObjectConstructor<HashMap<K, V>> {
        @Override
        public HashMap<K, V> construct() {
            return new HashMap<>();
        }
    }

    /**
     * Instantiater for {@link ConcurrentHashMap}
     */
    public static final class ConcurrentHashMapInstantiater<K, V> implements ObjectConstructor<ConcurrentHashMap<K, V>> {
        @Override
        public ConcurrentHashMap<K, V> construct() {
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Instantiater for {@link LinkedHashMap}
     */
    public static final class LinkedHashMapInstantiater<K, V> implements ObjectConstructor<LinkedHashMap<K, V>> {
        @Override
        public LinkedHashMap<K, V> construct() {
            return new LinkedHashMap<>();
        }
    }

    /**
     * Default Instantiater for Maps, by default it will create the Map of {@link LinkedHashMap} type
     */
    public static final class MapInstantiater<K, V> implements ObjectConstructor<Map<K, V>> {
        @Override
        public Map<K, V> construct() {
            return new LinkedHashMap<K, V>();
        }
    }

    /**
     * Type Adapter for {@link Collection}
     */
    public static final class ListTypeAdapter<V, T extends Collection<V>> extends TypeAdapter<T> {

        private TypeAdapter<V> valueTypeAdapter;
        private ObjectConstructor<T> objectConstructor;

        public ListTypeAdapter(TypeAdapter<V> valueTypeAdapter, ObjectConstructor<T> objectConstructor) {
            this.valueTypeAdapter = valueTypeAdapter;
            this.objectConstructor = objectConstructor;
        }

        @Override
        public void write(JsonWriter writer, T value) throws IOException {
            writer.beginArray();
            if(null != value) {
                for (V item : value) {
                    valueTypeAdapter.write(writer, item);
                }
            }
            writer.endArray();
        }

        @Override
        public T read(JsonReader reader) throws IOException {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return null;
            }

            if (reader.peek() != JsonToken.BEGIN_ARRAY) {
                reader.skipValue();
                return null;
            }

            T collection = objectConstructor.construct();
            reader.beginArray();
            while (reader.hasNext()) {
                collection.add(valueTypeAdapter.read(reader));
            }
            reader.endArray();
            return collection;
        }
    }

    /**
     * Type Adapter for {@link Map}. The constructor expects {@link ObjectConstructor} which is
     * used to instantiate maps of particular types eg, {@link HashMap} {@link LinkedHashMap} etc
     */
    public static final class MapTypeAdapter<K, V, T extends Map<K, V>> extends TypeAdapter<T> {
        private ObjectConstructor<T> objectConstructor;
        private TypeAdapter<V> valueTypeAdapter;
        private TypeAdapter<K> keyTypeAdapter;

        public MapTypeAdapter(TypeAdapter<K> keyTypeAdapter, TypeAdapter<V> valueTypeAdapter, ObjectConstructor<T> objectConstructor) {
            this.keyTypeAdapter = keyTypeAdapter;
            this.valueTypeAdapter = valueTypeAdapter;
            this.objectConstructor = objectConstructor;
        }

        @Override
        public void write(JsonWriter writer, T value) throws IOException {
            if(null == value) {
                writer.nullValue();
                return;
            }
            boolean hasComplexKeys = false;
            List<JsonElement> keys = new ArrayList<>(value.size());

            List<V> values = new ArrayList<>(value.size());
            for (Map.Entry<K, V> entry : value.entrySet()) {
                JsonElement keyElement = keyTypeAdapter.toJsonTree(entry.getKey());
                keys.add(keyElement);
                values.add(entry.getValue());
                hasComplexKeys |= keyElement.isJsonArray() || keyElement.isJsonObject();
            }

            if (hasComplexKeys) {
                writer.beginArray();
                for (int i = 0; i < keys.size(); i++) {
                    writer.beginArray(); // entry array
                    Streams.write(keys.get(i), writer);
                    valueTypeAdapter.write(writer, values.get(i));
                    writer.endArray();
                }
                writer.endArray();
            } else {
                writer.beginObject();
                for (int i = 0; i < keys.size(); i++) {
                    JsonElement keyElement = keys.get(i);
                    writer.name(keyToString(keyElement));
                    valueTypeAdapter.write(writer, values.get(i));
                }
                writer.endObject();
            }
        }

        @Override
        public T read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            T map = objectConstructor.construct();

            if (peek == JsonToken.BEGIN_ARRAY) {
                in.beginArray();
                while (in.hasNext()) {
                    in.beginArray(); // entry array
                    K key = keyTypeAdapter.read(in);
                    V value = valueTypeAdapter.read(in);
                    V replaced = map.put(key, value);
                    if (replaced != null) {
                        throw new JsonSyntaxException("duplicate key: " + key);
                    }
                    in.endArray();
                }
                in.endArray();
            } else {
                in.beginObject();
                while (in.hasNext()) {
                    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(in);
                    K key = keyTypeAdapter.read(in);
                    V value = valueTypeAdapter.read(in);
                    V replaced = map.put(key, value);
                    if (replaced != null) {
                        throw new JsonSyntaxException("duplicate key: " + key);
                    }
                }
                in.endObject();
            }
            return map;
        }

        private static String keyToString(JsonElement keyElement) {
            if (keyElement.isJsonPrimitive()) {
                JsonPrimitive primitive = keyElement.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    return String.valueOf(primitive.getAsNumber());
                } else if (primitive.isBoolean()) {
                    return Boolean.toString(primitive.getAsBoolean());
                } else if (primitive.isString()) {
                    return primitive.getAsString();
                } else {
                    throw new AssertionError();
                }
            } else if (keyElement.isJsonNull()) {
                return "null";
            } else {
                throw new AssertionError();
            }
        }
    }

    /**
     * Type Adapter for {@link Object}
     */
    public static final class ObjectTypeAdapter extends TypeAdapter<Object> {

        private final Gson gson;

        public ObjectTypeAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public Object read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    List<Object> list = new ArrayList<Object>();
                    in.beginArray();
                    while (in.hasNext()) {
                        list.add(read(in));
                    }
                    in.endArray();
                    return list;

                case BEGIN_OBJECT:
                    Map<String, Object> map = new LinkedTreeMap<String, Object>();
                    in.beginObject();
                    while (in.hasNext()) {
                        map.put(in.nextName(), read(in));
                    }
                    in.endObject();
                    return map;

                case STRING:
                    return in.nextString();

                case NUMBER:
                    return in.nextDouble();

                case BOOLEAN:
                    return in.nextBoolean();

                case NULL:
                    in.nextNull();
                    return null;

                default:
                    throw new IllegalStateException();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void write(JsonWriter out, Object value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            TypeAdapter<Object> typeAdapter = (TypeAdapter<Object>) gson.getAdapter(value.getClass());
            if (typeAdapter instanceof ObjectTypeAdapter) {
                out.beginObject();
                out.endObject();
                return;
            }

            typeAdapter.write(out, value);
        }
    }

    public static final TypeAdapter<JsonElement> JSON_ELEMENT_TYPE_ADAPTER = com.google.gson.internal.bind.TypeAdapters.JSON_ELEMENT.nullSafe();
    public static final TypeAdapter<JsonObject> JSON_OBJECT_TYPE_ADAPTER = new TypeAdapter<JsonObject>() {
        @Override
        public void write(JsonWriter out, JsonObject value) throws IOException {
            JSON_ELEMENT_TYPE_ADAPTER.write(out, value);
        }

        @Override
        public JsonObject read(JsonReader in) throws IOException {
            JsonElement jsonElement = JSON_ELEMENT_TYPE_ADAPTER.read(in);
            return jsonElement != null && jsonElement.isJsonObject() ? jsonElement.getAsJsonObject() : null;
        }
    }.nullSafe();

    public static final TypeAdapter<JsonArray> JSON_ARRAY_TYPE_ADAPTER = new TypeAdapter<JsonArray>() {
        @Override
        public void write(JsonWriter out, JsonArray value) throws IOException {
            JSON_ELEMENT_TYPE_ADAPTER.write(out, value);
        }

        @Override
        public JsonArray read(JsonReader in) throws IOException {
            JsonElement jsonElement = JSON_ELEMENT_TYPE_ADAPTER.read(in);
            return jsonElement != null && jsonElement.isJsonArray() ? jsonElement.getAsJsonArray() : null;
        }
    }.nullSafe();

    public static final TypeAdapter<JsonPrimitive> JSON_PRIMITIVE_TYPE_ADAPTER = new TypeAdapter<JsonPrimitive>() {

        @Override
        public void write(JsonWriter out, JsonPrimitive value) throws IOException {
            JSON_ELEMENT_TYPE_ADAPTER.write(out, value);
        }

        @Override
        public JsonPrimitive read(JsonReader in) throws IOException {
            JsonElement jsonElement = JSON_ELEMENT_TYPE_ADAPTER.read(in);
            return jsonElement != null && jsonElement.isJsonPrimitive() ? jsonElement.getAsJsonPrimitive() : null;
        }
    }.nullSafe();

    public static final TypeAdapter<JsonNull> JSON_NULL_TYPE_ADAPTER = new TypeAdapter<JsonNull>() {

        @Override
        public void write(JsonWriter out, JsonNull value) throws IOException {
            JSON_ELEMENT_TYPE_ADAPTER.write(out, value);
        }

        @Override
        public JsonNull read(JsonReader in) throws IOException {
            JsonElement jsonElement = JSON_ELEMENT_TYPE_ADAPTER.read(in);
            return jsonElement != null && jsonElement.isJsonNull() ? jsonElement.getAsJsonNull() : null;
        }
    }.nullSafe();
}