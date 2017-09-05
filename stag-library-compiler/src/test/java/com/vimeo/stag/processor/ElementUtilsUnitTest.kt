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
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class ElementUtilsUnitTest : BaseUnitTest() {

    @Before
    fun setup() {
        ElementUtils.initialize(elements)
        TypeUtils.initialize(types)
    }

    @Test
    @Throws(Exception::class)
    fun testConstructor() {
        Utils.testZeroArgumentConstructorFinalClass(ElementUtils::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testGetTypeFromQualifiedName() {
        Assert.assertEquals(Utils.getTypeMirrorFromClass(String::class.java),
                ElementUtils.getTypeFromQualifiedName(String::class.java.name))
        Assert.assertEquals(Utils.getTypeMirrorFromClass(Any::class.java),
                ElementUtils.getTypeFromQualifiedName(Any::class.java.name))
        Assert.assertEquals(Utils.getTypeMirrorFromClass(ArrayList::class.java),
                ElementUtils.getTypeFromQualifiedName(ArrayList::class.java.name))
        Assert.assertEquals(Utils.getTypeMirrorFromClass(DummyConcreteClass::class.java),
                ElementUtils.getTypeFromQualifiedName(DummyConcreteClass::class.java.name))
        Assert.assertEquals(Utils.getTypeMirrorFromClass(DummyGenericClass::class.java),
                ElementUtils.getTypeFromQualifiedName(DummyGenericClass::class.java.name))
        Assert.assertEquals(Utils.getTypeMirrorFromClass(DummyInheritedClass::class.java),
                ElementUtils.getTypeFromQualifiedName(DummyInheritedClass::class.java.name))

        Assert.assertNotEquals(Utils.getTypeMirrorFromClass(DummyConcreteClass::class.java),
                ElementUtils.getTypeFromQualifiedName(DummyInheritedClass::class.java.name))
        Assert.assertNotEquals(Utils.getTypeMirrorFromClass(DummyGenericClass::class.java),
                ElementUtils.getTypeFromQualifiedName(DummyConcreteClass::class.java.name))
        Assert.assertNotEquals(Utils.getTypeMirrorFromClass(DummyInheritedClass::class.java),
                ElementUtils.getTypeFromQualifiedName(DummyGenericClass::class.java.name))
    }

    @Test
    @Throws(Exception::class)
    fun testGetPackage() {
        Assert.assertEquals(String::class.java.`package`.name,
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(String::class.java)!!))
        Assert.assertEquals(ArrayList::class.java.`package`.name,
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(ArrayList::class.java)!!))
        Assert.assertEquals(Any::class.java.`package`.name,
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(Any::class.java)!!))
        Assert.assertEquals(DummyGenericClass::class.java.`package`.name,
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(DummyGenericClass::class.java)!!))

        Assert.assertEquals(DummyGenericClass::class.java.`package`.name,
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(DummyConcreteClass::class.java)!!))
        Assert.assertEquals(DummyConcreteClass::class.java.`package`.name,
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(DummyInheritedClass::class.java)!!))
        Assert.assertEquals(DummyInheritedClass::class.java.`package`.name,
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(DummyGenericClass::class.java)!!))


        Assert.assertNotEquals(Any::class.java.`package`.name,
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(ArrayList::class.java)!!))
        Assert.assertNotEquals(List::class.java.`package`.name,
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(String::class.java)!!))
        Assert.assertNotEquals(DummyInheritedClass::class.java.`package`.name,
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(Any::class.java)!!))
    }

    @Test
    @Throws(Exception::class)
    fun testGetConstructor() {
        var executableElement = ElementUtils.getFirstConstructor(Utils.getTypeMirrorFromClass(String::class.java))
        Assert.assertEquals(executableElement!!.enclosingElement.toString(), Utils.getElementFromClass(String::class.java)!!.toString())
        Assert.assertEquals(executableElement.parameters.size.toLong(), 0)

        executableElement = ElementUtils.getFirstConstructor(Utils.getTypeMirrorFromClass(DummyClassWithConstructor::class.java))
        Assert.assertEquals(executableElement!!.enclosingElement.toString(), Utils.getElementFromClass(DummyClassWithConstructor::class.java)!!.toString())
        Assert.assertEquals(executableElement.parameters.size.toLong(), 1)
        Assert.assertEquals(executableElement.parameters[0].asType().toString(), Utils.getElementFromClass(String::class.java)!!.toString())

        executableElement = ElementUtils.getFirstConstructor(Utils.getTypeMirrorFromClass(DummyGenericClass::class.java))
        Assert.assertEquals(executableElement!!.enclosingElement.toString(), Utils.getElementFromClass(DummyGenericClass::class.java)!!.toString())
        Assert.assertEquals(executableElement.parameters.size.toLong(), 0)

    }

}
