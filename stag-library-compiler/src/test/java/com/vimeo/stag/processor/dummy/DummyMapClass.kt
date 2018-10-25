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


class DummyMapClass : MutableMap<Any, Any> {
    override val size: Int
        get() = 0

    override fun containsKey(key: Any) = false

    override fun containsValue(value: Any) = false

    override fun get(key: Any): Any? = null

    override fun isEmpty() = true

    override val entries: MutableSet<MutableMap.MutableEntry<Any, Any>>
        get() = mutableSetOf()
    override val keys: MutableSet<Any>
        get() = mutableSetOf()
    override val values: MutableCollection<Any>
        get() = mutableListOf()

    override fun clear() {}

    override fun put(key: Any, value: Any): Nothing? = null

    override fun putAll(from: Map<out Any, Any>) {}

    override fun remove(key: Any): Nothing? = null

}
