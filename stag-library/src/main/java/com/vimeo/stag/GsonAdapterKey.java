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
package com.vimeo.stag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Use this annotation to tell Stag that a member variable
 * or a class should be processed.
 * <p/>
 * If a member variable of a class is annotated, Stag will
 * generate a TypeAdapter for that class. Stag does not generate
 * TypeAdapters for abstract classes.
 * <p/>
 * If the class is generic (class A), but there is a concrete
 * class (class B) that has annotated members, Stag will generate
 * a TypeAdapter for the concrete class (class B) that also includes
 * all the annotated member variables declared in class A.
 * No adapter for class A will be generated.
 * <p/>
 * If a class (class B) has no annotated members, but inherits from a
 * parent class (class A) with annotated members, Stag will not know to
 * generate a TypeAdapter for that child class (class B). In that case,
 * apply the annotation to the child class declaration (class B) and
 * then Stag will know to generate a TypeAdapter for it. In this case,
 * the optional value that can be passed in the annotation will be
 * ignored as it only applies to class fields, not classes themselves.
 * <p/>
 * See the sample code for examples of each.
 */
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface GsonAdapterKey {

    /**
     * The optional value for the JSON key
     * to which the member variable will be
     * mapped. If none is supplied, then the
     * name of the member variable will be
     * used as the key.
     *
     * @return the value for the JSON key
     * or empty if there is none.
     */
    String value() default "";

}