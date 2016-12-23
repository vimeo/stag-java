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
package com.vimeo.stag.processor.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public final class TypeUtils {

    private static final String TAG = TypeUtils.class.getSimpleName();
    private static Types sTypeUtils;

    private TypeUtils() {
        throw new UnsupportedOperationException("This class is not instantiable");
    }

    public static void initialize(@NotNull Types typeUtils) {
        sTypeUtils = typeUtils;
    }

    @NotNull
    public static Types getUtils() {
        Preconditions.checkNotNull(sTypeUtils);
        return sTypeUtils;
    }

    /**
     * Retrieves the outer type of a parameterized class.
     * e.g. an ArrayList{@literal <T>} would be returned as
     * just ArrayList. If an interface is passed in, i.e. a
     * List, the underlying implementation will be returned,
     * i.e. ArrayList.
     *
     * @param type the type to get the outer class from/
     * @return the outer class of the type passed in, or the
     * type itself if it is not parameterized.
     */
    @NotNull
    public static String getOuterClassType(@NotNull TypeMirror type) {
        if (type instanceof DeclaredType) {
            return ((DeclaredType) type).asElement().toString();
        } else {
            return type.toString();
        }
    }

    /**
     * Determines whether or not the type has type parameters.
     *
     * @param type the type to check.
     * @return true if the type is not null and has type parameters,
     * false otherwise.
     */
    public static boolean isParameterizedType(@Nullable TypeMirror type) {
        List<? extends TypeMirror> typeArguments = getTypeArguments(type);
        return null != typeArguments && !typeArguments.isEmpty();
    }

    /**
     * Determines whether or not the type has type parameters.
     *
     * @param type the type to check.
     * @return true if the type is not null and has type parameters,
     * false otherwise.
     */
    @Nullable
    public static List<? extends TypeMirror> getTypeArguments(@Nullable TypeMirror type) {
        return type instanceof DeclaredType ? ((DeclaredType) type).getTypeArguments() : null;
    }

    /**
     * Determines whether or not the Element is a concrete type.
     * If the element is a generic type or contains generic type
     * arguments, this method will return false.
     *
     * @param element the element to check.
     * @return true if the element is not generic and
     * contains no generic type arguments, false otherwise.
     */
    public static boolean isConcreteType(@NotNull Element element) {
        return isConcreteType(element.asType());
    }

    /**
     * Determines whether or not the Element is a abstract type.
     *
     * @param element the element to check.
     * @return true if the element is abstract and
     * contains no generic type arguments, false otherwise.
     */
    public static boolean isAbstract(@Nullable Element element) {
        return element != null && element.getModifiers().contains(Modifier.ABSTRACT);
    }

    /**
     * Determines whether or not the Element is a parameterized type.
     * If the element is a parameterized type or contains parameterized type
     * arguments, this method will return false.
     *
     * @param element the element to check.
     * @return true if the element is not generic and
     * contains no generic type arguments, false otherwise.
     */
    public static boolean isParameterizedType(@Nullable Element element) {
        return element != null && isParameterizedType(element.asType());
    }

    /**
     * Determines whether or not the TypeMirror is a concrete type.
     * If the type is a generic type or contains generic type
     * arguments (i.e. a parameterized type), this method will
     * return false.
     *
     * @param typeMirror the element to check.
     * @return true if the type is not generic and
     * contains no generic type arguments, false otherwise.
     */
    public static boolean isConcreteType(@NotNull TypeMirror typeMirror) {
        if (typeMirror.getKind() == TypeKind.TYPEVAR) {
            return false;
        }
        if (isPrimitive(typeMirror, sTypeUtils)) {
            return true;
        }
        if (typeMirror instanceof DeclaredType) {
            List<? extends TypeMirror> typeMirrors = ((DeclaredType) typeMirror).getTypeArguments();

            for (TypeMirror type : typeMirrors) {
                if (!isConcreteType(type)) {
                    return false;
                }
            }
        }
        return true;
    }


    public static boolean containsTypeVarParams(@NotNull TypeMirror typeMirror) {
        if (typeMirror.getKind() == TypeKind.TYPEVAR) {
            return true;
        }

        if (typeMirror instanceof DeclaredType) {
            List<? extends TypeMirror> typeMirrors = ((DeclaredType) typeMirror).getTypeArguments();

            for (TypeMirror type : typeMirrors) {
                if (containsTypeVarParams(type)) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Gets the inherited type from the element. If
     * the inherited type is Object, then this method
     * will return null.
     *
     * @param element the element to get the inherited type.
     * @return the inherited type, or null if the element
     * inherits from Object.
     */
    @Nullable
    public static TypeMirror getInheritedType(@Nullable Element element) {
        TypeElement typeElement = (TypeElement) element;
        if (typeElement != null && !typeElement.getSuperclass().toString().equals(Object.class.getName())) {
            return typeElement.getSuperclass();
        }
        return null;
    }

    /**
     * Retrieves a Map of the inherited concrete member variables of an Element. This takes all the
     * member variables that were inherited from the generic parent class and evaluates what their concrete
     * type will be based on the concrete inherited type. For instance, take the following code example:
     * <pre>
     * {@code
     * Factory<T> {
     *
     *  @literal @UseStag
     *   public T data;
     *
     * }
     *
     * VideoFactory extends Factory<Video> {
     *
     *   // other variables in here
     *
     * }
     * }
     * </pre>
     * In this example, VideoFactory has a public member variable T that is of type Video.
     * Since the Factory class has the UseStag annotation, we cannot just generate
     * parsing code for the Factory class, since it is generic and we need concrete types.
     * Instead when we generate the adapter for VideoFactory, we crawl the inheritance
     * hierarchy gathering the member variables. When we get to VideoFactory, we see it
     * has one member variable, T. We then look at the inherited type, Factory{@literal <Video>},
     * and compare it to the original type, Factory{@literal <T>}, and then infer the type
     * of T to be Video.
     *
     * @param concreteInherited the type inherited for the class you are using, in the example,
     *                          this would be Factory{@literal <Video>}
     * @param genericInherited  the raw type inherited for the class you are using, in the example,
     *                          this would be Factory{@literal <T>}
     * @param members           the member variable map of the field (Element) to their concrete
     *                          type (TypeMirror). This should be retrieved by calling getConcreteMembers
     *                          on the inherited class.
     * @return returns a map of the member variables mapped to their concrete types for the concrete
     * inherited class.
     */
    @NotNull
    public static Map<Element, TypeMirror> getConcreteMembers(@NotNull TypeMirror concreteInherited,
                                                              @NotNull Element genericInherited,
                                                              @NotNull Map<Element, TypeMirror> members) {

        DebugLog.log(TAG, "Inherited concrete type: " + concreteInherited.toString());
        DebugLog.log(TAG, "Inherited generic type: " + genericInherited.asType().toString());
        List<? extends TypeMirror> concreteTypes = getParameterizedTypes(concreteInherited);
        List<? extends TypeMirror> inheritedTypes = getParameterizedTypes(genericInherited);

        Map<Element, TypeMirror> map = new HashMap<>();

        for (Entry<Element, TypeMirror> member : members.entrySet()) {

            DebugLog.log(TAG, "\t\tEvaluating member - " + member.getValue().toString());

            if (isConcreteType(member.getValue())) {

                DebugLog.log(TAG, "\t\t\tConcrete Type: " + member.getValue().toString());
                map.put(member.getKey(), member.getValue());

            } else {

                if (isParameterizedType(member.getValue())) {

                    // HashMap<String, T> ...

                    List<TypeMirror> genericTypes = getMemberTypes(member.getValue());


                    List<TypeMirror> concreteGenericTypes = new ArrayList<>();

                    for (TypeMirror genericType : genericTypes) {
                        if (isConcreteType(genericType)) {
                            concreteGenericTypes.add(genericType);
                        } else {
                            int index = inheritedTypes.indexOf(genericType);
                            concreteGenericTypes.add(concreteTypes.get(index));
                        }
                    }

                    TypeElement typeElement = (TypeElement) sTypeUtils.asElement(member.getValue());
                    TypeMirror[] concreteTypeArray =
                            concreteGenericTypes.toArray(new TypeMirror[concreteGenericTypes.size()]);

                    DeclaredType declaredType = sTypeUtils.getDeclaredType(typeElement, concreteTypeArray);

                    map.put(member.getKey(), declaredType);

                    DebugLog.log(TAG, "\t\t\tGeneric Parameterized Type - " + member.getValue().toString() +
                            " resolved to - " + declaredType.toString());
                } else {

                    int index = inheritedTypes.indexOf(member.getKey().asType());
                    TypeMirror concreteType = concreteTypes.get(index);
                    map.put(member.getKey(), concreteType);

                    DebugLog.log(TAG, "\t\t\tGeneric Type - " + member.getValue().toString() +
                            " resolved to - " + concreteType.toString());
                }
            }
        }
        return map;
    }

    private static boolean isPrimitive(@NotNull TypeMirror type, @NotNull Types utils) {
        try {
            utils.getPrimitiveType(type.getKind());
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    @NotNull
    private static List<TypeMirror> getMemberTypes(@NotNull TypeMirror element) {
        List<TypeMirror> genericTypes = new ArrayList<>();
        if (element.getKind() != TypeKind.TYPEVAR) {
            List<? extends TypeMirror> typeMirrors = ((DeclaredType) element).getTypeArguments();
            if (typeMirrors.isEmpty()) {
                genericTypes.add(element);
            } else {
                for (TypeMirror type : typeMirrors) {
                    if (type.getKind() == TypeKind.TYPEVAR) {
                        genericTypes.add(type);
                    } else {
                        genericTypes.addAll(getMemberTypes(type));
                    }
                }
            }
        }

        // if the type is not parameterized, we will return an empty list
        return genericTypes;
    }

    @NotNull
    private static List<? extends TypeMirror> getParameterizedTypes(@NotNull Element element) {
        return ((DeclaredType) element.asType()).getTypeArguments();
    }

    @NotNull
    private static List<? extends TypeMirror> getParameterizedTypes(@NotNull TypeMirror typeMirror) {
        return ((DeclaredType) typeMirror).getTypeArguments();
    }

    /**
     * Method to check if the {@link TypeMirror} is of primitive type
     *
     * @param type :TypeMirror type
     * @return boolean
     */
    public static boolean isSupportedPrimitive(@NotNull String type) {
        return type.equals(long.class.getName())
                || type.equals(double.class.getName())
                || type.equals(boolean.class.getName())
                || type.equals(float.class.getName())
                || type.equals(int.class.getName())
                || type.equals(char.class.getName())
                || type.equals(short.class.getName())
                || type.equals(byte.class.getName());
    }

    /**
     * Method to check if the {@link TypeMirror} is of {@link ArrayType}
     *
     * @param type :TypeMirror type
     * @return boolean
     */
    public static boolean isNativeArray(@NotNull TypeMirror type) {
        return (type instanceof ArrayType);
    }

    /**
     * Method to check if the {@link TypeMirror} is of {@link Collection} type
     *
     * @param type :TypeMirror type
     * @return boolean
     */
    public static boolean isSupportedCollection(@Nullable TypeMirror type) {
        if (type == null) {
            return false;
        }
        if (isNativeArray(type)) {
            return true;
        }
        String outerClassType = TypeUtils.getOuterClassType(type);
        return isSupportedList(type) || outerClassType.equals(Collection.class.getName());
    }

    /**
     * Method to check if the {@link TypeMirror} is of {@link List} type
     *
     * @param type :TypeMirror type
     * @return boolean
     */
    private static boolean isSupportedList(@Nullable TypeMirror type) {
        if (type == null) {
            return false;
        }
        String outerClassType = TypeUtils.getOuterClassType(type);
        return outerClassType.equals(ArrayList.class.getName()) ||
                outerClassType.equals(List.class.getName());
    }

    /**
     * Method to check if the {@link TypeMirror} is of {@link Object}
     *
     * @param type :TypeMirror type
     * @return boolean
     */
    public static boolean isNativeObject(@Nullable TypeMirror type) {
        if (type == null) {
            return false;
        }
        String outerClassType = TypeUtils.getOuterClassType(type);
        return outerClassType.equals(Object.class.getName());
    }

    /**
     * Method to check if the {@link TypeMirror} is of {@link JsonElement} type
     *
     * @param type :TypeMirror type
     * @return boolean
     */
    public static boolean isJsonElement(@Nullable TypeMirror type) {
        if (type == null) {
            return false;
        }
        String outerClassType = TypeUtils.getOuterClassType(type);
        return outerClassType.equals(JsonElement.class.getName())
                || outerClassType.equals(JsonObject.class.getName())
                || outerClassType.equals(JsonArray.class.getName());
    }

    /**
     * Method to check if the {@link TypeMirror} is of {@link Map} type
     *
     * @param type :TypeMirror type
     * @return boolean
     */
    public static boolean isSupportedMap(@Nullable TypeMirror type) {
        if (type == null) {
            return false;
        }
        String outerClassType = TypeUtils.getOuterClassType(type);
        return outerClassType.equals(Map.class.getName()) ||
                outerClassType.equals(HashMap.class.getName()) ||
                outerClassType.equals(ConcurrentHashMap.class.getName()) ||
                outerClassType.equals("android.util.ArrayMap") ||
                outerClassType.equals("android.support.v4.util.ArrayMap") ||
                outerClassType.equals(LinkedHashMap.class.getName());
    }

    /**
     * Method to check if the type is natively supported such as {@link String} etc
     *
     * @param type String type
     * @return boolean
     */
    public static boolean isSupportedNative(@NotNull String type) {
        return isSupportedPrimitive(type)
                || type.equals(String.class.getName())
                || type.equals(Long.class.getName())
                || type.equals(Integer.class.getName())
                || type.equals(Boolean.class.getName())
                || type.equals(Double.class.getName())
                || type.equals(Float.class.getName())
                || type.equals(Number.class.getName());
    }

    /**
     * Returns the inner {@link TypeMirror} for a given {@link TypeMirror}
     */
    @NotNull
    public static TypeMirror getArrayInnerType(@NotNull TypeMirror type) {
        return (type instanceof ArrayType) ? ((ArrayType) type).getComponentType() : ((DeclaredType) type).getTypeArguments().get(0);
    }
}