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
 * If a member variable of
 * a class is annotated, Stag will generate a TypeAdapter for
 * that class.
 * <p/>
 * If the class is generic, but there is a concrete
 * class that also has annotated fields, Stag will ignore the
 * generic class and generate a TypeAdapter for the concrete
 * class that includes all the annotated member variables in
 * the inheritance hierarchy.
 * <p/>
 * If a class inherits from a parent
 * class with annotated members, but has no annotated members
 * itself, Stag will not know to generate a TypeAdapter for that
 * child class. In that case, apply the annotation to the child
 * class and Stag will pick it up and generate a TypeAdapter for
 * it. In this case, the value that can be specified in the
 * annotation will just be ignored.
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
