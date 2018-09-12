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
import com.vimeo.stag.processor.utils.logging.DebugLog
import com.vimeo.stag.processor.utils.logging.NoOpLogger
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*
import javax.lang.model.element.Element
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
        DebugLog.initialize(NoOpLogger())
    }

    @Test
    @Throws(Exception::class)
    fun testFinalClass_constructorFails() =
            Utils.testZeroArgumentConstructorFinalClass(TypeUtils::class.java)

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
     */
    @Test
    fun getConcreteMembers_isCorrect() {
        val genericElement = Utils.getElementFromClass(DummyGenericClass::class.java)
        assertNotNull(genericElement)

        val genericMembers = HashMap<FieldAccessor, TypeMirror>()
        genericElement.enclosedElements
                .filterIsInstance<VariableElement>()
                .forEach { genericMembers[DirectFieldAccessor(it)] = it.asType() }

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
    fun `isAbstract (Element) is correct`() {
        assertFalse(TypeUtils.isAbstract(null as Element?))

        val abstractElement = Utils.getElementFromClass(DummyAbstractClass::class.java)
        assertTrue(TypeUtils.isAbstract(abstractElement))

        val concreteElement = Utils.getElementFromClass(DummyConcreteClass::class.java)
        assertFalse(TypeUtils.isAbstract(concreteElement))

        val genericElement = Utils.getElementFromClass(DummyGenericClass::class.java)
        assertFalse(TypeUtils.isAbstract(genericElement))

        val enumElement = Utils.getElementFromClass(DummyEnumClass::class.java)
        assertFalse(TypeUtils.isAbstract(enumElement))

        val inheritedElement = Utils.getElementFromClass(DummyInheritedClass::class.java)
        assertFalse(TypeUtils.isAbstract(inheritedElement))
    }

    @Test
    fun `isAbstract (TypeMirror) is correct`() {
        assertFalse(TypeUtils.isAbstract(null as TypeMirror?))

        val abstractElement = Utils.getTypeMirrorFromClass(DummyAbstractClass::class.java)
        assertTrue(TypeUtils.isAbstract(abstractElement))

        val concreteElement = Utils.getTypeMirrorFromClass(DummyConcreteClass::class.java)
        assertFalse(TypeUtils.isAbstract(concreteElement))

        val genericElement = Utils.getTypeMirrorFromClass(DummyGenericClass::class.java)
        assertFalse(TypeUtils.isAbstract(genericElement))

        val enumElement = Utils.getTypeMirrorFromClass(DummyEnumClass::class.java)
        assertFalse(TypeUtils.isAbstract(enumElement))

        val inheritedElement = Utils.getTypeMirrorFromClass(DummyInheritedClass::class.java)
        assertFalse(TypeUtils.isAbstract(inheritedElement))
    }

    @Test
    fun `isParameterizedType (TypeElement) is correct`() {
        assertFalse(TypeUtils.isParameterizedType(null as TypeElement?))

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
        assertTrue(TypeUtils.isSupportedPrimitive(Long::class.javaPrimitiveType!!.name))
        assertTrue(TypeUtils.isSupportedPrimitive(Int::class.javaPrimitiveType!!.name))
        assertTrue(TypeUtils.isSupportedPrimitive(Boolean::class.javaPrimitiveType!!.name))
        assertTrue(TypeUtils.isSupportedPrimitive(Float::class.javaPrimitiveType!!.name))
        assertTrue(TypeUtils.isSupportedPrimitive(Double::class.javaPrimitiveType!!.name))
        assertTrue(TypeUtils.isSupportedPrimitive(Byte::class.javaPrimitiveType!!.name))
        assertTrue(TypeUtils.isSupportedPrimitive(Char::class.javaPrimitiveType!!.name))
        assertTrue(TypeUtils.isSupportedPrimitive(Short::class.javaPrimitiveType!!.name))

        // Check unsupported primitives
        assertFalse(TypeUtils.isSupportedPrimitive(Void.TYPE.name))

        // Check non-primitives
        assertFalse(TypeUtils.isSupportedPrimitive(String::class.java.name))
        assertFalse(TypeUtils.isSupportedPrimitive(Any::class.java.name))
    }

    @Test
    @Throws(Exception::class)
    fun testIsMap_supportsCorrectTypes() {
        // Check null
        assertFalse(TypeUtils.isSupportedMap(null))

        // Check supported types
        assertTrue(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(Map::class.java)))
        assertTrue(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(HashMap::class.java)))
        assertTrue(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(LinkedHashMap::class.java)))

        // Check type that implements map
        assertFalse(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(DummyMapClass::class.java)))

        // Check other types
        assertFalse(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(Any::class.java)))
        assertFalse(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(DummyConcreteClass::class.java)))
        assertFalse(TypeUtils.isSupportedMap(Utils.getTypeMirrorFromClass(String::class.java)))
    }
}
