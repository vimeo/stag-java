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

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
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
    public void testIsSupportedCollection_supportsTypes() throws Exception {
        // Check supported list types
        Assert.assertTrue(TypeAdapterGenerator.isSupportedCollection(Utils.getTypeMirrorFromClass(List.class)));
        Assert.assertTrue(TypeAdapterGenerator.isSupportedCollection(Utils.getTypeMirrorFromClass(ArrayList.class)));
        Assert.assertTrue(TypeAdapterGenerator.isSupportedCollection(Utils.getTypeMirrorFromClass(Collection.class)));

        // Check unsupported list types
        Assert.assertFalse(TypeAdapterGenerator.isSupportedCollection(Utils.getTypeMirrorFromClass(LinkedList.class)));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedCollection(Utils.getTypeMirrorFromClass(Vector.class)));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedCollection(Utils.getTypeMirrorFromClass(Stack.class)));

        Assert.assertFalse(TypeAdapterGenerator.isSupportedCollection(null));

        Assert.assertFalse(TypeAdapterGenerator.isSupportedCollection(Utils.getTypeMirrorFromClass(Object.class)));

        // Check array types
        Assert.assertTrue(
                TypeAdapterGenerator.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.INT))));
        Assert.assertTrue(
                TypeAdapterGenerator.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.BOOLEAN))));
        Assert.assertTrue(
                TypeAdapterGenerator.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.CHAR))));
        Assert.assertTrue(
                TypeAdapterGenerator.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(String.class))));
        Assert.assertTrue(
                TypeAdapterGenerator.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(Object.class))));
    }

    @Test
    public void testIsSupportedList_supportsTypes() throws Exception {
        // Check supported types
        Assert.assertTrue(TypeAdapterGenerator.isSupportedList(Utils.getTypeMirrorFromClass(List.class)));
        Assert.assertTrue(
                TypeAdapterGenerator.isSupportedList(Utils.getTypeMirrorFromClass(ArrayList.class)));

        // Check unsupported list types
        Assert.assertFalse(
                TypeAdapterGenerator.isSupportedList(Utils.getTypeMirrorFromClass(LinkedList.class)));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedList(Utils.getTypeMirrorFromClass(Vector.class)));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedList(Utils.getTypeMirrorFromClass(Stack.class)));
        Assert.assertFalse(
                TypeAdapterGenerator.isSupportedList(Utils.getTypeMirrorFromClass(Collection.class)));

        Assert.assertFalse(TypeAdapterGenerator.isSupportedList(null));

        Assert.assertFalse(TypeAdapterGenerator.isSupportedList(Utils.getTypeMirrorFromClass(Object.class)));

        // Check unsupported array types
        Assert.assertFalse(TypeAdapterGenerator.isSupportedList(
                types.getArrayType(types.getPrimitiveType(TypeKind.INT))));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedList(
                types.getArrayType(types.getPrimitiveType(TypeKind.BOOLEAN))));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedList(
                types.getArrayType(types.getPrimitiveType(TypeKind.CHAR))));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedList(
                types.getArrayType(Utils.getTypeMirrorFromClass(String.class))));
        Assert.assertFalse(TypeAdapterGenerator.isSupportedList(
                types.getArrayType(Utils.getTypeMirrorFromClass(Object.class))));
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

    @Test
    public void testIsMap_supportsCorrectTypes() throws Exception {
        // Check null
        Assert.assertFalse(TypeAdapterGenerator.isMap(null));

        // Check supported types
        Assert.assertTrue(TypeAdapterGenerator.isMap(Utils.getTypeMirrorFromClass(Map.class)));
        Assert.assertTrue(TypeAdapterGenerator.isMap(Utils.getTypeMirrorFromClass(HashMap.class)));
        Assert.assertTrue(TypeAdapterGenerator.isMap(Utils.getTypeMirrorFromClass(LinkedHashMap.class)));

        // Check type that implements map
        Assert.assertFalse(TypeAdapterGenerator.isMap(Utils.getTypeMirrorFromClass(DummyMapClass.class)));

        // Check other types
        Assert.assertFalse(TypeAdapterGenerator.isMap(Utils.getTypeMirrorFromClass(Object.class)));
        Assert.assertFalse(
                TypeAdapterGenerator.isMap(Utils.getTypeMirrorFromClass(DummyConcreteClass.class)));
        Assert.assertFalse(TypeAdapterGenerator.isMap(Utils.getTypeMirrorFromClass(String.class)));
    }

    @Test
    public void testNumberType_supportsCorrectTypes() throws Exception {

        // Check primitive number types
        Assert.assertTrue(TypeAdapterGenerator.isNumberType(long.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isNumberType(double.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isNumberType(int.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isNumberType(float.class.getName()));

        // Check object number types
        Assert.assertTrue(TypeAdapterGenerator.isNumberType(Long.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isNumberType(Double.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isNumberType(Integer.class.getName()));
        Assert.assertTrue(TypeAdapterGenerator.isNumberType(Float.class.getName()));

        // Check other primitive types
        Assert.assertFalse(TypeAdapterGenerator.isNumberType(byte.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isNumberType(char.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isNumberType(short.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isNumberType(boolean.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isNumberType(void.class.getName()));

        // Check other object types
        Assert.assertFalse(TypeAdapterGenerator.isNumberType(Byte.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isNumberType(Character.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isNumberType(Short.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isNumberType(Boolean.class.getName()));
        Assert.assertFalse(TypeAdapterGenerator.isNumberType(Void.class.getName()));

        Assert.assertFalse(TypeAdapterGenerator.isNumberType(BigInteger.class.getName()));
    }

}
