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

import com.vimeo.stag.processor.utils.Preconditions
import org.junit.Test
import java.util.*

class PreconditionsUnitTest {

    @Test
    fun testFinalClass_isNotInstantiable() =
            Utils.testZeroArgumentConstructorFinalClass(Preconditions::class.java)

    @Test(expected = NullPointerException::class)
    fun checkNotNull_Null_throwsNullPointer() {
        val o: Any? = null
        Preconditions.checkNotNull(o)
    }

    @Test
    fun checkNotNull_NotNull() {
        val o = Any()
        Preconditions.checkNotNull(o)
    }

    @Test
    fun checkNotEmpty_NotEmpty() {
        val list = listOf(Any())

        Preconditions.checkNotEmpty(list)
    }

    @Test(expected = IllegalStateException::class)
    fun checkNotEmpty_Empty_throwsException() {
        val list = ArrayList<Any>()
        Preconditions.checkNotEmpty(list)
    }

    @Test
    fun checkTrue_True() {
        Preconditions.checkTrue(true)
        Preconditions.checkTrue(java.lang.Boolean.TRUE)
    }

    @Test(expected = IllegalStateException::class)
    fun checkTrue_FalsePrimitive_throwsException() = Preconditions.checkTrue(false)

    @Test(expected = IllegalStateException::class)
    fun checkTrue_FalseObject_throwsException() = Preconditions.checkTrue(java.lang.Boolean.FALSE)

}
