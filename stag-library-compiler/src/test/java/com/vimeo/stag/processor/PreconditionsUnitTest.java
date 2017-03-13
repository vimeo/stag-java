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

import com.vimeo.stag.processor.utils.Preconditions;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PreconditionsUnitTest {

    @Test
    public void testFinalClass_isNotInstantiable() throws Exception {
        Utils.testZeroArgumentConstructorFinalClass(Preconditions.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void checkNotNull_Null_throwsNullPointer() {
        Object o = null;
        Preconditions.checkNotNull(o);
    }

    @Test
    public void checkNotNull_NotNull() throws Exception {
        Object o = new Object();
        Preconditions.checkNotNull(o);
    }

    @Test
    public void checkNotEmpty_NotEmpty() throws Exception {
        List<Object> list = new ArrayList<Object>() {{
            add(new Object());
        }};

        Preconditions.checkNotEmpty(list);
    }

    @Test(expected = IllegalStateException.class)
    public void checkNotEmpty_Empty_throwsException() {
        List list = new ArrayList();
        Preconditions.checkNotEmpty(list);
    }

    @Test
    public void checkTrue_True() throws Exception {
        Preconditions.checkTrue(true);
        Preconditions.checkTrue(Boolean.TRUE);
    }

    @Test(expected = IllegalStateException.class)
    public void checkTrue_FalsePrimitive_throwsException() {
        Preconditions.checkTrue(false);
    }

    @Test(expected = IllegalStateException.class)
    public void checkTrue_FalseObject_throwsException() {
        Preconditions.checkTrue(Boolean.FALSE);
    }

}
