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
package com.vimeo.stag.processor.generators;

import com.vimeo.stag.processor.BaseUnitTest;
import com.vimeo.stag.processor.Utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * Unit test for {@link TypeAdapterGenerator}
 */
public class TypeAdapterGeneratorUnitTest extends BaseUnitTest {

    @Test
    public void testIsSupportedPrimitive_supportsTypes() throws Exception {

        // Check supported primitives
        Assert.assertTrue(TypeAdapterGenerator.isSupportedPrimitive(long.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isSupportedPrimitive(int.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isSupportedPrimitive(boolean.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isSupportedPrimitive(float.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isSupportedPrimitive(double.class.getName()));

        // Check unsupported primitives
        Assert.assertFalse(TypeAdapterGenerator.isSupportedPrimitive(byte.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedPrimitive(char.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedPrimitive(short.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedPrimitive(void.class.getName()));

        // Check non-primitives
        Assert.assertFalse(TypeAdapterGenerator.isSupportedPrimitive(String.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedPrimitive(Object.class.getName()));
    }

    @Test
    public void testIsArray_supportsTypes() throws Exception {
        // Check supported list types
        Assert.assertTrue(TypeAdapterGenerator.isArray(Utils.getTypeMirrorFromClass(List.class)));
        Assert.assertTrue(TypeAdapterGenerator.isArray(Utils.getTypeMirrorFromClass(ArrayList.class)));
        Assert.assertTrue(TypeAdapterGenerator.isArray(Utils.getTypeMirrorFromClass(Collection.class)));

        // Check unsupported list types
        Assert.assertFalse(TypeAdapterGenerator.isArray(Utils.getTypeMirrorFromClass(LinkedList.class)));
        Assert.assertFalse(TypeAdapterGenerator.isArray(Utils.getTypeMirrorFromClass(Vector.class)));
        Assert.assertFalse(TypeAdapterGenerator.isArray(Utils.getTypeMirrorFromClass(Stack.class)));

        Assert.assertFalse(TypeAdapterGenerator.isArray(Utils.getTypeMirrorFromClass(Object.class)));
    }

    @Test
    public void testIsSupportedNative_supportsCorrectTypes() throws Exception {
        // Check supported primitives
        Assert.assertTrue(TypeAdapterGenerator.isSupportedNative(long.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isSupportedNative(int.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isSupportedNative(boolean.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isSupportedNative(float.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isSupportedNative(double.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isSupportedNative(String.class.getName()));

        // Check unsupported primitives
        Assert.assertFalse(TypeAdapterGenerator.isSupportedNative(byte.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedNative(char.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedNative(short.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedNative(void.class.getName()));

        // Check non-primitives
        Assert.assertFalse(TypeAdapterGenerator.isSupportedNative(Object.class.getName()));
    }

}
