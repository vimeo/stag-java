package com.vimeo.sample_java_model;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * {@link TypeAdapterFactory} implementation which looks for non-parameterized references to {@link DynamicallyTypedModel}
 * and configures the {@link DynamicallyTypedModelTypeAdapter} for the point of use.  Parameterized references are handled
 * automagically by Gson.
 */
class DynamicallyTypedModelTypeAdapterFactory implements TypeAdapterFactory
{

	@Override
	@SuppressWarnings("unchecked")
	public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken)
	{
		Class<? super T> rawType = typeToken.getRawType();
		if (!DynamicallyTypedModel.class.isAssignableFrom(rawType))
			return null;

		Type type = typeToken.getType();
		if (isTypeDataPresent(type))
			return null;

		final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, typeToken);
		return new DynamicallyTypedModelTypeAdapter<>(gson, this, delegate);
	}

	private boolean isTypeDataPresent(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            for (Type typeArgument : typeArguments) {
                if (typeArgument instanceof WildcardType) {
                    return false;
                }
            }
            return true;
        } else {
			return false;
		}
	}
}
