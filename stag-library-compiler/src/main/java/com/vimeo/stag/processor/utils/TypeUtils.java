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

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.vimeo.stag.processor.generators.model.accessor.FieldAccessor;

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
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;

public final class TypeUtils {

    private static final String TAG = TypeUtils.class.getSimpleName();

    @NotNull private static final HashMap<String, String> PRIMITIVE_TO_OBJECT_MAP = new HashMap<>();

    @Nullable private static Types sTypeUtils;


    static {
        PRIMITIVE_TO_OBJECT_MAP.put(boolean.class.getName(), Boolean.class.getName());
        PRIMITIVE_TO_OBJECT_MAP.put(int.class.getName(), Integer.class.getName());
        PRIMITIVE_TO_OBJECT_MAP.put(short.class.getName(), Short.class.getName());
        PRIMITIVE_TO_OBJECT_MAP.put(double.class.getName(), Double.class.getName());
        PRIMITIVE_TO_OBJECT_MAP.put(long.class.getName(), Long.class.getName());
        PRIMITIVE_TO_OBJECT_MAP.put(float.class.getName(), Float.class.getName());
        PRIMITIVE_TO_OBJECT_MAP.put(char.class.getName(), Character.class.getName());
        PRIMITIVE_TO_OBJECT_MAP.put(byte.class.getName(), Byte.class.getName());
    }

    private TypeUtils() {
        throw new UnsupportedOperationException("This class is not instantiable");
    }

    public static void initialize(@NotNull Types typeUtils) {
        sTypeUtils = typeUtils;
    }

    @NotNull
    private static Types getUtils() {
        Preconditions.checkNotNull(sTypeUtils);
        return sTypeUtils;
    }

    /**
     * Creates the full class name including package
     * name for the given class. Anonymous classes
     * and classes with the '$' character in their names
     * are not supported.
     *
     * @param clazz the class to get the name of.
     * @return the class's name, without any dollar
     * signs for inner classes and with periods instead.
     */
    @NotNull
    public static String className(@NotNull Class clazz) {
        return clazz.getName().replace('$', '.');
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
    public static String getSimpleOuterClassType(@NotNull TypeMirror type) {
        if (type instanceof DeclaredType) {
            return ((DeclaredType) type).asElement().getSimpleName().toString();
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
     * TypeMirrors should not be compared directly, but should use
     * {@link Types} in order to compare them.
     *
     * @param typeMirror1 the first type to compare.
     * @param typeMirror2 the second type to compare.
     * @return true if they are equal, false otherwise.
     */
    public static boolean areEqual(@Nullable TypeMirror typeMirror1, @Nullable TypeMirror typeMirror2) {
        if (typeMirror1 == null && typeMirror2 != null) {
            return false;
        } else if (typeMirror1 != null && typeMirror2 == null) {
            return false;
        } else if (typeMirror1 == typeMirror2) {
            return true;
        }
        return getUtils().isSameType(typeMirror1, typeMirror2);
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
     * Determines whether or not the Element is a abstract type.
     *
     * @param typeMirror the element to check.
     * @return true if the element is abstract and
     * contains no generic type arguments, false otherwise.
     */
    public static boolean isAbstract(@Nullable TypeMirror typeMirror) {
        return (typeMirror instanceof DeclaredType) && isAbstract(((DeclaredType) typeMirror).asElement());
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
    public static boolean isParameterizedType(@Nullable TypeElement element) {
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
        if (isPrimitive(typeMirror, getUtils())) {
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

    /**
     * Determines where the the type mirrors contains type var params or not
     *
     * @param typeMirror the element to check.
     * @return true if it contains type variables
     */
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
     * the inherited type is {@link Object} or {@link Enum},
     * then this method will return null.
     *
     * @param element the element to get the inherited type.
     * @return the inherited type, or null if the element
     * inherits from Object or Enum.
     */
    @Nullable
    public static TypeMirror getInheritedType(@Nullable TypeElement element) {
        TypeMirror typeMirror = element != null ? element.getSuperclass() : null;
        String className = typeMirror != null ? getClassNameFromTypeMirror(typeMirror) : null;
        if (!Object.class.getName().equals(className) && !Enum.class.getName().equals(className)) {
            return typeMirror;
        }
        return null;
    }

    /**
     * Determines whether the element is of the enum type or not.
     *
     * @param element the element to check.
     * @return true if the element inherits from an enum, false otherwise.
     */
    public static boolean isEnum(@Nullable TypeElement element) {
        TypeMirror typeMirror = element != null ? element.getSuperclass() : null;
        String className = typeMirror != null ? getClassNameFromTypeMirror(typeMirror) : null;

        return Enum.class.getName().equals(className);
    }

    /**
     * Retrieves a Map of the inherited concrete member variables of an Element. This takes all the
     * member variables that were inherited from the generic parent class and evaluates what their concrete
     * type will be based on the concrete inherited type. For instance, take the following code example:
     * <pre><code>
     * {@literal Factory<T>} {
     *
     *  {@literal @UseStag}
     *   public T data;
     *
     * }
     *
     * VideoFactory extends {@literal Factory<Video>}{
     *
     *   // other variables in here
     *
     * }
     * </code></pre>
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
     * @return returns a LinkedHashMap of the member variables mapped to their concrete types for the concrete
     * inherited class. (to maintain the ordering)
     */
    @NotNull
    public static LinkedHashMap<FieldAccessor, TypeMirror> getConcreteMembers(@NotNull TypeMirror concreteInherited,
                                                                              @NotNull TypeElement genericInherited,
                                                                              @NotNull Map<FieldAccessor, TypeMirror> members) {

        DebugLog.log(TAG, "Inherited concrete type: " + concreteInherited.toString());
        DebugLog.log(TAG, "Inherited generic type: " + genericInherited.asType().toString());
        List<? extends TypeMirror> concreteTypes = getParameterizedTypes(concreteInherited);
        List<? extends TypeMirror> inheritedTypes = getParameterizedTypes(genericInherited);

        LinkedHashMap<FieldAccessor, TypeMirror> map = new LinkedHashMap<>();

        for (Entry<FieldAccessor, TypeMirror> member : members.entrySet()) {

            DebugLog.log(TAG, "\t\tEvaluating member - " + member.getValue().toString());

            if (isConcreteType(member.getValue())) {

                DebugLog.log(TAG, "\t\t\tConcrete Type: " + member.getValue().toString());
                map.put(member.getKey(), member.getValue());

            } else {

                if (isParameterizedType(member.getValue())) {

                    // HashMap<String, T> ...
                    TypeMirror resolvedType = resolveTypeVars(member.getValue(), inheritedTypes, concreteTypes);
                    map.put(member.getKey(), resolvedType);

                    DebugLog.log(TAG, "\t\t\tGeneric Parameterized Type - " + member.getValue().toString() +
                        " resolved to - " + resolvedType.toString());
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

    /**
     * Gets the primitive type mirror type.
     *
     * @param typeKind the type kind to get the primitive for.
     * @return the primitive type, or null if the type is not primitive.
     */
    @Nullable
    public static TypeMirror getPrimitive(@NotNull TypeKind typeKind) {
        try {
            return getUtils().getPrimitiveType(typeKind);
        } catch (Exception ignored) {
            return null;
        }
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
    private static TypeMirror resolveTypeVars(@NotNull TypeMirror element,
                                              @NotNull final List<? extends TypeMirror> inheritedTypes,
                                              @NotNull final List<? extends TypeMirror> concreteTypes) {
        if (isConcreteType(element)) {
            return element;
        }

        if (element.getKind() == TypeKind.TYPEVAR) {
            int index = inheritedTypes.indexOf(element);
            return concreteTypes.get(index);
        }

        Types types = getUtils();
        List<? extends TypeMirror> typeMirrors = ((DeclaredType) element).getTypeArguments();
        TypeElement typeElement = (TypeElement) types.asElement(element);
        List<TypeMirror> concreteGenericTypes = new ArrayList<>(typeMirrors.size());
        for (TypeMirror type : typeMirrors) {
            concreteGenericTypes.add(resolveTypeVars(type, inheritedTypes, concreteTypes));
        }
        TypeMirror[] concreteTypeArray =
            concreteGenericTypes.toArray(new TypeMirror[concreteGenericTypes.size()]);
        return types.getDeclaredType(typeElement, concreteTypeArray);
    }

    @NotNull
    private static List<? extends TypeMirror> getParameterizedTypes(@NotNull TypeElement element) {
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
        return PRIMITIVE_TO_OBJECT_MAP.containsKey(type);
    }

    /**
     * Method to check if the {@link TypeMirror} is of primitive type
     *
     * @param type :TypeMirror type
     * @return String
     */
    public static String getObjectForPrimitive(@NotNull String type) {
        return PRIMITIVE_TO_OBJECT_MAP.get(type);
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
        return type != null && (isNativeArray(type) || isSupportedList(type));
    }

    /**
     * Method to check if the {@link TypeMirror} is of {@link List} type
     *
     * @param type :TypeMirror type
     * @return boolean
     */
    public static boolean isSupportedList(@Nullable TypeMirror type) {
        if (type == null) {
            return false;
        }
        String outerClassType = TypeUtils.getOuterClassType(type);
        return outerClassType.equals(ArrayList.class.getName()) ||
            outerClassType.equals(List.class.getName()) ||
            outerClassType.equals(Collection.class.getName());
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
        return isSupportedPrimitive(type) || type.equals(String.class.getName()) ||
            type.equals(Long.class.getName()) || type.equals(Integer.class.getName()) ||
            type.equals(Boolean.class.getName()) || type.equals(Double.class.getName()) ||
            type.equals(Float.class.getName()) || type.equals(Number.class.getName());
    }

    /**
     * Returns the inner {@link TypeMirror} for a given {@link TypeMirror}
     */
    @NotNull
    public static TypeMirror getArrayInnerType(@NotNull TypeMirror type) {
        return (type instanceof ArrayType) ? ((ArrayType) type).getComponentType() : ((DeclaredType) type).getTypeArguments()
            .get(0);
    }

    @NotNull
    public static String getClassNameFromTypeMirror(@NotNull TypeMirror typeMirror) {
        String classAndPackage = typeMirror.toString();

        // This is done to avoid the generic template from being included in the file name
        // to be generated (since it will be an invalid file name)
        int idx = classAndPackage.indexOf("<");
        if (idx > 0) {
            classAndPackage = classAndPackage.substring(0, idx);
        }

        return classAndPackage;
    }

    /**
     * Convert the provided {@link TypeMirror} into an {@link Element} instance.  This method
     * call assumes that the provided {@link TypeMirror} is constrained to only known supported
     * types.  As a result it will guarantee non-null result values.
     *
     * @param typeMirror type mirror to convert
     * @return TypeElement representation of the type mirror
     */
    @NotNull
    public static TypeElement safeTypeMirrorToTypeElement(@NotNull TypeMirror typeMirror) {
        TypeElement element = unsafeTypeMirrorToTypeElement(typeMirror);
        // unsafeTypeMirrorToTypeElement may return null but not in the scenarios we are specifically using it for
        if (element == null) {
            throw new IllegalStateException("Supported type could not be converted into an Element");
        }
        return element;
    }

    /**
     * Convert the provided {@link TypeMirror} into an {@link Element} instance.
     *
     * @param typeMirror TypeMirror to convert
     * @return TypeElement representation of the TypeMirror
     */
    @Nullable
    public static TypeElement unsafeTypeMirrorToTypeElement(@NotNull TypeMirror typeMirror) {
        return (TypeElement) getUtils().asElement(typeMirror);
    }


    public enum JsonAdapterType {
        NONE,
        TYPE_ADAPTER,
        TYPE_ADAPTER_FACTORY,
        JSON_SERIALIZER,
        JSON_DESERIALIZER,
        JSON_SERIALIZER_DESERIALIZER

    }

    /**
     * Return the type of JsonAdapter {@link TypeMirror}
     *
     * @param type :TypeMirror type
     * @return {@link JsonAdapterType}
     */
    @NotNull
    public static JsonAdapterType getJsonAdapterType(@NotNull TypeMirror type) {
        Types types = getUtils();
        if (types.isSubtype(type, getDeclaredTypeForParameterizedClass(TypeAdapter.class.getName()))) {
            return JsonAdapterType.TYPE_ADAPTER;
        } else if (types.isAssignable(type, ElementUtils.getTypeFromQualifiedName(TypeAdapterFactory.class.getName()))) {
            return JsonAdapterType.TYPE_ADAPTER_FACTORY;
        } else {
            boolean isDeserializer = types.isSubtype(type, getDeclaredTypeForParameterizedClass(JsonDeserializer.class.getName()));
            boolean isSerializer = types.isSubtype(type, getDeclaredTypeForParameterizedClass(JsonSerializer.class.getName()));
            if (isSerializer && isDeserializer) {
                return JsonAdapterType.JSON_SERIALIZER_DESERIALIZER;
            } else if (isSerializer) {
                return JsonAdapterType.JSON_SERIALIZER;
            } else if (isDeserializer) {
                return JsonAdapterType.JSON_DESERIALIZER;
            } else {
                return JsonAdapterType.NONE;
            }
        }
    }

    public static boolean isAssignable(TypeMirror t1, TypeMirror t2) {
        return getUtils().isAssignable(t1, t2);
    }

    @NotNull
    public static DeclaredType getDeclaredTypeForParameterizedClass(@NotNull String className) {
        Types types = getUtils();
        WildcardType wildcardType = types.getWildcardType(null, null);
        TypeMirror[] typex = {wildcardType};
        return types.getDeclaredType(ElementUtils.getTypeElementFromQualifiedName(className), typex);
    }


    @NotNull
    public static DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
        Types types = getUtils();
        return types.getDeclaredType(typeElem, typeArgs);
    }

    public static boolean isWildcardType(@Nullable TypeMirror typeMirror) {
        return typeMirror instanceof WildcardType;
    }
}
