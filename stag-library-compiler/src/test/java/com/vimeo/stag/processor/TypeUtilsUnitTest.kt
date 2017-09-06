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
package com.vimeo.stag.processor

import com.vimeo.stag.processor.dummy.*
import com.vimeo.stag.processor.generators.model.accessor.DirectFieldAccessor
import com.vimeo.stag.processor.generators.model.accessor.FieldAccessor
import com.vimeo.stag.processor.utils.TypeUtils
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

/**
 * Unit tests for the annotation processor.
 * Run using: `./gradlew :stag-library-compiler:test --continue`
 */
class TypeUtilsUnitTest : BaseUnitTest() {

    @Before
    fun setup() {
        TypeUtils.initialize(types)
    }

    @Test
    @Throws(Exception::class)
    fun testFinalClass_constructorFails() {
        Utils.testZeroArgumentConstructorFinalClass(TypeUtils::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun getInheritedType_isCorrect() {
        val concreteType = TypeUtils.getInheritedType(Utils.getElementFromClass(DummyInheritedClass::class.java))
        assertNotNull(concreteType)

        val realConcreteType = types.getDeclaredType(Utils.getElementFromClass(DummyGenericClass::class.java),
                Utils.getTypeMirrorFromClass(String::class.java))

        assertTrue(realConcreteType.toString() == concreteType!!.toString())

        val stringInheritedType = TypeUtils.getInheritedType(Utils.getElementFromClass(String::class.java))
        assertNull(stringInheritedType)

        val enumType = TypeUtils.getInheritedType(Utils.getElementFromClass(DummyEnumClass::class.java))
        assertNull(enumType)
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
    fun getConcreteMembers_isCorrect() {
        val genericElement = Utils.getElementFromClass(DummyGenericClass::class.java)
        assertNotNull(genericElement)

        val genericMembers = HashMap<FieldAccessor, TypeMirror>()
        genericElement.enclosedElements
                .filterIsInstance<VariableElement>()
                .forEach { genericMembers.put(DirectFieldAccessor(it), it.asType()) }

        val concreteType = TypeUtils.getInheritedType(Utils.getElementFromClass(DummyInheritedClass::class.java))

        assertNotNull(concreteType)

        val genericType = Utils.getGenericVersionOfClass(DummyGenericClass::class.java)

        assertNotNull(genericType)

        val members = TypeUtils.getConcreteMembers(concreteType!!, types.asElement(genericType) as TypeElement, genericMembers)


        val stringType = Utils.getTypeMirrorFromClass(String::class.java)
        assertNotNull(stringType)

        for ((key, value) in members) {
            val getterCode = key.createGetterCode()
            when {
                getterCode.contentEquals("testObject = ") ->
                    assertTrue(value.toString() == stringType.toString())
                getterCode.contentEquals("testList = ") ->
                    assertTrue(value.toString() == types.getDeclaredType(Utils.getElementFromClass(ArrayList::class.java), stringType).toString())
                getterCode.contentEquals("testMap = ") ->
                    assertTrue(value.toString() == types.getDeclaredType(Utils.getElementFromClass(HashMap::class.java), stringType, stringType).toString())
                getterCode.contentEquals("testSet = ") ->
                    assertTrue(value.toString() == types.getDeclaredType(Utils.getElementFromClass(HashSet::class.java), stringType).toString())
                getterCode.contentEquals("testArrayMap = ") -> {
                    val listString = types.getDeclaredType(Utils.getElementFromClass(List::class.java), stringType)

                    assertTrue(value.toString() == types.getDeclaredType(Utils.getElementFromClass(HashMap::class.java), stringType, listString).toString())
                }
                getterCode.contentEquals("testListMap = ") -> {
                    val mapStringString = types.getDeclaredType(Utils.getElementFromClass(Map::class.java), stringType, stringType)

                    assertTrue(value.toString() == types.getDeclaredType(Utils.getElementFromClass(ArrayList::class.java), mapStringString).toString())
                }
            }
        }
    }

    @Test
    fun isEnum_isCorrect() {
        assertTrue(TypeUtils.isEnum(Utils.getElementFromClass(DummyEnumClass::class.java)))

        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(DummyAbstractClass::class.java)))
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(DummyClassWithConstructor::class.java)))
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(DummyConcreteClass::class.java)))
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(DummyGenericClass::class.java)))
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(DummyInheritedClass::class.java)))
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(DummyMapClass::class.java)))
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(Any::class.java)))
        assertFalse(TypeUtils.isEnum(Utils.getElementFromClass(String::class.java)))
    }

    @Test
    fun areEqual_isCorrect() {
        assertTrue(TypeUtils.areEqual(Utils.getTypeMirrorFromClass(Any::class.java), Utils.getTypeMirrorFromClass(Any::class.java)))
        assertTrue(TypeUtils.areEqual(Utils.getTypeMirrorFromClass(String::class.java), Utils.getTypeMirrorFromClass(String::class.java)))
        assertTrue(TypeUtils.areEqual(Utils.getTypeMirrorFromClass(List::class.java), Utils.getTypeMirrorFromClass(List::class.java)))

        assertFalse(TypeUtils.areEqual(Utils.getTypeMirrorFromClass(Any::class.java), Utils.getTypeMirrorFromClass(String::class.java)))
        assertFalse(TypeUtils.areEqual(Utils.getTypeMirrorFromClass(String::class.java), Utils.getTypeMirrorFromClass(List::class.java)))
        assertFalse(TypeUtils.areEqual(Utils.getTypeMirrorFromClass(List::class.java), Utils.getTypeMirrorFromClass(ArrayList::class.java)))

        assertTrue(TypeUtils.areEqual(Utils.getParameterizedClass(List::class.java, String::class.java), Utils.getParameterizedClass(List::class.java, String::class.java)))
        assertFalse(TypeUtils.areEqual(Utils.getParameterizedClass(List::class.java, String::class.java), Utils.getParameterizedClass(List::class.java, Int::class.javaObjectType)))
    }

    @Test
    fun isParameterizedType_isCorrect() {

        val testMap = HashMap<String, List<Any>>()
        assertTrue(TypeUtils.isParameterizedType(Utils.getTypeMirrorFromObject(testMap)))

        val testList = ArrayList<Any>()
        assertTrue(TypeUtils.isParameterizedType(Utils.getTypeMirrorFromObject(testList)))

        val testString = "test"
        assertFalse(TypeUtils.isParameterizedType(Utils.getTypeMirrorFromObject(testString)))

        val testObject = Any()
        assertFalse(TypeUtils.isParameterizedType(Utils.getTypeMirrorFromObject(testObject)))
    }

    @Test
    fun getOuterClassType_isCorrect() {

        // Test different objects
        val testMap = HashMap<String, List<Any>>()
        val mapMirror = Utils.getTypeMirrorFromObject(testMap)
        assertNotNull(mapMirror)
        assertTrue(HashMap::class.java.name == TypeUtils.getOuterClassType(mapMirror))

        val testList = ArrayList<Any>()
        val listMirror = Utils.getTypeMirrorFromObject(testList)
        assertNotNull(listMirror)
        assertTrue(ArrayList::class.java.name == TypeUtils.getOuterClassType(listMirror))

        val testString = "test"
        val stringMirror = Utils.getTypeMirrorFromObject(testString)
        assertNotNull(stringMirror)
        assertTrue(String::class.java.name == TypeUtils.getOuterClassType(stringMirror))

        val testObject = Any()
        val objectMirror = Utils.getTypeMirrorFromObject(testObject)
        assertNotNull(objectMirror)
        assertTrue(Any::class.java.name == TypeUtils.getOuterClassType(objectMirror))

        // Test primitives
        assertTrue(Int::class.javaPrimitiveType!!.name == TypeUtils.getOuterClassType(types.getPrimitiveType(TypeKind.INT)))
    }

    @Test
    @Throws(Exception::class)
    fun isConcreteType_Element_isCorrect() {

        val concreteElement = Utils.getElementFromClass(DummyConcreteClass::class.java)
        assertNotNull(concreteElement)
        concreteElement.enclosedElements
                .filterIsInstance<VariableElement>()
                .forEach { assertTrue(TypeUtils.isConcreteType(it)) }

        val genericElement = Utils.getElementFromClass(DummyGenericClass::class.java)
        assertNotNull(genericElement)
        genericElement.enclosedElements
                .filterIsInstance<VariableElement>()
                .forEach {
                    if ("testString" == it.simpleName.toString()) {
                        assertTrue(TypeUtils.isConcreteType(it))
                    } else {
                        assertFalse(TypeUtils.isConcreteType(it))
                    }
                }

    }

    @Test
    @Throws(Exception::class)
    fun isConcreteType_TypeMirror_isCorrect() {

        val concreteElement = Utils.getElementFromClass(DummyConcreteClass::class.java)
        assertNotNull(concreteElement)
        concreteElement.enclosedElements
                .filterIsInstance<VariableElement>()
                .forEach { assertTrue(TypeUtils.isConcreteType(it.asType())) }

        val genericElement = Utils.getElementFromClass(DummyGenericClass::class.java)
        assertNotNull(genericElement)
        genericElement.enclosedElements
                .filterIsInstance<VariableElement>()
                .forEach {
                    if ("testString" == it.simpleName.toString()) {
                        assertTrue(TypeUtils.isConcreteType(it.asType()))
                    } else {
                        assertFalse(TypeUtils.isConcreteType(it.asType()))
                    }
                }
    }

    @Test
    @Throws(Exception::class)
    fun isAbstract_isCorrect() {
        val abstractElement = Utils.getElementFromClass(DummyAbstractClass::class.java)
        Assert.assertTrue(TypeUtils.isAbstract(abstractElement))

        val concreteElement = Utils.getElementFromClass(DummyConcreteClass::class.java)
        Assert.assertFalse(TypeUtils.isAbstract(concreteElement))

        val genericElement = Utils.getElementFromClass(DummyGenericClass::class.java)
        Assert.assertFalse(TypeUtils.isAbstract(genericElement))

        val enumElement = Utils.getElementFromClass(DummyEnumClass::class.java)
        Assert.assertFalse(TypeUtils.isAbstract(enumElement))

        val inheritedElement = Utils.getElementFromClass(DummyInheritedClass::class.java)
        Assert.assertFalse(TypeUtils.isAbstract(inheritedElement))

    }

    @Test
    @Throws(Exception::class)
    fun isParameterizedType_Element_isCorrect() {
        val testMap = HashMap<String, List<Any>>()
        assertTrue(TypeUtils.isParameterizedType(Utils.getElementFromObject(testMap)))

        val testList = ArrayList<Any>()
        assertTrue(TypeUtils.isParameterizedType(Utils.getElementFromObject(testList)))

        val testString = "test"
        assertFalse(TypeUtils.isParameterizedType(Utils.getElementFromObject(testString)))

        val testObject = Any()
        assertFalse(TypeUtils.isParameterizedType(Utils.getElementFromObject(testObject)))
    }

    @Test
    @Throws(Exception::class)
    fun testIsSupportedPrimitive_supportsTypes() {

        // Check supported primitives
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(Long::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(Int::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(Boolean::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(Float::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(Double::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(Byte::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(Char::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedPrimitive(Short::class.javaPrimitiveType!!.name))

        // Check unsupported primitives
        Assert.assertFalse(TypeUtils.isSupportedPrimitive(Void.TYPE.name))

        // Check non-primitives
        Assert.assertFalse(TypeUtils.isSupportedPrimitive(String::class.java.name))
        Assert.assertFalse(TypeUtils.isSupportedPrimitive(Any::class.java.name))
    }

    @Test
    @Throws(Exception::class)
    fun testIsSupportedCollection_supportsTypes() {
        // Check supported list types
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(List::class.java)))
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(ArrayList::class.java)))
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Collection::class.java)))

        // Check unsupported list types
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(LinkedList::class.java)))
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Vector::class.java)))
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Stack::class.java)))

        Assert.assertFalse(TypeUtils.isSupportedCollection(null))

        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Any::class.java)))

        // Check array types
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.INT))))
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.BOOLEAN))))
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.CHAR))))
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(String::class.java))))
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(Any::class.java))))
    }

    @Test
    @Throws(Exception::class)
    fun testIsSupportedList_supportsTypes() {
        // Check supported types
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(List::class.java)))
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(ArrayList::class.java)))
        Assert.assertTrue(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Collection::class.java)))

        // Check unsupported list types
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(LinkedList::class.java)))
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Vector::class.java)))
        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Stack::class.java)))


        Assert.assertFalse(TypeUtils.isSupportedCollection(null))

        Assert.assertFalse(TypeUtils.isSupportedCollection(Utils.getTypeMirrorFromClass(Any::class.java)))

        // Check supported array types
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.INT))))
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.BOOLEAN))))
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(types.getPrimitiveType(TypeKind.CHAR))))
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(String::class.java))))
        Assert.assertTrue(TypeUtils.isSupportedCollection(types.getArrayType(Utils.getTypeMirrorFromClass(Any::class.java))))

    }

    @Test
    @Throws(Exception::class)
    fun testIsSupportedNative_supportsCorrectTypes() {
        // Check supported primitives
        Assert.assertTrue(TypeUtils.isSupportedNative(Long::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedNative(Int::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedNative(Boolean::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedNative(Float::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedNative(Double::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedNative(String::class.java.name))
        Assert.assertTrue(TypeUtils.isSupportedNative(Byte::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedNative(Char::class.javaPrimitiveType!!.name))
        Assert.assertTrue(TypeUtils.isSupportedNative(Short::class.javaPrimitiveType!!.name))

        // Check unsupported primitives
        Assert.assertFalse(TypeUtils.isSupportedNative(Void.TYPE.name))

        // Check non-primitives
        Assert.assertFalse(TypeUtils.isSupportedNative(Any::class.java.name))
    }

    @Test
    @Throws(Exception::class)
    fun testIsMap_supportsCorrectTypes() {
        // Check null
        Assert.assertFalse(TypeUtils.isSupportedMap(null))

        // Check supported types
        Assert.assertTrue(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(Map::class.java)))
        Assert.assertTrue(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(HashMap::class.java)))
        Assert.assertTrue(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(LinkedHashMap::class.java)))

        // Check type that implements map
        Assert.assertFalse(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(DummyMapClass::class.java)))

        // Check other types
        Assert.assertFalse(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(Any::class.java)))
        Assert.assertFalse(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(DummyConcreteClass::class.java)))
        Assert.assertFalse(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(String::class.java)))
    }
}