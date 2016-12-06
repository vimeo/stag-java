package com.vimeo.stag.utils;

import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class ParameterizedTypeUtil {
    static void checkArgument(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

    public static ParameterizedType newParameterizedTypeWithOwner(Type ownerType, Type rawType, Type... typeArguments) {
        return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
    }

    static Type canonicalize(Type type) {
        if (type instanceof Class) {
            Class<?> c = (Class<?>) type;
            return c;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return new ParameterizedTypeImpl(p.getOwnerType(), p.getRawType(), p.getActualTypeArguments());
        }
        return null;
    }

    static void checkNotPrimitive(Type type) {
        checkArgument(!(type instanceof Class<?>) || !((Class<?>) type).isPrimitive());
    }

    public static TypeToken getTypeToken(Type rawType, Type... typeArguments) {
        return TypeToken.get(new ParameterizedTypeImpl(null, rawType, typeArguments));
    }

    static <T> T checkNotNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    private static final class ParameterizedTypeImpl implements ParameterizedType, Serializable {
        private static final long serialVersionUID = 0;

        private final Type ownerType;

        private final Type rawType;

        private final Type[] typeArguments;

        public ParameterizedTypeImpl(Type ownerType, Type rawType, Type[] typeArguments) {
            if (rawType instanceof Class<?>) {
                Class<?> rawTypeAsClass = (Class<?>) rawType;
                boolean isStaticOrTopLevelClass = java.lang.reflect.Modifier.isStatic(rawTypeAsClass.getModifiers()) || rawTypeAsClass.getEnclosingClass() == null;
                checkArgument(ownerType != null || isStaticOrTopLevelClass);
            }
            this.ownerType = ownerType == null ? null : canonicalize(ownerType);
            this.rawType = canonicalize(rawType);
            this.typeArguments = typeArguments.clone();
            for (int t = 0; t < this.typeArguments.length; t++) {
                checkNotNull(this.typeArguments[t]);
                checkNotPrimitive(this.typeArguments[t]);
                this.typeArguments[t] = canonicalize(this.typeArguments[t]);
            }
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments.clone();
        }
    }
}