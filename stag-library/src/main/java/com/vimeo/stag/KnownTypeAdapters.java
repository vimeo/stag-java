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
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KnownTypeAdapters {

    public static final TypeAdapter<BitSet> BIT_SET = new TypeAdapter<BitSet>() {
        @Override
        public BitSet read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            BitSet bitset = new BitSet();
            in.beginArray();
            int i = 0;
            JsonToken tokenType = in.peek();
            while (tokenType != JsonToken.END_ARRAY) {
                boolean set;
                switch (tokenType) {
                    case NUMBER:
                        set = in.nextInt() != 0;
                        break;
                    case BOOLEAN:
                        set = in.nextBoolean();
                        break;
                    case STRING:
                        String stringValue = in.nextString();
                        try {
                            set = Integer.parseInt(stringValue) != 0;
                        } catch (NumberFormatException e) {
                            throw new JsonSyntaxException(
                                    "Error: Expecting: bitset number value (1, 0), Found: " + stringValue);
                        }
                        break;
                    default:
                        throw new JsonSyntaxException("Invalid bitset value type: " + tokenType);
                }
                if (set) {
                    bitset.set(i);
                }
                ++i;
                tokenType = in.peek();
            }
            in.endArray();
            return bitset;
        }

        @Override
        public void write(JsonWriter out, BitSet src) throws IOException {
            if (src == null) {
                out.nullValue();
                return;
            }

            out.beginArray();
            for (int i = 0; i < src.length(); i++) {
                int value = (src.get(i)) ? 1 : 0;
                out.value(value);
            }
            out.endArray();
        }
    };

    public static final TypeAdapter<Boolean> BOOLEAN = new TypeAdapter<Boolean>() {
        @Override
        public Boolean read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            } else if (in.peek() == JsonToken.STRING) {
                // support strings for compatibility with GSON 1.7
                return Boolean.parseBoolean(in.nextString());
            }
            return in.nextBoolean();
        }

        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            out.value(value);
        }
    };

    public static final TypeAdapter<Boolean> BOOLEAN_AS_STRING = new TypeAdapter<Boolean>() {
        @Override
        public Boolean read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return Boolean.valueOf(in.nextString());
        }

        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            out.value(value == null ? "null" : value.toString());
        }
    };

    public static final TypeAdapter<Byte> BYTE = new TypeAdapter<Byte>() {
        @Override
        public Byte read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
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
    };

    public static final TypeAdapter<Short> SHORT = new TypeAdapter<Short>() {
        @Override
        public Short read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
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
    };

    public static final TypeAdapter<Integer> INTEGER = new TypeAdapter<Integer>() {
        @Override
        public Integer read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
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
    };

    public static final TypeAdapter<Long> LONG = new TypeAdapter<Long>() {
        @Override
        public Long read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
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
    };

    public static final TypeAdapter<Float> FLOAT = new TypeAdapter<Float>() {
        @Override
        public Float read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return (float) in.nextDouble();
        }

        @Override
        public void write(JsonWriter out, Float value) throws IOException {
            out.value(value);
        }
    };

    public static final TypeAdapter<Double> DOUBLE = new TypeAdapter<Double>() {
        @Override
        public Double read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return in.nextDouble();
        }

        @Override
        public void write(JsonWriter out, Double value) throws IOException {
            out.value(value);
        }
    };

    public static final TypeAdapter<Number> NUMBER = new TypeAdapter<Number>() {
        @Override
        public Number read(JsonReader in) throws IOException {
            JsonToken jsonToken = in.peek();
            switch (jsonToken) {
                case NULL:
                    in.nextNull();
                    return null;
                case NUMBER:
                case STRING:
                    return new LazilyParsedNumber(in.nextString());
                default:
                    throw new JsonSyntaxException("Expecting number, got: " + jsonToken);
            }
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }
    };

    public static final TypeAdapter<Character> CHARACTER = new TypeAdapter<Character>() {
        @Override
        public Character read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String str = in.nextString();
            if (str.length() != 1) {
                throw new JsonSyntaxException("Expecting character, got: " + str);
            }
            return str.charAt(0);
        }

        @Override
        public void write(JsonWriter out, Character value) throws IOException {
            out.value(value == null ? null : String.valueOf(value));
        }
    };

    public static final TypeAdapter<String> STRING = new TypeAdapter<String>() {
        @Override
        public String read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            /* coerce booleans to strings for backwards compatibility */
            if (peek == JsonToken.BOOLEAN) {
                return Boolean.toString(in.nextBoolean());
            }
            return in.nextString();
        }

        @Override
        public void write(JsonWriter out, String value) throws IOException {
            out.value(value);
        }
    };

    public static final TypeAdapter<BigDecimal> BIG_DECIMAL = new TypeAdapter<BigDecimal>() {
        @Override
        public BigDecimal read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                return new BigDecimal(in.nextString());
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public void write(JsonWriter out, BigDecimal value) throws IOException {
            out.value(value);
        }
    };

    public static final TypeAdapter<BigInteger> BIG_INTEGER = new TypeAdapter<BigInteger>() {
        @Override
        public BigInteger read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                return new BigInteger(in.nextString());
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public void write(JsonWriter out, BigInteger value) throws IOException {
            out.value(value);
        }
    };

    public static final TypeAdapter<Calendar> CALENDAR = new TypeAdapter<Calendar>() {
        private static final String YEAR = "year";
        private static final String MONTH = "month";
        private static final String DAY_OF_MONTH = "dayOfMonth";
        private static final String HOUR_OF_DAY = "hourOfDay";
        private static final String MINUTE = "minute";
        private static final String SECOND = "second";

        @Override
        public Calendar read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            in.beginObject();
            int year = 0;
            int month = 0;
            int dayOfMonth = 0;
            int hourOfDay = 0;
            int minute = 0;
            int second = 0;
            while (in.peek() != JsonToken.END_OBJECT) {
                String name = in.nextName();
                int value = in.nextInt();
                if (YEAR.equals(name)) {
                    year = value;
                } else if (MONTH.equals(name)) {
                    month = value;
                } else if (DAY_OF_MONTH.equals(name)) {
                    dayOfMonth = value;
                } else if (HOUR_OF_DAY.equals(name)) {
                    hourOfDay = value;
                } else if (MINUTE.equals(name)) {
                    minute = value;
                } else if (SECOND.equals(name)) {
                    second = value;
                }
            }
            in.endObject();
            return new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
        }

        @Override
        public void write(JsonWriter out, Calendar value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.beginObject();
            out.name(YEAR);
            out.value(value.get(Calendar.YEAR));
            out.name(MONTH);
            out.value(value.get(Calendar.MONTH));
            out.name(DAY_OF_MONTH);
            out.value(value.get(Calendar.DAY_OF_MONTH));
            out.name(HOUR_OF_DAY);
            out.value(value.get(Calendar.HOUR_OF_DAY));
            out.name(MINUTE);
            out.value(value.get(Calendar.MINUTE));
            out.name(SECOND);
            out.value(value.get(Calendar.SECOND));
            out.endObject();
        }
    };

    public static final TypeAdapter<JsonElement> JSON_ELEMENT = new TypeAdapter<JsonElement>() {
        @Override
        public JsonElement read(JsonReader in) throws IOException {
            switch (in.peek()) {
                case STRING:
                    return new JsonPrimitive(in.nextString());
                case NUMBER:
                    String number = in.nextString();
                    return new JsonPrimitive(new LazilyParsedNumber(number));
                case BOOLEAN:
                    return new JsonPrimitive(in.nextBoolean());
                case NULL:
                    in.nextNull();
                    return JsonNull.INSTANCE;
                case BEGIN_ARRAY:
                    JsonArray array = new JsonArray();
                    in.beginArray();
                    while (in.hasNext()) {
                        array.add(read(in));
                    }
                    in.endArray();
                    return array;
                case BEGIN_OBJECT:
                    JsonObject object = new JsonObject();
                    in.beginObject();
                    while (in.hasNext()) {
                        object.add(in.nextName(), read(in));
                    }
                    in.endObject();
                    return object;
                case END_DOCUMENT:
                case NAME:
                case END_OBJECT:
                case END_ARRAY:
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public void write(JsonWriter out, JsonElement value) throws IOException {
            if (value == null || value.isJsonNull()) {
                out.nullValue();
            } else if (value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    out.value(primitive.getAsNumber());
                } else if (primitive.isBoolean()) {
                    out.value(primitive.getAsBoolean());
                } else {
                    out.value(primitive.getAsString());
                }

            } else if (value.isJsonArray()) {
                out.beginArray();
                for (JsonElement e : value.getAsJsonArray()) {
                    write(out, e);
                }
                out.endArray();

            } else if (value.isJsonObject()) {
                out.beginObject();
                for (Map.Entry<String, JsonElement> e : value.getAsJsonObject().entrySet()) {
                    out.name(e.getKey());
                    write(out, e.getValue());
                }
                out.endObject();

            } else {
                throw new IllegalArgumentException("Couldn't write " + value.getClass());
            }
        }
    };

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

    public final class ObjectTypeAdapter extends TypeAdapter<Object> {

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
}