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
package com.vimeo.stag.processor.generators;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.KnownTypeAdapters.ArrayTypeAdapter;
import com.vimeo.stag.KnownTypeAdapters.ListTypeAdapter;
import com.vimeo.stag.KnownTypeAdapters.MapTypeAdapter;
import com.vimeo.stag.KnownTypeAdapters.ObjectTypeAdapter;
import com.vimeo.stag.processor.generators.model.AnnotatedClass;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.generators.model.SupportedTypesModel;
import com.vimeo.stag.processor.utils.ElementUtils;
import com.vimeo.stag.processor.utils.FileGenUtils;
import com.vimeo.stag.processor.utils.KnownTypeAdapterUtils;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

public class TypeAdapterGenerator extends AdapterGenerator {

    private static final String TYPE_ADAPTER_FIELD_PREFIX = "mTypeAdapter";
    private static boolean sGsonVariableUsed;
    private static boolean sStagFactoryUsed;
    @NotNull
    private final ClassInfo mInfo;

    public TypeAdapterGenerator(@NotNull ClassInfo info) {
        mInfo = info;
    }

    /**
     * This is used to generate the type token code for the types that are unknown.
     */
    @Nullable
    private static String getTypeTokenCode(@NotNull TypeMirror fieldType,
                                           @NotNull Map<TypeVariable, String> typeVarsMap,
                                           @NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator) {
        String result = null;
        if (!TypeUtils.isConcreteType(fieldType)) {
            if (fieldType.getKind() == TypeKind.TYPEVAR) {
                result = " com.google.gson.reflect.TypeToken.get(" + typeVarsMap.get(fieldType) + ")";
            } else if (fieldType instanceof DeclaredType) {
                /*
                 * If it is of ParameterizedType, {@link com.vimeo.stag.utils.ParameterizedTypeUtil} is used to get the
                 * type token of the parameter type.
                 */
                DeclaredType declaredFieldType = (DeclaredType) fieldType;
                List<? extends TypeMirror> typeMirrors = ((DeclaredType) fieldType).getTypeArguments();
                result = "com.google.gson.reflect.TypeToken.getParameterized(" +
                         declaredFieldType.asElement().toString() + ".class";
                /*
                 * Iterate through all the types from the typeArguments and generate type token code accordingly
                 */
                for (TypeMirror parameterTypeMirror : typeMirrors) {
                    if (TypeUtils.isSupportedNative(parameterTypeMirror.toString())) {
                        result += ", " + parameterTypeMirror.toString() + ".class";
                    } else if (parameterTypeMirror.getKind() == TypeKind.TYPEVAR) {
                        result += ", " + typeVarsMap.get(parameterTypeMirror);
                    } else {
                        result += ",\n" + getTypeTokenCode(parameterTypeMirror, typeVarsMap,
                                                           typeTokenConstantsGenerator) + ".getType()";
                    }
                }
                result += ")";
            }
        } else {
            result = typeTokenConstantsGenerator.addTypeToken(fieldType);
        }

        return result;
    }

    /**
     * This is used to generate the type token code for the types that are known.
     */
    @Nullable
    private String getTypeAdapterCode(@NotNull TypeMirror fieldType, @NotNull TypeSpec.Builder adapterBuilder,
                                      @NotNull MethodSpec.Builder constructorBuilder,
                                      @NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator,
                                      @NotNull Map<TypeVariable, String> typeVarsMap,
                                      @NotNull StagGenerator stagGenerator,
                                      @NotNull AdapterFieldInfo adapterFieldInfo) {
        String result = null;
        if (!TypeUtils.isConcreteType(fieldType)) {
            if (fieldType.getKind() == TypeKind.TYPEVAR) {
                result = typeVarsMap.get(fieldType);
            } else if (fieldType instanceof DeclaredType) {
                DeclaredType declaredFieldType = (DeclaredType) fieldType;
                List<? extends TypeMirror> typeMirrors = ((DeclaredType) fieldType).getTypeArguments();

                if (typeMirrors.isEmpty()) {
                    result = typeTokenConstantsGenerator.addTypeToken(fieldType);
                } else if (TypeUtils.isSupportedMap(fieldType)) {
                    /**
                     * If the fieldType is of {@link Map} type, generate the MapTypeAdapter with its key and valueTypeAdapter
                     */
                    TypeMirror keyTypeMirror = typeMirrors.get(0);
                    TypeMirror valueTypeMirror = typeMirrors.get(1);
                    String keyAdapterAccessor =
                            getAdapterAccessor(keyTypeMirror, adapterBuilder, constructorBuilder,
                                               typeTokenConstantsGenerator, typeVarsMap, stagGenerator,
                                               adapterFieldInfo);
                    String valueAdapterAccessor =
                            getAdapterAccessor(valueTypeMirror, adapterBuilder, constructorBuilder,
                                               typeTokenConstantsGenerator, typeVarsMap, stagGenerator,
                                               adapterFieldInfo);
                    result = "new " + TypeUtils.className(MapTypeAdapter.class) + "<" +
                             keyTypeMirror.toString() + "," + valueTypeMirror.toString() + "," +
                             fieldType.toString() + ">(" + keyAdapterAccessor + " ," + valueAdapterAccessor +
                             ", " + KnownTypeAdapterUtils.getMapInstantiator(fieldType) + ")";
                } else if (TypeUtils.isSupportedCollection(fieldType)) {
                    /**
                     * If the fieldType is of {@link java.util.Collection} type, generate the ListTypeAdapter with its valueTypeAdapter
                     */
                    TypeMirror valueTypeMirror = typeMirrors.get(0);
                    String valueAdapterAccessor =
                            getAdapterAccessor(valueTypeMirror, adapterBuilder, constructorBuilder,
                                               typeTokenConstantsGenerator, typeVarsMap, stagGenerator,
                                               adapterFieldInfo);
                    result = "new " + TypeUtils.className(ListTypeAdapter.class) + "<" +
                             valueTypeMirror.toString() + "," + fieldType.toString() + ">(" +
                             valueAdapterAccessor + ", " +
                             KnownTypeAdapterUtils.getListInstantiator(fieldType) + ")";
                } else {
                    /**
                     * If the fieldType is of Known parameterized type, recursively call the function to generate the type adapter code.
                     */
                    TypeMirror outerClass = declaredFieldType.asElement().asType();
                    sGsonVariableUsed = true;
                    List<? extends TypeMirror> typeArguments = declaredFieldType.getTypeArguments();
                    ExternalAdapterInfo externalAdapterInfo =
                            stagGenerator.getExternalSupportedAdapter(outerClass);

                    String typeAdapterCode = "";
                    for (TypeMirror typeMirror : typeArguments) {
                        typeAdapterCode += ", " +
                                           getAdapterAccessor(typeMirror, adapterBuilder, constructorBuilder,
                                                              typeTokenConstantsGenerator, typeVarsMap,
                                                              stagGenerator, adapterFieldInfo);
                    }

                    String adapterCode;
                    if (null != externalAdapterInfo) {
                        // If the field type is an external model
                        adapterCode = externalAdapterInfo.getInitializer("gson", typeAdapterCode);
                    } else {
                        ClassInfo classInfo = new ClassInfo(outerClass);
                        if (classInfo.equals(mInfo)) {
                            // In this case the adapter will be the same as the one we are generating
                            adapterCode = "this";
                        } else {
                            int idx1 = fieldType.toString().indexOf("<");
                            String argument = idx1 > 0 ? fieldType.toString().substring(idx1) : "";
                            adapterCode = "new " + classInfo.getTypeAdapterQualifiedClassName() + argument +
                                          "(gson, stagFactory" + typeAdapterCode + ")";
                        }
                    }
                    return adapterCode;
                }
            }
        } else {
            result = typeTokenConstantsGenerator.addTypeToken(fieldType);
        }

        return result;
    }

    @NotNull
    private static TypeName getAdapterFieldTypeName(@NotNull TypeMirror type) {
        TypeName typeName = TypeVariableName.get(type);
        return ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeName);
    }

    @NotNull
    private static MethodSpec getReadMethodSpec(@NotNull TypeName typeName,
                                                @NotNull Map<Element, TypeMirror> elements,
                                                @NotNull AdapterFieldInfo adapterFieldInfo) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("read")
                .addParameter(JsonReader.class, "reader")
                .returns(typeName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class);

        builder.addCode("\tif (reader.peek() == com.google.gson.stream.JsonToken.NULL) {\n" +
                        "\t\treader.nextNull();\n" +
                        "\t\treturn null;\n" +
                        "\t}\n" +
                        "\tif (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {\n" +
                        "\t\treader.skipValue();\n" +
                        "\t\treturn null;\n" +
                        "\t}\n" +
                        "\treader.beginObject();\n" +
                        '\n' +
                        '\t' + typeName + " object = new " + typeName +
                        "();\n" +
                        "\twhile (reader.hasNext()) {\n" +
                        "\t\tString name = reader.nextName();\n" +
                        "\t\tcom.google.gson.stream.JsonToken jsonToken = reader.peek();\n" +
                        "\t\tif (jsonToken == com.google.gson.stream.JsonToken.NULL) {\n" +
                        "\t\t\treader.skipValue();\n" +
                        "\t\t\tcontinue;\n" +
                        "\t\t}\n" +
                        "\t\tswitch (name) {\n");

        final List<String> nonNullFields = new ArrayList<>();

        for (Map.Entry<Element, TypeMirror> element : elements.entrySet()) {
            String name = getJsonName(element.getKey());
            final String variableName = element.getKey().getSimpleName().toString();
            final TypeMirror elementValue = element.getValue();

            builder.addCode("\t\t\tcase \"" + name + "\":\n");

            String[] alternateJsonNames = getAlternateJsonNames(element.getKey());
            if (alternateJsonNames != null && alternateJsonNames.length > 0) {
                for (String alternateJsonName : alternateJsonNames) {
                    builder.addCode("\t\t\tcase \"" + alternateJsonName + "\":\n");
                }
            }

            builder.addCode("\t\t\t\tobject." + variableName + " = " +
                            adapterFieldInfo.getAdapterAccessor(elementValue) + ".read(reader);");

            builder.addCode("\n\t\t\t\tbreak;\n");
            runIfAnnotationSupported(element.getKey().getAnnotationMirrors(), new Runnable() {
                @Override
                public void run() {
                    if (!TypeUtils.isSupportedPrimitive(elementValue.toString())) {
                        nonNullFields.add(variableName);
                    }
                }
            });
        }

        builder.addCode("\t\t\tdefault:\n" +
                        "\t\t\t\treader.skipValue();\n" +
                        "\t\t\t\tbreak;\n" +
                        "\t\t}\n" +
                        "\t}\n" +
                        '\n' +
                        "\treader.endObject();\n");

        for (String nonNullField : nonNullFields) {
            builder.addCode("\n\tif (object." + nonNullField + " == null) {");
            builder.addCode("\n\t\tthrow new java.io.IOException(\"" + nonNullField + " cannot be null\");");
            builder.addCode("\n\t}\n\n");
        }

        builder.addCode("\treturn object;\n");

        return builder.build();
    }

    private static void runIfAnnotationSupported(@NotNull List<? extends AnnotationMirror> annotationMirrors,
                                                 @NotNull Runnable runnable) {
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            switch (annotationMirror.toString()) {
                case "@javax.validation.constraints.NotNull":
                case "@edu.umd.cs.findbugs.annotations.NonNull":
                case "@javax.annotation.Nonnull":
                case "@lombok.NonNull":
                case "@org.eclipse.jdt.annotation.NonNull":
                case "@org.jetbrains.annotations.NotNull":
                case "@android.support.annotation.NonNull":
                    runnable.run();
                    break;
            }
        }
    }

    /**
     * Returns the adapter code for the known types.
     */
    private String getAdapterAccessor(@NotNull TypeMirror fieldType, @NotNull TypeSpec.Builder adapterBuilder,
                                      @NotNull MethodSpec.Builder constructorBuilder,
                                      @NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator,
                                      @NotNull Map<TypeVariable, String> typeVarsMap,
                                      @NotNull StagGenerator stagGenerator,
                                      @NotNull AdapterFieldInfo adapterFieldInfo) {

        String knownTypeAdapter = KnownTypeAdapterUtils.getKnownTypeAdapterForType(fieldType);

        if (null != knownTypeAdapter) {
            return knownTypeAdapter;
        } else if (TypeUtils.isConcreteType(fieldType)) {
            /*
             * If the fields are of concrete types, and not parameterized
             */
            String getterField = stagGenerator.getClassAdapterFactoryMethod(fieldType);

            if (null != getterField) {
                sGsonVariableUsed = true;
                sStagFactoryUsed = true;
                /*
                 * If we already have the adapter generated for the fieldType in Stag.Factory class
                 */
                return "mStagFactory.get" + getterField + "(mGson)";
            } else if (TypeUtils.isNativeArray(fieldType)) {
                /*
                 * If the fieldType is of type native arrays such as String[] or int[]
                 */
                TypeMirror arrayInnerType = TypeUtils.getArrayInnerType(fieldType);
                if (TypeUtils.isSupportedPrimitive(arrayInnerType.toString())) {
                    return KnownTypeAdapterUtils.getNativePrimitiveArrayTypeAdapter(fieldType);
                } else {
                    sStagFactoryUsed = true;
                    sGsonVariableUsed = true;
                    ArrayType arrayType = (ArrayType) fieldType;
                    String adapterAccessor =
                            getAdapterAccessor(arrayInnerType, adapterBuilder, constructorBuilder,
                                               typeTokenConstantsGenerator, typeVarsMap, stagGenerator,
                                               adapterFieldInfo);
                    String nativeArrayInstantiator =
                            KnownTypeAdapterUtils.getNativeArrayInstantiator(arrayInnerType);
                    String adapterCode = "new " + TypeUtils.className(ArrayTypeAdapter.class) + "<" +
                                         arrayInnerType.toString() + ">" +
                                         "(" + adapterAccessor + ", " + nativeArrayInstantiator + ")";
                    if (arrayType.getComponentType().getKind() != TypeKind.TYPEVAR &&
                        !adapterCode.contains(TYPE_ADAPTER_FIELD_PREFIX)) {
                        String getterName = stagGenerator.addFieldForKnownType(fieldType, adapterCode.replace(
                                "mStagFactory", "this").replace("mGson", "gson"));
                        return "mStagFactory." + getterName + "(mGson)";
                    } else {
                        return adapterCode;
                    }
                }
            } else if (TypeUtils.isSupportedList(fieldType)) {
                DeclaredType declaredType = (DeclaredType) fieldType;
                /*
                 * If the fieldType is of type List
                 */
                sStagFactoryUsed = true;
                sGsonVariableUsed = true;
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                TypeMirror param = typeArguments.get(0);
                String paramAdapterAccessor = getAdapterAccessor(param, adapterBuilder, constructorBuilder,
                                                                 typeTokenConstantsGenerator, typeVarsMap,
                                                                 stagGenerator, adapterFieldInfo);


                String listInstantiator = KnownTypeAdapterUtils.getListInstantiator(fieldType);
                String adapterCode =
                        "new " + TypeUtils.className(ListTypeAdapter.class) + "<" + param.toString() + "," +
                        fieldType.toString() + ">" +
                        "(" + paramAdapterAccessor + ", " + listInstantiator + ")";
                if (declaredType.getKind() != TypeKind.TYPEVAR &&
                    !adapterCode.contains(TYPE_ADAPTER_FIELD_PREFIX)) {
                    String getterName = stagGenerator.addFieldForKnownType(fieldType, adapterCode.replaceAll(
                            "mStagFactory.", "").replaceAll("mGson", "gson"));
                    return "mStagFactory." + getterName + "(mGson)";
                } else {
                    return adapterCode;
                }
            } else if (TypeUtils.isSupportedMap(fieldType)) {
                DeclaredType declaredType = (DeclaredType) fieldType;
                 /*
                  * If the fieldType is of type Map
                  */
                sGsonVariableUsed = true;
                sStagFactoryUsed = true;
                String mapInstantiator = KnownTypeAdapterUtils.getMapInstantiator(fieldType);
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                String keyAdapterAccessor;
                String valueAdapterAccessor;
                String arguments = "";
                if (typeArguments != null && typeArguments.size() == 2) {
                    TypeMirror keyType = typeArguments.get(0);
                    TypeMirror valueType = typeArguments.get(1);
                    keyAdapterAccessor = getAdapterAccessor(keyType, adapterBuilder, constructorBuilder,
                                                            typeTokenConstantsGenerator, typeVarsMap,
                                                            stagGenerator, adapterFieldInfo);
                    valueAdapterAccessor = getAdapterAccessor(valueType, adapterBuilder, constructorBuilder,
                                                              typeTokenConstantsGenerator, typeVarsMap,
                                                              stagGenerator, adapterFieldInfo);
                    arguments = "<" + keyType.toString() + ", " + valueType.toString() + ", " +
                                fieldType.toString() + ">";
                } else {
                    // If the map does not have any type arguments, use Object as type params in this case
                    String objectTypeAdapter = TypeUtils.className(ObjectTypeAdapter.class);
                    keyAdapterAccessor = "new " + objectTypeAdapter + "(mGson)";
                    valueAdapterAccessor = "new " + objectTypeAdapter + "(mGson)";
                }

                String adapterCode = "new " + TypeUtils.className(MapTypeAdapter.class) + arguments +
                                     "(" + keyAdapterAccessor + ", " + valueAdapterAccessor + ", " +
                                     mapInstantiator + ")";
                if (declaredType.getKind() != TypeKind.TYPEVAR &&
                    !adapterCode.contains(TYPE_ADAPTER_FIELD_PREFIX)) {
                    String getterName = stagGenerator.addFieldForKnownType(fieldType, adapterCode.replaceAll(
                            "mStagFactory.", "").replaceAll("mGson", "gson"));
                    return "mStagFactory." + getterName + "(mGson)";
                } else {
                    return adapterCode;
                }
            } else if (TypeUtils.isNativeObject(fieldType)) {
                /*
                 * If the fieldType is Object, use ObjectTypeAdapter
                 */
                sGsonVariableUsed = true;
                String adapterCode = "new " + TypeUtils.className(ObjectTypeAdapter.class) + "(mGson)";
                String getterName = stagGenerator.addFieldForKnownType(fieldType,
                                                                       adapterCode.replaceAll("mStagFactory.",
                                                                                              "")
                                                                               .replaceAll("mGson", "gson"));
                return "mStagFactory." + getterName + "(mGson)";
            } else if (fieldType instanceof DeclaredType) {
                /*
                 * If the field type is generic and does not belong to above cases.
                 */
                DeclaredType declaredType = (DeclaredType) fieldType;
                int size =
                        declaredType.getTypeArguments() == null ? 0 : declaredType.getTypeArguments().size();
                TypeMirror outerClass = declaredType.asElement().asType();
                if (size != 0 && (stagGenerator.isKnownType(outerClass) ||
                                  (null != stagGenerator.getExternalSupportedAdapter(outerClass)))) {
                    sGsonVariableUsed = true;
                    sStagFactoryUsed = true;
                    ClassInfo outerClassInfo = new ClassInfo(outerClass);
                    int idx1 = fieldType.toString().indexOf("<");
                    String argument = idx1 > 0 ? fieldType.toString().substring(idx1) : "";
                    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                    ExternalAdapterInfo externalAdapterInfo =
                            stagGenerator.getExternalSupportedAdapter(outerClass);
                    String adapterCode =
                            "new " + outerClassInfo.getTypeAdapterQualifiedClassName() + argument + "(gson, ";

                    if (null != externalAdapterInfo) {
                        adapterCode += externalAdapterInfo.getFactoryInitializer();
                    } else {
                        adapterCode += "mStagFactory";
                    }

                    adapterCode += ", ";
                    for (TypeMirror typeMirror : typeArguments) {
                        adapterCode += getAdapterAccessor(typeMirror, adapterBuilder, constructorBuilder,
                                                          typeTokenConstantsGenerator, typeVarsMap,
                                                          stagGenerator, adapterFieldInfo);
                    }
                    adapterCode += ")";
                    if (!adapterCode.contains(TYPE_ADAPTER_FIELD_PREFIX)) {
                        return "mStagFactory." + stagGenerator.addFieldForKnownType(fieldType,
                                                                                    adapterCode.replace(
                                                                                            "mStagFactory",
                                                                                            "this")
                                                                                            .replace("mGson",
                                                                                                     "gson")) +
                               "(mGson)";
                    } else {
                        return adapterCode;
                    }

                } else {
                    return addFieldForUnknownType(fieldType, adapterBuilder, constructorBuilder,
                                                  stagGenerator, adapterFieldInfo);
                }
            } else {
                return addFieldForUnknownType(fieldType, adapterBuilder, constructorBuilder, stagGenerator,
                                              adapterFieldInfo);
            }
        } else {

            /*
             * If the fieldType is parameterized, generate the type adapter in the constructor itself.
             */
            String fieldName = adapterFieldInfo.getFieldName(fieldType);
            if (null == fieldName) {
                fieldName = TYPE_ADAPTER_FIELD_PREFIX + adapterFieldInfo.size();
                adapterFieldInfo.addField(fieldType, fieldName);
                String originalFieldName = FileGenUtils.unescapeEscapedString(fieldName);
                TypeName typeName = getAdapterFieldTypeName(fieldType);
                adapterBuilder.addField(typeName, originalFieldName, Modifier.PRIVATE, Modifier.FINAL);
                String typeAdapterCode = getTypeAdapterCode(fieldType, adapterBuilder, constructorBuilder,
                                                            typeTokenConstantsGenerator, typeVarsMap,
                                                            stagGenerator, adapterFieldInfo);

                if (null != typeAdapterCode) {
                    constructorBuilder.addStatement(
                            fieldName + " = " + getCleanedFieldInitializer(typeAdapterCode));
                }
            }
            return fieldName;
        }
    }

    private static String getCleanedFieldInitializer(String code) {
        return code.replace("mStagFactory", "stagFactory").replace("mGson", "gson");
    }

    private static String addFieldForUnknownType(@NotNull TypeMirror fieldType,
                                                 @NotNull TypeSpec.Builder adapterBuilder,
                                                 @NotNull MethodSpec.Builder constructorBuilder,
                                                 @NotNull StagGenerator stagGenerator,
                                                 @NotNull AdapterFieldInfo adapterFieldInfo) {
        ExternalAdapterInfo externalAdapterInfo = stagGenerator.getExternalSupportedAdapter(fieldType);
        if (null != externalAdapterInfo) {
            //Generate the Type Adapter as a member variable
            String fieldName = adapterFieldInfo.getFieldName(fieldType);
            if (null == fieldName) {
                fieldName = TYPE_ADAPTER_FIELD_PREFIX + adapterFieldInfo.size();
                adapterFieldInfo.addField(fieldType, fieldName);
                String originalFieldName = FileGenUtils.unescapeEscapedString(fieldName);
                TypeName typeName = getAdapterFieldTypeName(fieldType);
                adapterBuilder.addField(typeName, originalFieldName, Modifier.PRIVATE, Modifier.FINAL);
                String statement = fieldName + " = " +
                                   getCleanedFieldInitializer(externalAdapterInfo.getInitializer("gson", ""));
                constructorBuilder.addStatement(statement);
            }

            return fieldName;
        } else {
            String getterField = stagGenerator.addFieldForUnknownType(fieldType);
            sGsonVariableUsed = true;
            sStagFactoryUsed = true;
            return "mStagFactory." + "get" + getterField + "(mGson)";
        }
    }

    /**
     * Returns the adapter code for the unknown types.
     */
    private static String getAdapterForUnknownType(@NotNull TypeMirror fieldType,
                                                   @NotNull TypeSpec.Builder adapterBuilder,
                                                   @NotNull MethodSpec.Builder constructorBuilder, @NotNull
                                                           TypeTokenConstantsGenerator typeTokenConstantsGenerator,
                                                   @NotNull Map<TypeVariable, String> typeVarsMap,
                                                   @NotNull AdapterFieldInfo adapterFieldInfo) {

        String fieldName = adapterFieldInfo.getFieldName(fieldType);
        if (null == fieldName) {
            fieldName = TYPE_ADAPTER_FIELD_PREFIX + adapterFieldInfo.size();
            adapterFieldInfo.addField(fieldType, fieldName);
            String originalFieldName = FileGenUtils.unescapeEscapedString(fieldName);
            TypeName typeName = getAdapterFieldTypeName(fieldType);
            adapterBuilder.addField(typeName, originalFieldName, Modifier.PRIVATE, Modifier.FINAL);
            constructorBuilder.addStatement(
                    fieldName + " = (TypeAdapter<" + fieldType + ">) gson.getAdapter(" +
                    getTypeTokenCode(fieldType, typeVarsMap, typeTokenConstantsGenerator) + ")");
        }
        return fieldName;
    }


    @NotNull
    private AdapterFieldInfo addAdapterFields(@Nullable StagGenerator.GenericClassInfo genericClassInfo,
                                              @NotNull TypeSpec.Builder adapterBuilder,
                                              @NotNull MethodSpec.Builder constructorBuilder,
                                              @NotNull Map<Element, TypeMirror> memberVariables, @NotNull
                                                      TypeTokenConstantsGenerator typeTokenConstantsGenerator,
                                              @NotNull Map<TypeVariable, String> typeVarsMap,
                                              @NotNull StagGenerator stagGenerator) {

        AdapterFieldInfo result = new AdapterFieldInfo(memberVariables.size());
        boolean hasUnknownGenericField =
                genericClassInfo != null && genericClassInfo.mHasUnknownVarTypeFields;
        for (Map.Entry<Element, TypeMirror> entry : memberVariables.entrySet()) {
            TypeMirror fieldType = entry.getValue();
            JsonAdapter annotation = entry.getKey().getAnnotation(JsonAdapter.class);
            String adapterAccessor;
            if(null != annotation) {
                TypeMirror jsonAdapterType = null;
                Element jsonAdapterTypeElement = null;
                // Using this trick to get the class type
                // https://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
                try
                {
                    annotation.value();
                }
                catch( MirroredTypeException mte )
                {
                    jsonAdapterType = mte.getTypeMirror();
                    jsonAdapterTypeElement = TypeUtils.getElementFromTypeMirror(jsonAdapterType);
                    TypeUtils.JsonAdapterType jsonAdapterType1 = TypeUtils.getJsonAdapterType(jsonAdapterType);
                    for(Element element : jsonAdapterTypeElement.getEnclosedElements()) {
                        if (element instanceof ExecutableElement) {
                            ExecutableElement executableElement =
                                    ((ExecutableElement) element);
                            Name name = executableElement.getSimpleName();
                            if (name.contentEquals("<init>")) {
                                System.out.println("Yasir : " + fieldType.toString() + " " + executableElement.toString() + " jsonAdapterType1 : " + jsonAdapterType1.name() );
                            }
                        }
                    }
                }

            }

            if (hasUnknownGenericField && TypeUtils.containsTypeVarParams(fieldType)) {
                adapterAccessor = getAdapterForUnknownType(fieldType, adapterBuilder, constructorBuilder,
                                                           typeTokenConstantsGenerator, typeVarsMap, result);
            } else {
                adapterAccessor = getAdapterAccessor(fieldType, adapterBuilder, constructorBuilder,
                                                     typeTokenConstantsGenerator, typeVarsMap, stagGenerator,
                                                     result);

                if (null != adapterAccessor && adapterAccessor.startsWith("new ")) {
                    //Add this to a member variable
                    String fieldName = TYPE_ADAPTER_FIELD_PREFIX + result.size();
                    result.addField(fieldType, fieldName);
                    String originalFieldName = FileGenUtils.unescapeEscapedString(fieldName);
                    TypeName typeName = getAdapterFieldTypeName(fieldType);
                    adapterBuilder.addField(typeName, originalFieldName, Modifier.PRIVATE, Modifier.FINAL);
                    String statement = fieldName + " = " + getCleanedFieldInitializer(adapterAccessor);
                    constructorBuilder.addStatement(statement);
                    adapterAccessor = fieldName;
                }
            }

            if (null != adapterAccessor) {
                result.addTypeToAdapterAccessor(fieldType, adapterAccessor);
            }
        }
        return result;
    }

    @NotNull
    private static MethodSpec getWriteMethodSpec(@NotNull TypeName typeName,
                                                 @NotNull Map<Element, TypeMirror> memberVariables,
                                                 @NotNull AdapterFieldInfo adapterFieldInfo) {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("write")
                .addParameter(JsonWriter.class, "writer")
                .addParameter(typeName, "object")
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class);

        builder.addStatement("writer.beginObject()");
        builder.beginControlFlow("if (object == null)");
        builder.addStatement("writer.endObject()");
        builder.addStatement("return");
        builder.endControlFlow();

        for (Map.Entry<Element, TypeMirror> element : memberVariables.entrySet()) {
            String name = getJsonName(element.getKey());
            final String variableName = element.getKey().getSimpleName().toString();
            String variableType = element.getValue().toString();

            boolean isPrimitive = TypeUtils.isSupportedPrimitive(variableType);

            builder.addCode("\n");
            if (!isPrimitive) {
                builder.beginControlFlow("if (object." + variableName + " != null) ");
            }

            builder.addStatement("writer.name(\"" + name + "\")");
            if (!isPrimitive) {
                builder.addStatement(
                        adapterFieldInfo.getAdapterAccessor(element.getValue()) + ".write(writer, object." +
                        variableName + ")");
                /*
                * If the element is annotated with NonNull annotation, throw {@link IOException} if it is null.
                */
                runIfAnnotationSupported(element.getKey().getAnnotationMirrors(), new Runnable() {
                    @Override
                    public void run() {
                        builder.endControlFlow();
                        builder.beginControlFlow("else if (object." + variableName + " == null)");
                        builder.addStatement("throw new java.io.IOException(\"" + variableName +
                                             " cannot be null\")");
                    }
                });

                builder.endControlFlow();
            } else {
                builder.addStatement("writer.value(object." + variableName + ")");
            }
        }

        builder.addCode("\n");
        builder.addStatement("writer.endObject()");
        return builder.build();
    }

    /**
     * Generates the TypeSpec for the TypeAdapter
     * that this class generates.
     *
     * @return a valid TypeSpec that can be written
     * to a file or added to another class.
     */
    @Override
    @NotNull
    public TypeSpec getTypeAdapterSpec(@NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator,
                                       @NotNull StagGenerator stagGenerator) {
        sGsonVariableUsed = false;
        sStagFactoryUsed = false;
        TypeMirror typeMirror = mInfo.getType();
        TypeName typeVariableName = TypeVariableName.get(typeMirror);

        List<? extends TypeMirror> typeArguments = mInfo.getTypeArguments();

        TypeVariableName stagFactoryTypeName = stagGenerator.getGeneratedClassName();
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                                       .addMember("value", "\"unchecked\"")
                                       .build())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson")
                .addParameter(stagFactoryTypeName, "stagFactory");

        String className = FileGenUtils.unescapeEscapedString(mInfo.getTypeAdapterClassName());
        TypeSpec.Builder adapterBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeVariableName));

        Map<TypeVariable, String> typeVarsMap = new HashMap<>();

        int idx = 0;
        StagGenerator.GenericClassInfo genericClassInfo = null;
        if (null != typeArguments) {
            genericClassInfo = stagGenerator.getGenericClassInfo(mInfo.getType());
            for (TypeMirror innerTypeMirror : typeArguments) {
                if (innerTypeMirror.getKind() == TypeKind.TYPEVAR) {
                    TypeVariable typeVariable = (TypeVariable) innerTypeMirror;
                    String simpleName = typeVariable.asElement().getSimpleName().toString();
                    adapterBuilder.addTypeVariable(TypeVariableName.get(simpleName, TypeVariableName.get(
                            typeVariable.getUpperBound())));
                    String paramName;
                    if (genericClassInfo != null && genericClassInfo.mHasUnknownVarTypeFields) {
                        //If the classInfo has unknown types, pass type... as param in constructor.
                        paramName = "type[" + String.valueOf(idx) + "]";
                    } else {
                        ParameterizedTypeName parameterizedTypeName =
                                ParameterizedTypeName.get(ClassName.get(TypeAdapter.class),
                                                          TypeVariableName.get(innerTypeMirror.toString()));
                        paramName = "typeAdapter" + idx;
                        constructorBuilder.addParameter(parameterizedTypeName, paramName);
                    }

                    typeVarsMap.put(typeVariable, paramName);
                    idx++;
                }
            }

            if (idx > 0 && genericClassInfo != null && genericClassInfo.mHasUnknownVarTypeFields) {
                constructorBuilder.addParameter(Type[].class, "type");
                constructorBuilder.varargs(true);
            }
        }

        AnnotatedClass annotatedClass = SupportedTypesModel.getInstance().getSupportedType(typeMirror);
        if (null == annotatedClass) {
            throw new IllegalStateException("The AnnotatedClass class can't be null in TypeAdapterGenerator : " + typeMirror.toString());
        }
        Map<Element, TypeMirror> memberVariables = annotatedClass.getMemberVariables();

        AdapterFieldInfo adapterFieldInfo =
                addAdapterFields(genericClassInfo, adapterBuilder, constructorBuilder, memberVariables,
                                 typeTokenConstantsGenerator, typeVarsMap, stagGenerator);

        MethodSpec writeMethod = getWriteMethodSpec(typeVariableName, memberVariables, adapterFieldInfo);
        MethodSpec readMethod = getReadMethodSpec(typeVariableName, memberVariables, adapterFieldInfo);

        if (sGsonVariableUsed) {
            adapterBuilder.addField(Gson.class, "mGson", Modifier.FINAL, Modifier.PRIVATE);
            constructorBuilder.addStatement("this.mGson = gson");
        }

        if (sStagFactoryUsed) {
            adapterBuilder.addField(stagFactoryTypeName, "mStagFactory", Modifier.FINAL, Modifier.PRIVATE);
            constructorBuilder.addStatement("this.mStagFactory = stagFactory");
        }

        adapterBuilder.addMethod(constructorBuilder.build());
        adapterBuilder.addMethod(writeMethod);
        adapterBuilder.addMethod(readMethod);

        return adapterBuilder.build();
    }

    private static class AdapterFieldInfo {

        //Type.toString -> Accessor Map
        @NotNull
        private final Map<String, String> mAdapterAccessor;

        //Type.toString -> Accessor Map
        @NotNull
        private final Map<String, String> mAdapterFields;

        AdapterFieldInfo(int capacity) {
            mAdapterFields = new HashMap<>(capacity);
            mAdapterAccessor = new HashMap<>(capacity);
        }

        String getAdapterAccessor(@NotNull TypeMirror typeMirror) {
            return mAdapterAccessor.get(typeMirror.toString());
        }

        String getFieldName(@NotNull TypeMirror fieldType) {
            return mAdapterFields.get(fieldType.toString());
        }

        int size() {
            return mAdapterFields.size();
        }

        void addField(@NotNull TypeMirror fieldType, @NotNull String fieldName) {
            mAdapterFields.put(fieldType.toString(), fieldName);
        }

        void addTypeToAdapterAccessor(@NotNull TypeMirror typeMirror, String accessorCode) {
            mAdapterAccessor.put(typeMirror.toString(), accessorCode);
        }
    }
}