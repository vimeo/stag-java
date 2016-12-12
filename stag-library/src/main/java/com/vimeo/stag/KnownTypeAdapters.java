package com.vimeo.stag;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KnownTypeAdapters {

    private static TypeAdapter stringMapTypeAdapter;
    private static TypeAdapter mapTypeAdapter;
    private static TypeAdapter stringToIntegerMapTypeAdapter;

    public static <V> TypeAdapter getStringMapTypeAdapter(TypeAdapter<V> valueTypeAdapter, Instantiater<Map> instantiater) {
        if (null == stringMapTypeAdapter) {
            stringMapTypeAdapter = new StringMapTypeAdapter<>(valueTypeAdapter, instantiater);
        }
        return stringMapTypeAdapter;
    }

    public static <K, V> TypeAdapter getMapTypeAdapter(TypeAdapter<K> keyTypeAdapter, TypeAdapter<V> valueTypeAdapter, Instantiater<Map> instantiater) {
        if (null == mapTypeAdapter) {
            mapTypeAdapter = new MapTypeAdapter<>(keyTypeAdapter, valueTypeAdapter, instantiater);
        }
        return mapTypeAdapter;
    }

    public static TypeAdapter getStringToIntegerMapTypeAdapter(Instantiater<Map> instantiater) {
        if (null == stringToIntegerMapTypeAdapter) {
            stringToIntegerMapTypeAdapter = new StringToIntegerMapTypeAdapter(instantiater);
        }
        return stringToIntegerMapTypeAdapter;
    }

    private static class StringToIntegerMapTypeAdapter extends MapTypeAdapter<String, Integer> {

        StringToIntegerMapTypeAdapter(Instantiater<Map> instantiater) {
            super(null, null, instantiater);
        }

        @Override
        public void write(JsonWriter writer, Map<String, Integer> value) throws IOException {
            writer.beginObject();
            for (java.util.HashMap.Entry<String, Integer> entry : value.entrySet()) {
                writer.name(entry.getKey());
                writer.value(entry.getValue());
            }
            writer.endObject();
        }

        @Override
        public Map<String, Integer> read(JsonReader reader) throws IOException {
            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
                reader.nextNull();
                return null;
            }
            if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                reader.skipValue();
                return null;
            }
            reader.beginObject();

            Map<String, Integer> object = new LinkedHashMap<>();
            if (instantiater != null) {
                object = instantiater.instantiate();
            }

            while (reader.hasNext()) {
                com.google.gson.stream.JsonToken jsonToken = reader.peek();
                if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                    reader.skipValue();
                    continue;
                }

                if (reader.peek() == com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        com.google.gson.internal.JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
                        String key = reader.nextString();
                        Integer value = reader.nextInt();
                        Integer replaced = object.put(key, value);
                        if (replaced != null) {
                            throw new com.google.gson.JsonSyntaxException("duplicate key: " + key);
                        }
                    }
                    reader.endObject();
                } else if (reader.peek() == com.google.gson.stream.JsonToken.BEGIN_ARRAY) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginArray();
                        String key = reader.nextString();
                        Integer value = reader.nextInt();
                        Integer replaced = object.put(key, value);
                        if (replaced != null) {
                            throw new com.google.gson.JsonSyntaxException("duplicate key: " + key);
                        }
                        reader.endArray();
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

    private static class StringMapTypeAdapter<V> extends MapTypeAdapter<String, V> {

        StringMapTypeAdapter(TypeAdapter<V> valueTypeAdapter, Instantiater<Map> instantiater) {
            super(null, valueTypeAdapter, instantiater);
        }

        @Override
        public void write(JsonWriter writer, Map<String, V> value) throws IOException {
            writer.beginObject();
            for (java.util.HashMap.Entry<String, V> entry : value.entrySet()) {
                writer.name(entry.getKey());
                valueTypeAdapter.write(writer, entry.getValue());
            }
            writer.endObject();
        }

        @Override
        public Map<String, V> read(JsonReader reader) throws IOException {
            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
                reader.nextNull();
                return null;
            }
            if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                reader.skipValue();
                return null;
            }
            reader.beginObject();

            Map<String, V> object = new LinkedHashMap<>();
            if (instantiater != null) {
                object = instantiater.instantiate();
            }

            while (reader.hasNext()) {
                com.google.gson.stream.JsonToken jsonToken = reader.peek();
                if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                    reader.skipValue();
                    continue;
                }

                if (reader.peek() == com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        com.google.gson.internal.JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
                        String key = reader.nextString();
                        V value = valueTypeAdapter.read(reader);
                        V replaced = object.put(key, value);
                        if (replaced != null) {
                            throw new com.google.gson.JsonSyntaxException("duplicate key: " + key);
                        }
                    }
                    reader.endObject();
                } else if (reader.peek() == com.google.gson.stream.JsonToken.BEGIN_ARRAY) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginArray();
                        String key = reader.nextString();
                        V value = valueTypeAdapter.read(reader);
                        V replaced = object.put(key, value);
                        if (replaced != null) {
                            throw new com.google.gson.JsonSyntaxException("duplicate key: " + key);
                        }
                        reader.endArray();
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

    public static class MapTypeAdapter<K, V> extends TypeAdapter<Map<K, V>> {

        protected Instantiater<Map> instantiater;
        protected TypeAdapter<V> valueTypeAdapter;
        private TypeAdapter<K> keyTypeAdapter;

        MapTypeAdapter(TypeAdapter<K> keyTypeAdapter, TypeAdapter<V> valueTypeAdapter, Instantiater<Map> instantiater) {
            this.keyTypeAdapter = keyTypeAdapter;
            this.valueTypeAdapter = valueTypeAdapter;
            this.instantiater = instantiater;
        }

        @Override
        public void write(JsonWriter writer, Map<K, V> value) throws IOException {
            boolean hasComplexKeys = false;
            List<JsonElement> keys = new ArrayList<JsonElement>(value.size());

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
        public Map<K, V> read(JsonReader reader) throws IOException {
            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
                reader.nextNull();
                return null;
            }
            if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                reader.skipValue();
                return null;
            }
            reader.beginObject();

            Map<K, V> object = new LinkedHashMap<>();
            if (instantiater != null) {
                object = instantiater.instantiate();
            }

            while (reader.hasNext()) {
                com.google.gson.stream.JsonToken jsonToken = reader.peek();
                if (jsonToken == com.google.gson.stream.JsonToken.NULL) {
                    reader.skipValue();
                    continue;
                }

                if (reader.peek() == com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        com.google.gson.internal.JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
                        K key = keyTypeAdapter.read(reader);
                        V value = valueTypeAdapter.read(reader);
                        V replaced = object.put(key, value);
                        if (replaced != null) {
                            throw new com.google.gson.JsonSyntaxException("duplicate key: " + key);
                        }
                    }
                    reader.endObject();
                } else if (reader.peek() == com.google.gson.stream.JsonToken.BEGIN_ARRAY) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginArray();
                        K key = keyTypeAdapter.read(reader);
                        V value = valueTypeAdapter.read(reader);
                        V replaced = object.put(key, value);
                        if (replaced != null) {
                            throw new com.google.gson.JsonSyntaxException("duplicate key: " + key);
                        }
                        reader.endArray();
                    }
                    reader.endArray();
                } else {
                    reader.skipValue();
                }
            }

            reader.endObject();
            return object;
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

    public static class ListTypeAdapter<T> extends TypeAdapter<List<T>> {

        private TypeAdapter<T> valueTypeAdapter;
        private Instantiater<List> instantiater;

        public ListTypeAdapter(TypeAdapter<T> valueTypeAdapter, Instantiater<List> instantiater) {
            this.valueTypeAdapter = valueTypeAdapter;
            this.instantiater = instantiater;
        }

        @Override
        public void write(JsonWriter writer, List<T> value) throws IOException {
            writer.beginArray();
            for (T item : value) {
                valueTypeAdapter.write(writer, item);
            }
            writer.endArray();
        }

        @Override
        public List<T> read(JsonReader reader) throws IOException {
            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
                reader.nextNull();
                return null;
            }
            if (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                reader.skipValue();
                return null;
            }
            reader.beginObject();

            List<T> object = new ArrayList<>();
            if (instantiater != null) {
                object = instantiater.instantiate();
            }

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
}