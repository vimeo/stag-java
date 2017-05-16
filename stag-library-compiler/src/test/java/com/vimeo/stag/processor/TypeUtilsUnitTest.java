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
package com.vimeo.stag.processor;

import com.vimeo.stag.processor.dummy.DummyAbstractClass;
import com.vimeo.stag.processor.dummy.DummyClassWithConstructor;
import com.vimeo.stag.processor.dummy.DummyConcreteClass;
import com.vimeo.stag.processor.dummy.DummyEnumClass;
import com.vimeo.stag.processor.dummy.DummyGenericClass;
import com.vimeo.stag.processor.dummy.DummyInheritedClass;
import com.vimeo.stag.processor.dummy.DummyMapClass;
import com.vimeo.stag.processor.generators.model.accessor.DirectFieldAccessor;
import com.vimeo.stag.processor.generators.model.accessor.FieldAccessor;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.Vector;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the annotation processor.
 * Run using: {@code ./gradlew :stag-library-compiler:test --continue}
 */
public class TypeUtilsUnitTest extends BaseUnitTest {

    @Before
    public void setup() {
        TypeUtils.initialize(types);
    }

    @Test
    public void testFinalClass_constructorFails() throws Exception {
        Utils.testZeroArgumentConstructorFinalClass(TypeUtils.class);
    }

    @Test
    public void getInheritedType_isCorrect() throws Exception {
        TypeMirror concreteType =
                TypeUtils.getInheritedType(Utils.getElementFromClass(DummyInheritedClass.class));
        assertNotNull(concreteType);

        TypeMirror realConcreteType =
                types.getDeclaredType(Utils.getElementFromClass(DummyGenericClass.class),
                                      Utils.getTypeMirrorFromClass(String.class));

        assertTrue(realConcreteType.toString().equals(concreteType.toString()));

        TypeMirror stringInheritedType = TypeUtils.getInheritedType(Utils.getElementFromClass(String.class));
        assertNull(stringInheritedType);

        TypeMirror enumType = TypeUtils.getInheritedType(Utils.getElementFromClass(DummyEnumClass.class));
        assertNull(enumType);
    }

    /**
     * This test is particularly susceptible to changes
     * in the DummyGenericClass. Any fields
     * added, removed, renamed, or changed, will probably
     * break this test either explicitly, or implicitly.
     * Any changes to that class need to be reflected here.
     *
     * @throws Exception thrown if the test fails.
     */
    @Test
    public void getConcreteMembers_isCorrect() throws Exception {
        Element genericElement = Utils.getElementFromClass(DummyGenericClass.class);
        assertNotNull(genericElement);

        Map<FieldAccessor, TypeMirror> genericMembers = new HashMap<>();
        for (Element element : genericElement.getEnclosedElements()) {
            if (element instanceof VariableElement) {
                genericMembers.put(new DirectFieldAccessor((VariableElement) element), element.asType());
            }
        }

        TypeMirror concreteType =
                TypeUtils.getInheritedType(Utils.getElementFromClass(DummyInheritedClass.class));

        assertNotNull(concreteType);

        TypeMirror genericType = Utils.getGenericVersionOfClass(DummyGenericClass.class);

        assertNotNull(genericType);

        LinkedHashMap<FieldAccessor, TypeMirror> members =
                TypeUtils.getConcreteMembers(concreteType, (TypeElement) types.asElement(genericType), genericMembers);


        TypeMirror stringType = Utils.getTypeMirrorFromClass(String.class);
        assertNotNull(stringType);

        for (Entry<FieldAccessor, TypeMirror> entry : members.entrySet()) {
            if (entry.getKey().createGetterCode().contentEquals("testObject = ")) {

                assertTrue(entry.getValue().toString().equals(stringType.toString()));

            } else if (entry.getKey().createGetterCode().contentEquals("testList = ")) {

                assertTrue(entry.getValue()
                                   .toString()
                                   .equals(types.getDeclaredType(Utils.getElementFromClass(ArrayList.class),
                                           stringType).toString()));

            } else if (entry.getKey().createGetterCode().contentEquals("testMap = ")) {

                assertTrue(entry.getValue()
                                   .toString()
                                   .equals(types.getDeclaredType(Utils.getElementFromClass(HashMap.class), stringType,
                                                                 stringType).toString()));

            } else if (entry.getKey().createGetterCode().contentEquals("testSet = ")) {

                assertTrue(entry.getValue()
                                   .toString()
                                   .equals(types.getDeclaredType(Utils.getElementFromClass(HashSet.class), stringType)
                                                   .toString()));
            } else if (entry.getKey().createGetterCode().contentEquals("testArrayMap = ")) {
                TypeMirror listString = types.getDeclaredType(Utils.getElementFromClass(List.class), stringType);

                assertTrue(entry.getValue()
                        .toString()
                        .equals(types.getDeclaredType(Utils.getElementFromClass(HashMap.class), stringType, listString)
                                .toString()));
            } else if (entry.getKey().createGetterCode().contentEquals("testListMap = ")) {
                TypeMirror mapStringString = types.getDeclaredType(Utils.getElementFromClass(Map.class), stringType, stringType);
                assertTrue(entry.getValue()
                        .toString()
                        .equals(types.getDeclaredType(Utils.getElementFromClass(ArrayList.class), mapStringString)
                                .toString()));
            }
        }
    }

    @Test
    public void isEnum_isCorrect() throws Exception {
        assertTrue(TypeUtils.isEnum(Utils.getElementFromClass(DummyEnumClass.class)));

        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(DummyAbstractClass.class)));
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(DummyClassWithConstructor.class)));
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(DummyConcreteClass.class)));
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(DummyGenericClass.class)));
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(DummyInheritedClass.class)));
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(DummyMapClass.class)));
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(Object.class)));
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(String.class)));
    }

    @Test
    public void isParameterizedType_isCorrect() throws Exception {

        Map<String, List<Object>> testMap = new HashMap<>();
        assertTrue(TypeUtils.isParameterizedType(Utils.getTypeMirrorFromObject(testMap)));

        List<Object> testList = new ArrayList<>();
        assertTrue(TypeUtils.isParameterizedType(Utils.getTypeMirrorFromObject(testList)));

        String testString = "test";
        assertFalse(TypeUtils.isParameterizedType(Utils.getTypeMirrorFromObject(testString)));

        Object testObject = new Object();
        assertFalse(TypeUtils.isParameterizedType(Utils.getTypeMirrorFromObject(testObject)));
    }

    @Test
    public void getOuterClassType_isCorrect() throws Exception {

        // Test different objects
        HashMap<String, List<Object>> testMap = new HashMap<>();
        TypeMirror mapMirror = Utils.getTypeMirrorFromObject(testMap);
        assertNotNull(mapMirror);
        assertTrue(HashMap.class.getName().equals(TypeUtils.getOuterClassType(mapMirror)));

        ArrayList<Object> testList = new ArrayList<>();
        TypeMirror listMirror = Utils.getTypeMirrorFromObject(testList);
        assertNotNull(listMirror);
        assertTrue(ArrayList.class.getName().equals(TypeUtils.getOuterClassType(listMirror)));

        String testString = "test";
        TypeMirror stringMirror = Utils.getTypeMirrorFromObject(testString);
        assertNotNull(stringMirror);
        assertTrue(String.class.getName().equals(TypeUtils.getOuterClassType(stringMirror)));

        Object testObject = new Object();
        TypeMirror objectMirror = Utils.getTypeMirrorFromObject(testObject);
        assertNotNull(objectMirror);
        assertTrue(Object.class.getName().equals(TypeUtils.getOuterClassType(objectMirror)));

        // Test primitives
        assertTrue(int.class.getName()
                           .equals(TypeUtils.getOuterClassType(types.getPrimitiveType(TypeKind.INT))));
    }

    @Test
    public void isConcreteType_Element_isCorrect() throws Exception {

        Element concreteElement = Utils.getElementFromClass(DummyConcreteClass.class);
        assertNotNull(concreteElement);
        for (Element element : concreteElement.getEnclosedElements()) {
            if (element instanceof VariableElement) {
                assertTrue(TypeUtils.isConcreteType(element));
            }
        }

        Element genericElement = Utils.getElementFromClass(DummyGenericClass.class);
        assertNotNull(genericElement);
        for (Element element : genericElement.getEnclosedElements()) {
            if (element instanceof VariableElement) {
                if ("testString".equals(element.getSimpleName().toString())) {
                    assertTrue(TypeUtils.isConcreteType(element));
                } else {
                    assertFalse(TypeUtils.isConcreteType(element));
                }
            }
        }

    }

    @Test
    public void isConcreteType_TypeMirror_isCorrect() throws Exception {

        Element concreteElement = Utils.getElementFromClass(DummyConcreteClass.class);
        assertNotNull(concreteElement);
        for (Element element : concreteElement.getEnclosedElements()) {
            if (element instanceof VariableElement) {
                assertTrue(TypeUtils.isConcreteType(element.asType()));
            }
        }

        Element genericElement = Utils.getElementFromClass(DummyGenericClass.class);
        assertNotNull(genericElement);
        for (Element element : genericElement.getEnclosedElements()) {
            if (element instanceof VariableElement) {
                if ("testString".equals(element.getSimpleName().toString())) {
                    assertTrue(TypeUtils.isConcreteType(element.asType()));
                } else {
                    assertFalse(TypeUtils.isConcreteType(element.asType()));
                }
            }
        }
    }

    @Test
    public void isAbstract_isCorrect() throws Exception {
        Element abstractElement = Utils.getElementFromClass(DummyAbstractClass.class);
        Assert.assertTrue(TypeUtils.isAbstract(abstractElement));

        Element concreteElement = Utils.getElementFromClass(DummyConcreteClass.class);
        Assert.assertFalse(TypeUtils.isAbstract(concreteElement));

        Element genericElement = Utils.getElementFromClass(DummyGenericClass.class);
        Assert.assertFalse(TypeUtils.isAbstract(genericElement));

        Element enumElement = Utils.getElementFromClass(DummyEnumClass.class);
        Assert.assertFalse(TypeUtils.isAbstract(enumElement));

        Element inheritedElement = Utils.getElementFromClass(DummyInheritedClass.class);
        Assert.assertFalse(TypeUtils.isAbstract(inheritedElement));

    }

    @Test
    public void isParameterizedType_Element_isCorrect() throws Exception {
        Map<String, List<Object>> testMap = new HashMap<>();
        assertTrue(TypeUtils.isParameterizedType(Utils.getElementFromObject(testMap)));

        List<Object> testList = new ArrayList<>();
        assertTrue(TypeUtils.isParameterizedType(Utils.getElementFromObject(testList)));

        String testString = "test";
        assertFalse(TypeUtils.isParameterizedType(Utils.getElementFromObject(testString)));

        Object testObject = new Object();
        assertFalse(TypeUtils.isParameterizedType(Utils.getElementFromObject(testObject)));
    }

    @Test
    public void testIsSupportedPrimitive_supportsTypes() throws Exception {

        // Check supported primitives
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(long.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(int.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(boolean.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(float.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(double.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(byte.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(char.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(short.class.getName()));

        // Check unsupported primitives
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
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.INT))));
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.BOOLEAN))));
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.CHAR))));
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(String.class))));
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(Object.class))));
    }

    @Test
    public void testIsSupportedList_supportsTypes() throws Exception {
        // Check supported types
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(List.class)));
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(ArrayList.class)));
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Collection.class)));

        // Check unsupported list types
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(LinkedList.class)));
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Vector.class)));
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Stack.class)));


        Assert.assertFalse(TypeUtils.isSupportedCollection(null));

        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Object.class)));

        // Check supported array types
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.INT))));
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.BOOLEAN))));
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.CHAR))));
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(String.class))));
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(Object.class))));

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
        Assert.assertTrue(TypeUtils.isSupportedNative(byte.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedNative(char.class.getName()));
        Assert.assertTrue(TypeUtils.isSupportedNative(short.class.getName()));

        // Check unsupported primitives
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
        Assert.assertFalse(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(DummyConcreteClass.class)));
        Assert.assertFalse(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(String.class)));
    }
}