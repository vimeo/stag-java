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
import com.vimeo.stag.processor.dummy.DummyConcreteClass;
import com.vimeo.stag.processor.dummy.DummyMapClass;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import javax.lang.model.type.TypeKind;

/**
 * Unit test for {@link TypeAdapterGenerator}
 */
public class TypeAdapterGeneratorUnitTest extends BaseUnitTest {

    @Test
    public void testIsSupportedPrimitive_supportsTypes() throws Exception {

        // Check supported primitives
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(long.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(int.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(boolean.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(float.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(double.class.getName()));

        // Check unsupported primitives
        Assert.assertFalse(TypeUtils.isSupportedPrimitive(byte.class.getName()));
        Assert.assertFalse(TypeUtils.isSupportedPrimitive(char.class.getName()));
        Assert.assertFalse(TypeUtils.isSupportedPrimitive(short.class.getName()));
        Assert.assertFalse(TypeUtils.isSupportedPrimitive(void.class.getName()));

        // Check non-primitives
        Assert.assertFalse(TypeUtils.isSupportedPrimitive(String.class.getName()));
        Assert.assertFalse(TypeUtils.isSupportedPrimitive(Object.class.getName()));
    }

    @Test
    public void testIsSupportedCollection_supportsTypes() throws Exception {
        // Check supported list types
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(List.class)));
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(ArrayList.class)));
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Collection.class)));

        // Check unsupported list types
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(LinkedList.class)));
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Vector.class)));
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Stack.class)));

        Assert.assertFalse(TypeUtils.isSupportedCollection(null));

        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Object.class)));

        // Check array types
        Assert.assertTrue(
                TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.INT))));
        Assert.assertTrue(
                TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.BOOLEAN))));
        Assert.assertTrue(
                TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.CHAR))));
        Assert.assertTrue(
                TypeUtils.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(String.class))));
        Assert.assertTrue(
                TypeUtils.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(Object.class))));
    }

    @Test
    public void testIsSupportedList_supportsTypes() throws Exception {
        // Check supported types
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(List.class)));
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(ArrayList.class)));

        // Check unsupported list types
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(LinkedList.class)));
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Vector.class)));
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Stack.class)));
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Collection.class)));

        Assert.assertFalse(TypeUtils.isSupportedCollection(null));

        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Object.class)));

        // Check unsupported array types
        Assert.assertFalse(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.INT))));
        Assert.assertFalse(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.BOOLEAN))));
        Assert.assertFalse(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.CHAR))));
        Assert.assertFalse(TypeUtils.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(String.class))));
        Assert.assertFalse(TypeUtils.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(Object.class))));
    }

    @Test
    public void testIsSupportedNative_supportsCorrectTypes() throws Exception {
        // Check supported primitives
        Assert.assertTrue(TypeUtils.isSupportedNative(long.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedNative(int.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedNative(boolean.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedNative(float.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedNative(double.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedNative(String.class.getName()));

        // Check unsupported primitives
        Assert.assertFalse(TypeUtils.isSupportedNative(byte.class.getName()));
        Assert.assertFalse(TypeUtils.isSupportedNative(char.class.getName()));
        Assert.assertFalse(TypeUtils.isSupportedNative(short.class.getName()));
        Assert.assertFalse(TypeUtils.isSupportedNative(void.class.getName()));

        // Check non-primitives
        Assert.assertFalse(TypeUtils.isSupportedNative(Object.class.getName()));
    }

    @Test
    public void testIsMap_supportsCorrectTypes() throws Exception {
        // Check null
        Assert.assertFalse(TypeUtils.isSupportedMap(null));

        // Check supported types
        Assert.assertTrue(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(Map.class)));
        Assert.assertTrue(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(HashMap.class)));
        Assert.assertTrue(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(LinkedHashMap.class)));

        // Check type that implements map
        Assert.assertFalse(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(DummyMapClass.class)));

        // Check other types
        Assert.assertFalse(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(Object.class)));
        Assert.assertFalse(
                TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(DummyConcreteClass.class)));
        Assert.assertFalse(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(String.class)));
    }
}