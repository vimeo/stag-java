package com.vimeo.stag;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

@SuppressWarnings("unused")
public final class Types {

    private Types() {}

    @NotNull
    public static WildcardType getWildcardType(@NotNull Type[] upperBounds, @NotNull Type[] lowerBounds) {
        return new WildcardTypeImpl(upperBounds, lowerBounds);
    }

    private static final class WildcardTypeImpl implements WildcardType {

        @NotNull
        private final Type[] upperBounds;
        @NotNull
        private final Type[] lowerBounds;

        WildcardTypeImpl(@NotNull Type[] upperBounds, @NotNull Type[] lowerBounds) {
            this.upperBounds = upperBounds;
            this.lowerBounds = lowerBounds;
        }

        @Override
        public Type[] getUpperBounds() {
            return upperBounds.clone();
        }

        @Override
        public Type[] getLowerBounds() {
            return lowerBounds.clone();
        }
    }
}
