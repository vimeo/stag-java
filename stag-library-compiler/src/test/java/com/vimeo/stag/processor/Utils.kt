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

import com.vimeo.stag.processor.dummy.DummyGenericClass
import com.vimeo.stag.processor.utils.Preconditions
import org.junit.Assert.assertTrue
import java.lang.reflect.InvocationTargetException
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

internal object Utils {

    internal lateinit var elements: Elements
    internal lateinit var types: Types

    private fun safeTypes(): Types {
        Preconditions.checkNotNull(types)
        return types
    }

    fun <T> testZeroArgumentConstructorFinalClass(clazz: Class<T>) {
        var exceptionThrown = false
        try {
            val constructor = clazz.getDeclaredConstructor()
            constructor.isAccessible = true
            constructor.newInstance()
        } catch (e: InvocationTargetException) {
            if (e.cause is UnsupportedOperationException) {
                exceptionThrown = true
            }
        }

        assertTrue(exceptionThrown)
    }

    /**
     * Gets the parameterized class with the given parameters.
     *
     * @param clazz      the class to parameterize.
     * @param parameters the parameters to use.
     * @return the declared type mirror with the correct type parameters.
     */
    fun getParameterizedClass(clazz: Class<*>, vararg parameters: Class<*>): DeclaredType {
        val rootType = elements.getTypeElement(clazz.name)
        val params = parameters.map { elements.getTypeElement(it.name).asType() }.toTypedArray()

        return safeTypes().getDeclaredType(rootType, *params)
    }

    fun getElementFromClass(clazz: Class<*>): TypeElement = elements.getTypeElement(clazz.name)

    fun getTypeMirrorFromClass(clazz: Class<*>): TypeMirror {
        val element = getElementFromClass(clazz)
        return element.asType()
    }

    fun getElementFromObject(`object`: Any): TypeElement = elements.getTypeElement(`object`.javaClass.name)

    fun getTypeMirrorFromObject(`object`: Any): TypeMirror {
        val element = getElementFromObject(`object`)
        return element.asType()
    }

    fun getGenericVersionOfClass(clazz: Class<*>): TypeMirror {
        val params = elements.getTypeElement(clazz.name).typeParameters
        val genericTypes = arrayOfNulls<TypeMirror>(params.size)
        for (n in genericTypes.indices) {
            genericTypes[n] = params[n].asType()
        }
        return safeTypes().getDeclaredType(elements.getTypeElement(DummyGenericClass::class.java.name),
                *genericTypes)
    }

}
