package com.vimeo.sample_java_model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link TypeAdapter} implementation which unmarshals into a {@link JsonObject}, consults the {@code type}
 * name/value pair to determine the expected value type, then unmarshals the {@link JsonObject} into the
 * parameterized type specified.  This is is effectively a customized form of:
 *
 * https://github.com/google/gson/blob/master/extras/src/main/java/com/google/gson/typeadapters/RuntimeTypeAdapterFactory.java
 *
 * @param <T> value type
 */
class DynamicallyTypedModelTypeAdapter<T> extends TypeAdapter<T>
{

	private static final String TYPE_PROPERTY = "type";

	private final AtomicReference<TypeAdapter<JsonObject>> objectDelegateRef = new AtomicReference<>();

	private final Gson gson;
	private final TypeAdapterFactory skipPast;
	private final TypeAdapter<T> delegate;

	DynamicallyTypedModelTypeAdapter(
		Gson gson,
		TypeAdapterFactory skipPast,
		TypeAdapter<T> delegate)
	{
		this.gson = gson;
		this.skipPast = skipPast;
		this.delegate = delegate;
	}

	@Override
	public void write(JsonWriter out, T value) throws IOException
	{
		delegate.write(out, value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T read(JsonReader in) throws IOException
	{
		TypeAdapter<JsonObject> objectDelegate = objectDelegateRef.get();
		if (objectDelegate == null)
		{
			objectDelegate = gson.getAdapter(TypeToken.get(JsonObject.class));
			objectDelegateRef.compareAndSet(null, objectDelegate);
		}

		JsonObject jsonObject = objectDelegate.read(in);
		JsonElement typeElement = jsonObject == null ? null : jsonObject.get(TYPE_PROPERTY);
		String typeString = typeElement == null ? null : typeElement.getAsString();

		TypeToken<?> parameterizedType;
		try {
			DynamicallyTypedModel.Types propertyType = DynamicallyTypedModel.Types.valueOf(typeString);
			parameterizedType = propertyType.getTypeToken();
		} catch (IllegalArgumentException e) {
			throw new IOException("Type not registered: " + typeString, e);
		}

		TypeAdapter<?> adapter = gson.getDelegateAdapter(skipPast, parameterizedType);
		return (T) adapter.fromJsonTree(jsonObject);
	}

}
