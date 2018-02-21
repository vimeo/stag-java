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
package com.vimeo.stag.processor.dummy

import com.vimeo.stag.processor.TypeUtilsUnitTest
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

/**
 * Do not change this class without sure the
 * [TypeUtilsUnitTest.getConcreteMembers_isCorrect]
 * test still works correctly. All members of
 * this class should be tested by that test,
 * and any generic ones here should be explicitly
 * checked in the test to make sure they are
 * resolved correctly.
 *
 * @param <T> the type the the inheriting type
 * should be of.
</T> */
open class DummyGenericClass<T> {

    internal var testString: String? = null

    internal var testObject: T? = null

    internal var testList: ArrayList<T>? = null

    internal var testMap: HashMap<String, T>? = null

    internal var testSet: HashSet<T>? = null

    internal var testArrayMap: HashMap<String, List<T>>? = null

    internal var testListMap: ArrayList<Map<String, T>>? = null

}
