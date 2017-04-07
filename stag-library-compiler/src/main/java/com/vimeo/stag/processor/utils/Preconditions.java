/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Vimeo
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.vimeo.stag.processor.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class Preconditions {

    private Preconditions() {
        throw new UnsupportedOperationException("This class is not instantiable");
    }

    /**
     * Asserts that the object is not null.
     * Throws a null pointer exception if
     * it is null.
     *
     * @param o the object to check.
     */
    public static void checkNotNull(@Nullable Object o) {
        if (o == null) {
            throw new NullPointerException("Object must not be null");
        }
    }

    /**
     * Asserts that a collection is not empty.
     * Throws an illegal state exception if it
     * is empty.
     *
     * @param collection the collection to check.
     */
    public static void checkNotEmpty(@NotNull Collection collection) {
        if (collection.isEmpty()) {
            throw new IllegalStateException("Collection must not be empty");
        }
    }

    /**
     * Asserts that a boolean is true.
     * Throws an illegal state exception
     * if the boolean is false.
     *
     * @param bool the boolean to check.
     */
    public static void checkTrue(boolean bool) {
        if (!bool) {
            throw new IllegalStateException("Boolean must be true");
        }
    }

}
