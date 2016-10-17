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
package com.vimeo.stag.processor.generators.model;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.type.TypeMirror;

public class ClassInfo {

    @NotNull
    private final String mClassName;

    @NotNull
    private final String mPackageName;

    @NotNull
    private final TypeMirror mType;

    public ClassInfo(@NotNull TypeMirror typeMirror) {
        mType = typeMirror;
        String classAndPackage = typeMirror.toString();

        mPackageName = classAndPackage.substring(0, classAndPackage.lastIndexOf('.'));
        mClassName = classAndPackage.substring(mPackageName.length() + 1, classAndPackage.length());
    }

    /**
     * The name of the class without
     * the package name.
     *
     * @return a valid class name.
     */
    @NotNull
    public String getClassName() {
        return mClassName;
    }

    /**
     * The package name of the class.
     *
     * @return a valid package name.
     */
    @NotNull
    public String getPackageName() {
        return mPackageName;
    }

    /**
     * The full unmodified package name
     * and class name of this class object.
     *
     * @return a valid class and package name.
     */
    @NotNull
    public String getClassAndPackage() {
        return mType.toString();
    }

    /**
     * The TypeMirror object backing this
     * ClassInfo object.
     *
     * @return the TypeMirror object used
     * to instantiate this class.
     */
    @NotNull
    public TypeMirror getType() {
        return mType;
    }

}
