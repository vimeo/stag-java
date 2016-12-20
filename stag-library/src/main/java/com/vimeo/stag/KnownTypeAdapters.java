package com.vimeo.stag;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KnownTypeAdapters {

    interface KnownTypeInstantiater<T> {
        T instantiate();
    }

    public static class ListInstantiater<V> implements KnownTypeInstantiater<List<V>> {

        @Override
        public List<V> instantiate() {
            return new ArrayList<V>();
        }
    }

    public static class CollectionInstantiater<V> implements KnownTypeInstantiater<Collection<V>> {

        @Override
        public Collection<V> instantiate() {
            return new ArrayList<V>();
        }
    }

    public static class ArrayListInstantiater<V> implements KnownTypeInstantiater<ArrayList<V>> {

        @Override
        public ArrayList<V> instantiate() {
            return new ArrayList<V>();
        }
    }

    public static class HashMapInstantiater<K, V> implements KnownTypeInstantiater<HashMap<K, V>> {

        @Override
        public HashMap<K, V> instantiate() {
            return new HashMap<K, V>();
        }
    }

    public static class ConcurrentHashMapInstantiater<K, V> implements KnownTypeInstantiater<ConcurrentHashMap<K, V>> {

        @Override
        public ConcurrentHashMap<K, V> instantiate() {
            return new ConcurrentHashMap<K, V>();
        }
    }

    public static class LinkedHashMapInstantiater<K, V> implements KnownTypeInstantiater<LinkedHashMap<K, V>> {

        @Override
        public LinkedHashMap<K, V> instantiate() {
            return new LinkedHashMap<K, V>();
        }
    }

    public static class MapInstantiater<K, V> implements KnownTypeInstantiater<Map<K, V>> {

        @Override
        public Map<K, V> instantiate() {
            return new LinkedHashMap<K, V>();
        }
    }

    public static class StringTypeAdapter extends TypeAdapter<String> {

        @Override
        public void write(JsonWriter writer, String value) throws IOException {
            writer.value(value);
        }

        @Override
        public String read(JsonReader in) throws IOException {
            return in.nextString();
        }
    }

    public static class FloatTypeAdapter extends TypeAdapter<Float> {

        @Override
        public void write(JsonWriter writer, Float value) throws IOException {
            writer.value(value);
        }

        @Override
        public Float read(JsonReader in) throws IOException {
            return (float) in.nextDouble();
        }
    }

    public static class BooleanTypeAdapter extends TypeAdapter<Boolean> {

        @Override
        public void write(JsonWriter writer, Boolean value) throws IOException {
            writer.value(value);
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            return in.nextBoolean();
        }
    }

    public static class DoubleTypeAdapter extends TypeAdapter<Double> {

        @Override
        public void write(JsonWriter writer, Double value) throws IOException {
            writer.value(value);
        }

        @Override
        public Double read(JsonReader in) throws IOException {
            return in.nextDouble();
        }
    }

    public static class IntegerTypeAdapter extends TypeAdapter<Integer> {

        @Override
        public void write(JsonWriter writer, Integer value) throws IOException {
            writer.value(value);
        }

        @Override
        public Integer read(JsonReader in) throws IOException {
            return in.nextInt();
        }
    }

    public static class ListTypeAdapter<V, T extends Collection<V>> extends TypeAdapter<T> {

        private TypeAdapter<V> valueTypeAdapter;
        private KnownTypeInstantiater<T> instantiator;

        public ListTypeAdapter(TypeAdapter<V> valueTypeAdapter, KnownTypeInstantiater<T> instantiator) {
            this.valueTypeAdapter = valueTypeAdapter;
            this.instantiator = instantiator;
        }

        @Override
        public void write(JsonWriter writer, T value) throws IOException {
            writer.beginArray();
            for (V item : value) {
                valueTypeAdapter.write(writer, item);
            }
            writer.endArray();
        }

        @Override
        public T read(JsonReader reader) throws IOException {
            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
                reader.nextNull();
                return null;
            }
            if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                reader.skipValue();
                return null;
            }
            reader.beginObject();

            T object = instantiator.instantiate();

            while (reader.hasNext()) {
                com.google.gson.stream.JsonToken jsonToken = reader.peek();
                if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                    reader.skipValue();
                    continue;
                }

                if (jsonToken == com.google.gson.stream.JsonToken.BEGIN_ARRAY) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        object.add(valueTypeAdapter.read(reader));
                    }
                    reader.endArray();
                } else {
                    reader.skipValue();
                }
            }

            reader.endObject();
            return object;
        }
    }

    public static class MapTypeAdapter<K, V, T extends Map<K, V>> extends TypeAdapter<T> {

        private KnownTypeInstantiater<T> instantiater;
        private TypeAdapter<V> valueTypeAdapter;
        private TypeAdapter<K> keyTypeAdapter;

        public MapTypeAdapter(TypeAdapter<K> keyTypeAdapter, TypeAdapter<V> valueTypeAdapter, KnownTypeInstantiater<T> instantiater) {
            this.keyTypeAdapter = keyTypeAdapter;
            this.valueTypeAdapter = valueTypeAdapter;
            this.instantiater = instantiater;
        }

        @Override
        public void write(JsonWriter writer, T value) throws IOException {
            boolean hasComplexKeys = false;
            List<JsonElement> keys = new ArrayList<>(value.size());

            List<V> values = new ArrayList<>(value.size());
            for (T.Entry<K, V> entry : value.entrySet()) {
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

            T map = instantiater.instantiate();

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

        private String keyToString(JsonElement keyElement) {
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
}