package com.vimeo.sample_java_model;

import com.google.gson.reflect.TypeToken;
import com.vimeo.stag.UseStag;

@UseStag
public class DynamicallyTypedModel<T> {

    public Types type;
    public T value;

    public enum Types {
        string(new TypeToken<DynamicallyTypedModel<String>>() {}),
        integer(new TypeToken<DynamicallyTypedModel<Integer>>() {});

        private final TypeToken<?> typeToken;

        Types(TypeToken<?> propertyTypeToken)
        {
            this.typeToken = propertyTypeToken;
        }

        public TypeToken<?> getTypeToken() {
            return typeToken;
        }
    }
}