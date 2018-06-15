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

import com.vimeo.stag.processor.dummy.DummyClassWithConstructor
import com.vimeo.stag.processor.dummy.DummyConcreteClass
import com.vimeo.stag.processor.dummy.DummyGenericClass
import com.vimeo.stag.processor.dummy.DummyInheritedClass
import com.vimeo.stag.processor.utils.ElementUtils
import com.vimeo.stag.processor.utils.TypeUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.ArrayList

class ElementUtilsUnitTest : BaseUnitTest() {

    @Before
    fun setup() {
        ElementUtils.initialize(elements)
        TypeUtils.initialize(types)
    }

    @Test
    fun testConstructor() = Utils.testZeroArgumentConstructorFinalClass(ElementUtils::class.java)

    @Test
    fun testGetTypeFromQualifiedName() {
        fun <T> testTypeMirrorCreationAndEquality(clazz: Class<T>) {
            assertEquals(Utils.getTypeMirrorFromClass(clazz), ElementUtils.getTypeFromQualifiedName(clazz.name))
        }

        fun <T, R> testTypeMirrorCreationAndInequality(clazz: Class<T>, otherClass: Class<R>) {
            assertNotEquals(Utils.getTypeMirrorFromClass(clazz), ElementUtils.getTypeFromQualifiedName(otherClass.name))
        }

        assertNull(ElementUtils.getTypeElementFromQualifiedName(""))

        testTypeMirrorCreationAndEquality(String::class.java)
        testTypeMirrorCreationAndEquality(Any::class.java)
        testTypeMirrorCreationAndEquality(ArrayList::class.java)
        testTypeMirrorCreationAndEquality(DummyConcreteClass::class.java)
        testTypeMirrorCreationAndEquality(DummyGenericClass::class.java)
        testTypeMirrorCreationAndEquality(DummyInheritedClass::class.java)

        testTypeMirrorCreationAndInequality(DummyConcreteClass::class.java, DummyInheritedClass::class.java)
        testTypeMirrorCreationAndInequality(DummyGenericClass::class.java, DummyConcreteClass::class.java)
        testTypeMirrorCreationAndInequality(DummyInheritedClass::class.java, DummyGenericClass::class.java)
    }

    @Test
    fun `isSupportedElementKind returns false for null Element`() {
        assertFalse(ElementUtils.isSupportedElementKind(null))
    }

    @Test
    fun `isSupportedElementKind returns false for non annotated element`() {
        assertFalse(ElementUtils.isSupportedElementKind(Utils.getElementFromClass(String::class.java)))
    }

    @Test
    fun testGetPackage() {
        testPackageEquality(String::class.java)
        testPackageEquality(ArrayList::class.java)
        testPackageEquality(Any::class.java)
        testPackageEquality(DummyGenericClass::class.java)

        testPackageEquality(DummyGenericClass::class.java, DummyConcreteClass::class.java)
        testPackageEquality(DummyConcreteClass::class.java, DummyInheritedClass::class.java)
        testPackageEquality(DummyInheritedClass::class.java, DummyGenericClass::class.java)


        testPackageInequality(Any::class.java, ArrayList::class.java)
        testPackageInequality(List::class.java, String::class.java)
        testPackageInequality(DummyInheritedClass::class.java, Any::class.java)
    }

    private fun testPackageEquality(clazz: Class<out Any>, otherClass: Class<out Any> = clazz) {
        assertEquals(clazz.`package`.name, ElementUtils.getPackage(Utils.getTypeMirrorFromClass(otherClass)))
    }

    private fun <T, R> testPackageInequality(clazz: Class<T>, otherClass: Class<R>) {
        assertNotEquals(clazz.`package`.name, ElementUtils.getPackage(Utils.getTypeMirrorFromClass(otherClass)))
    }

    @Test
    fun testGetConstructor() {
        var executableElement = ElementUtils.getFirstConstructor(Utils.getTypeMirrorFromClass(String::class.java))!!
        assertEquals(executableElement.enclosingElement.toString(), Utils.getElementFromClass(String::class.java).toString())
        assertEquals(executableElement.parameters.size.toLong(), 0)

        executableElement = ElementUtils.getFirstConstructor(Utils.getTypeMirrorFromClass(DummyClassWithConstructor::class.java))!!
        assertEquals(executableElement.enclosingElement.toString(), Utils.getElementFromClass(DummyClassWithConstructor::class.java).toString())
        assertEquals(executableElement.parameters.size.toLong(), 1)
        assertEquals(executableElement.parameters[0].asType().toString(), Utils.getElementFromClass(String::class.java).toString())

        executableElement = ElementUtils.getFirstConstructor(Utils.getTypeMirrorFromClass(DummyGenericClass::class.java))!!
        assertEquals(executableElement.enclosingElement.toString(), Utils.getElementFromClass(DummyGenericClass::class.java).toString())
        assertEquals(executableElement.parameters.size.toLong(), 0)

    }

}
