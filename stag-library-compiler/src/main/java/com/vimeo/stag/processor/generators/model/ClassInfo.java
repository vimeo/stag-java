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

import com.vimeo.stag.processor.utils.ElementUtils;
import com.vimeo.stag.processor.utils.FileGenUtils;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.lang.model.type.TypeMirror;

public final class ClassInfo {

    @NotNull
    private final String mClassName;

    @NotNull
    private final String mPackageName;

    @NotNull
    private final String mTypeName;

    @NotNull
    private final TypeMirror mType;

    public ClassInfo(@NotNull TypeMirror typeMirror) {
        mType = typeMirror;
        mPackageName = ElementUtils.getPackage(mType);

        String classAndPackage = mType.toString();

        /**
         * This is done to avoid the generic template from being included in the file name to be generated
         * (since it will be an invalid file name)
         */
        int idx = classAndPackage.indexOf("<");
        if (idx > 0) {
            classAndPackage = classAndPackage.substring(0, idx);
        }
        mTypeName = classAndPackage;
        mClassName = classAndPackage.substring(mPackageName.length() + 1, classAndPackage.length())
                .replaceAll("\\.", "\\$");
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
     * The simple class name of the {@link com.google.gson.TypeAdapter} class for this model class.
     *
     * @return simple class name
     */
    @NotNull
    public String getTypeAdapterClassName() {
        return FileGenUtils.escapeStringForCodeBlock(mClassName + "$TypeAdapter");
    }

    /**
     * The fully qualified class name of the {@link com.google.gson.TypeAdapter} class for this
     * model class.
     *
     * @return qualified class name
     */
    @NotNull
    public String getTypeAdapterQualifiedClassName() {
        return mPackageName + "." + getTypeAdapterClassName();
    }

    /**
     * The full unmodified package name
     * and class name of this class object.
     *
     * @return a valid class and package name.
     */
    @NotNull
    public String getClassAndPackage() {
        return mTypeName;
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

    @Nullable
    public List<? extends TypeMirror> getTypeArguments() {
        return TypeUtils.getTypeArguments(mType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClassInfo classInfo = (ClassInfo) o;

        return mClassName.equals(classInfo.mClassName) && mPackageName.equals(classInfo.mPackageName) &&
               mTypeName.equals(classInfo.mTypeName) && mType.equals(classInfo.mType);

    }

    @Override
    public int hashCode() {
        int result = mClassName.hashCode();
        result = 31 * result + mPackageName.hashCode();
        result = 31 * result + mTypeName.hashCode();
        result = 31 * result + mType.hashCode();
        return result;
    }
}