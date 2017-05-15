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
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.KnownTypeAdapters.ArrayTypeAdapter;
import com.vimeo.stag.KnownTypeAdapters.ListTypeAdapter;
import com.vimeo.stag.KnownTypeAdapters.MapTypeAdapter;
import com.vimeo.stag.KnownTypeAdapters.ObjectTypeAdapter;
import com.vimeo.stag.processor.generators.StagGenerator.GenericClassInfo;
import com.vimeo.stag.processor.generators.model.AnnotatedClass;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.generators.model.SupportedTypesModel;
import com.vimeo.stag.processor.utils.ElementUtils;
import com.vimeo.stag.processor.utils.FileGenUtils;
import com.vimeo.stag.processor.utils.KnownTypeAdapterUtils;
import com.vimeo.stag.processor.utils.Preconditions;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
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

    @NotNull private final ClassInfo mInfo;
    @NotNull private final SupportedTypesModel mSupportedTypesModel;

    public TypeAdapterGenerator(@NotNull SupportedTypesModel supportedTypesModel, @NotNull ClassInfo info) {
        mSupportedTypesModel = supportedTypesModel;
        mInfo = info;
    }

    /**
     * This is used to generate the type token code for the types that are unknown.
     */
    @Nullable
    private static String getTypeTokenCodeForGenericType(@NotNull TypeMirror fieldType,
                                                         @NotNull Map<TypeMirror, String> typeVarsMap) {

        // This method should only be called if the type is a generic type
        Preconditions.checkTrue(!TypeUtils.isConcreteType(fieldType) && TypeUtils.containsTypeVarParams(fieldType));

        String result = null;
        if (fieldType.getKind() == TypeKind.TYPEVAR) {
            result = "com.google.gson.reflect.TypeToken.get(" + typeVarsMap.get(fieldType) + ")";
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
                    result += ",\n" + getTypeTokenCodeForGenericType(parameterTypeMirror, typeVarsMap) + ".getType()";
                }
            }
            result += ")";
        }

        return result;
    }

    /**
     * This is used to generate the type token code for the types that are known.
     */
    @Nullable
    private String getTypeAdapterCodeForGenericTypes(@NotNull TypeMirror fieldType, @NotNull Builder adapterBuilder,
                                                     @NotNull MethodSpec.Builder constructorBuilder,
                                                     @NotNull Map<TypeMirror, String> typeVarsMap,
                                                     @NotNull StagGenerator stagGenerator,
                                                     @NotNull AdapterFieldInfo adapterFieldInfo) {

        // This method should only be called for generic types
        Preconditions.checkTrue(!TypeUtils.isConcreteType(fieldType));

        String result = null;
        if (fieldType.getKind() == TypeKind.TYPEVAR) {
            result = typeVarsMap.get(fieldType);
        } else if (fieldType instanceof DeclaredType) {
            DeclaredType declaredFieldType = (DeclaredType) fieldType;
            List<? extends TypeMirror> typeMirrors = ((DeclaredType) fieldType).getTypeArguments();

            // List must not be empty because TypeUtils.isConcreteType (see outer condition) returns true if it is.
            Preconditions.checkNotEmpty(typeMirrors);

            if (TypeUtils.isSupportedMap(fieldType)) {
                    /*
                      If the fieldType is of {@link Map} type, generate the MapTypeAdapter with its key and valueTypeAdapter
                     */
                TypeMirror keyTypeMirror = typeMirrors.get(0);
                TypeMirror valueTypeMirror = typeMirrors.get(1);
                String keyAdapterAccessor =
                        getAdapterAccessor(keyTypeMirror, adapterBuilder, constructorBuilder,
                                           typeVarsMap, stagGenerator,
                                           adapterFieldInfo);
                String valueAdapterAccessor =
                        getAdapterAccessor(valueTypeMirror, adapterBuilder, constructorBuilder,
                                           typeVarsMap, stagGenerator,
                                           adapterFieldInfo);
                result = "new " + TypeUtils.className(MapTypeAdapter.class) + "<" +
                         keyTypeMirror.toString() + "," + valueTypeMirror.toString() + "," +
                         fieldType.toString() + ">(" + keyAdapterAccessor + " ," + valueAdapterAccessor +
                         ", " + KnownTypeAdapterUtils.getMapInstantiator(fieldType) + ")";
            } else if (TypeUtils.isSupportedCollection(fieldType)) {
                    /*
                      If the fieldType is of {@link java.util.Collection} type, generate the ListTypeAdapter with its valueTypeAdapter
                     */
                TypeMirror valueTypeMirror = typeMirrors.get(0);
                String valueAdapterAccessor =
                        getAdapterAccessor(valueTypeMirror, adapterBuilder, constructorBuilder,
                                           typeVarsMap, stagGenerator,
                                           adapterFieldInfo);
                result = "new " + TypeUtils.className(ListTypeAdapter.class) + "<" +
                         valueTypeMirror.toString() + "," + fieldType.toString() + ">(" +
                         valueAdapterAccessor + ", " +
                         KnownTypeAdapterUtils.getListInstantiator(fieldType) + ")";
            } else {
                    /*
                      If the fieldType is of Known parameterized type, recursively call the function to generate the type adapter code.
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
                                                          typeVarsMap,
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

        return result;
    }

    @NotNull
    private static TypeName getAdapterFieldTypeName(@NotNull TypeMirror type) {
        TypeName typeName = TypeVariableName.get(type);
        return ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeName);
    }

    @NotNull
    private static MethodSpec getReadMethodSpec(@NotNull TypeName typeName,
                                                @NotNull Map<VariableElement, TypeMirror> elements,
                                                @NotNull AdapterFieldInfo adapterFieldInfo) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("read")
                .addParameter(JsonReader.class, "reader")
                .returns(typeName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class);

        builder.addCode("\tcom.google.gson.stream.JsonToken peek = reader.peek();\n");
        builder.addCode("\tif (com.google.gson.stream.JsonToken.NULL == peek) {\n" +
                        "\t\treader.nextNull();\n" +
                        "\t\treturn null;\n" +
                        "\t}\n" +
                        "\tif (com.google.gson.stream.JsonToken.BEGIN_OBJECT != peek) {\n" +
                        "\t\treader.skipValue();\n" +
                        "\t\treturn null;\n" +
                        "\t}\n" +
                        "\treader.beginObject();\n" +
                        '\n' +
                        '\t' + typeName + " object = new " + typeName +
                        "();\n" +
                        "\twhile (reader.hasNext()) {\n" +
                        "\t\tString name = reader.nextName();\n" +
                        "\t\tswitch (name) {\n");

        final List<String> nonNullFields = new ArrayList<>();

        for (Map.Entry<VariableElement, TypeMirror> element : elements.entrySet()) {
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

            String variableType = element.getValue().toString();
            boolean isPrimitive = TypeUtils.isSupportedPrimitive(variableType);

            if (isPrimitive) {
                builder.addCode("\t\t\t\tobject." + variableName + " = " +
                                adapterFieldInfo.getAdapterAccessor(elementValue, name) + ".read(reader, object." + variableName + ");");

            } else {
                builder.addCode("\t\t\t\tobject." + variableName + " = " +
                                adapterFieldInfo.getAdapterAccessor(elementValue, name) + ".read(reader);");
            }


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

    private static String getFieldAccessorForKnownJsonAdapterType(@NotNull ExecutableElement adapterType,
                                                                  @NotNull TypeSpec.Builder adapterBuilder,
                                                                  @NotNull MethodSpec.Builder constructorBuilder,
                                                                  @NotNull TypeMirror fieldType,
                                                                  @NotNull TypeUtils.JsonAdapterType jsonAdapterType,
                                                                  @NotNull AdapterFieldInfo adapterFieldInfo,
                                                                  boolean isNullSafe,
                                                                  @NotNull String keyFieldName) {
        String fieldAdapterAccessor = "new " + FileGenUtils.escapeStringForCodeBlock(adapterType.getEnclosingElement().toString());
        if (jsonAdapterType == TypeUtils.JsonAdapterType.TYPE_ADAPTER) {
            ArrayList<String> constructorParameters = new ArrayList<>();
            if (!adapterType.getParameters().isEmpty()) {
                for (VariableElement parameter : adapterType.getParameters()) {
                    if (parameter.asType().toString().equals(TypeUtils.className(Gson.class))) {
                        constructorParameters.add("gson");
                    } else if (TypeUtils.isAssignable(parameter.asType(), ElementUtils.getTypeFromQualifiedName(TypeAdapterFactory.class.getName()))) {
                        constructorParameters.add("new " + parameter.asType() + "()");
                    } else {
                        throw new IllegalStateException("Not supported " + parameter.asType() + "parameter for @JsonAdapter value");
                    }
                }
            }


            String constructorParameterStr = "(";
            for (int i = 0; i < constructorParameters.size(); i++) {
                constructorParameterStr += constructorParameters.get(i);
                if (i != constructorParameters.size() - 1) {
                    constructorParameterStr += ",";
                }
            }
            constructorParameterStr += ")";
            fieldAdapterAccessor += constructorParameterStr;
        } else if (jsonAdapterType == TypeUtils.JsonAdapterType.TYPE_ADAPTER_FACTORY) {
            TypeName typeTokenField = ParameterizedTypeName.get(ClassName.get(TypeToken.class), TypeVariableName.get(fieldType));
            fieldAdapterAccessor += "().create(gson, new " + typeTokenField + "(){})";
        } else if (jsonAdapterType == TypeUtils.JsonAdapterType.JSON_SERIALIZER
                   || jsonAdapterType == TypeUtils.JsonAdapterType.JSON_DESERIALIZER
                   || jsonAdapterType == TypeUtils.JsonAdapterType.JSON_SERIALIZER_DESERIALIZER) {
            String serializer = null, deserializer = null;

            if (jsonAdapterType == TypeUtils.JsonAdapterType.JSON_SERIALIZER_DESERIALIZER) {
                String varName = keyFieldName + "SerializerDeserializer";
                String initializer = adapterType.getEnclosingElement().toString() + " " + varName + " = " +
                                     "new " + adapterType;
                constructorBuilder.addStatement(initializer);
                serializer = deserializer = varName;
            } else if (jsonAdapterType == TypeUtils.JsonAdapterType.JSON_SERIALIZER) {
                serializer = "new " + adapterType;
            } else if (jsonAdapterType == TypeUtils.JsonAdapterType.JSON_DESERIALIZER) {
                deserializer = "new " + adapterType;
            }
            TypeName typeTokenField = ParameterizedTypeName.get(ClassName.get(TypeToken.class), TypeVariableName.get(fieldType));
            fieldAdapterAccessor = "new " + TypeVariableName.get(TreeTypeAdapter.class) + "(" + serializer + ", " + deserializer + ", gson, new " + typeTokenField + "(){}, null)";
        } else {
            throw new IllegalArgumentException(
                    "@JsonAdapter value must be TypeAdapter, TypeAdapterFactory, "
                    + "JsonSerializer or JsonDeserializer reference.");
        }
        //Add this to a member variable
        String fieldName = TYPE_ADAPTER_FIELD_PREFIX + adapterFieldInfo.size();
        String originalFieldName = FileGenUtils.unescapeEscapedString(fieldName);
        TypeName typeName = getAdapterFieldTypeName(fieldType);
        adapterBuilder.addField(typeName, originalFieldName, Modifier.PRIVATE, Modifier.FINAL);
        String statement = fieldName + " = " + getCleanedFieldInitializer(fieldAdapterAccessor);
        if (isNullSafe) {
            statement += ".nullSafe()";
        }
        constructorBuilder.addStatement(statement);

        return fieldName;
    }

    /**
     * Returns the adapter code for the known types.
     */
    private String getAdapterAccessor(@NotNull TypeMirror fieldType, @NotNull Builder adapterBuilder,
                                      @NotNull MethodSpec.Builder constructorBuilder,
                                      @NotNull Map<TypeMirror, String> typeVarsMap,
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
                    String adapterAccessor = getAdapterAccessor(arrayInnerType, adapterBuilder,
                                                                constructorBuilder, typeVarsMap,
                                                                stagGenerator, adapterFieldInfo);
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
                                                                 typeVarsMap,
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
                                                            typeVarsMap,
                                                            stagGenerator, adapterFieldInfo);
                    valueAdapterAccessor = getAdapterAccessor(valueType, adapterBuilder, constructorBuilder,
                                                              typeVarsMap,
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
                sStagFactoryUsed = true;
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
                                                          typeVarsMap,
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
                String typeAdapterCode = getTypeAdapterCodeForGenericTypes(fieldType, adapterBuilder, constructorBuilder, typeVarsMap,
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
    private static String getAdapterForUnknownGenericType(@NotNull TypeMirror fieldType,
                                                          @NotNull Builder adapterBuilder,
                                                          @NotNull MethodSpec.Builder constructorBuilder,
                                                          @NotNull Map<TypeMirror, String> typeVarsMap,
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
                    getTypeTokenCodeForGenericType(fieldType, typeVarsMap) + ")");
        }
        return fieldName;
    }

    @NotNull
    private AdapterFieldInfo addAdapterFields(@Nullable GenericClassInfo genericClassInfo,
                                              @NotNull Builder adapterBuilder,
                                              @NotNull MethodSpec.Builder constructorBuilder,
                                              @NotNull Map<VariableElement, TypeMirror> memberVariables,
                                              @NotNull Map<TypeMirror, String> typeVarsMap,
                                              @NotNull StagGenerator stagGenerator) {

        AdapterFieldInfo result = new AdapterFieldInfo(memberVariables.size());
        boolean hasUnknownGenericField =
                genericClassInfo != null && genericClassInfo.mHasUnknownVarTypeFields;
        for (Map.Entry<VariableElement, TypeMirror> entry : memberVariables.entrySet()) {
            TypeMirror fieldType = entry.getValue();
            JsonAdapter annotation = entry.getKey().getAnnotation(JsonAdapter.class);
            String adapterAccessor = null;
            if (null != annotation) {
                // Using this trick to get the class type
                // https://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
                try {
                    annotation.value();
                } catch (MirroredTypeException mte) {
                    TypeMirror typeMirror = mte.getTypeMirror();
                    ExecutableElement constructor = ElementUtils.getFirstConstructor(typeMirror);
                    TypeUtils.JsonAdapterType jsonAdapterType1 = TypeUtils.getJsonAdapterType(typeMirror);
                    if (constructor != null) {
                        String fieldAdapterAccessor = getFieldAccessorForKnownJsonAdapterType(constructor, adapterBuilder, constructorBuilder, fieldType,
                                                                                              jsonAdapterType1, result, annotation.nullSafe(), getJsonName(entry.getKey()));
                        result.addFieldToAccessor(getJsonName(entry.getKey()), fieldAdapterAccessor);
                    } else {
                        throw new IllegalStateException("Not supported @JsonAdapter value");
                    }
                }

            } else if (hasUnknownGenericField && TypeUtils.containsTypeVarParams(fieldType)) {
                adapterAccessor = getAdapterForUnknownGenericType(fieldType, adapterBuilder, constructorBuilder,
                                                                  typeVarsMap, result);
            } else if (KnownTypeAdapterUtils.hasNativePrimitiveTypeAdapter(fieldType)) {
                adapterAccessor = KnownTypeAdapterUtils.getNativePrimitiveTypeAdapter(fieldType);
            } else {
                adapterAccessor = getAdapterAccessor(fieldType, adapterBuilder, constructorBuilder,
                                                     typeVarsMap, stagGenerator,
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
                                                 @NotNull Map<VariableElement, TypeMirror> memberVariables,
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

        for (Map.Entry<VariableElement, TypeMirror> element : memberVariables.entrySet()) {
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
                        adapterFieldInfo.getAdapterAccessor(element.getValue(), name) + ".write(writer, object." +
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
    public TypeSpec createTypeAdapterSpec(@NotNull StagGenerator stagGenerator) {
        sGsonVariableUsed = false;
        sStagFactoryUsed = false;
        TypeMirror typeMirror = mInfo.getType();
        TypeName typeVariableName = TypeVariableName.get(typeMirror);

        List<? extends TypeMirror> typeArguments = mInfo.getTypeArguments();

        TypeVariableName stagFactoryTypeName = stagGenerator.getGeneratedClassName();
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                                       .addMember("value", "\"unchecked\"")
                                       .addMember("value", "\"rawtypes\"")
                                       .build())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson")
                .addParameter(stagFactoryTypeName, "stagFactory");

        String className = FileGenUtils.unescapeEscapedString(mInfo.getTypeAdapterClassName());
        TypeSpec.Builder adapterBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeVariableName));

        Map<TypeMirror, String> typeVarsMap = new HashMap<>();

        int idx = 0;
        StagGenerator.GenericClassInfo genericClassInfo = null;
        if (null != typeArguments) {
            genericClassInfo = stagGenerator.getGenericClassInfo(mInfo.getType());
            for (TypeMirror innerTypeMirror : typeArguments) {
                if (innerTypeMirror.getKind() == TypeKind.TYPEVAR) {
                    TypeVariable typeVariable = (TypeVariable) innerTypeMirror;
                    String simpleName = typeVariable.asElement().getSimpleName().toString();
                    adapterBuilder.addTypeVariable(TypeVariableName.get(simpleName, TypeVariableName.get(typeVariable.getUpperBound())));
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

        AnnotatedClass annotatedClass = mSupportedTypesModel.getSupportedType(typeMirror);
        if (null == annotatedClass) {
            throw new IllegalStateException("The AnnotatedClass class can't be null in TypeAdapterGenerator : " + typeMirror.toString());
        }
        Map<VariableElement, TypeMirror> memberVariables = annotatedClass.getMemberVariables();

        AdapterFieldInfo adapterFieldInfo =
                addAdapterFields(genericClassInfo, adapterBuilder, constructorBuilder, memberVariables,
                                 typeVarsMap, stagGenerator);

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

        //FieldName -> Accessor Map
        @NotNull
        private final Map<String, String> mFieldAdapterAccessor;

        //Type.toString -> Accessor Map
        @NotNull
        private final Map<String, String> mAdapterFields;

        AdapterFieldInfo(int capacity) {
            mAdapterFields = new HashMap<>(capacity);
            mAdapterAccessor = new HashMap<>(capacity);
            mFieldAdapterAccessor = new HashMap<>(capacity);
        }

        String getAdapterAccessor(@NotNull TypeMirror typeMirror, @NotNull String fieldName) {
            String adapterAccessor = mFieldAdapterAccessor.get(fieldName);
            if (adapterAccessor == null) {
                adapterAccessor = mAdapterAccessor.get(typeMirror.toString());
            }
            return adapterAccessor;
        }

        String getFieldName(@NotNull TypeMirror fieldType) {
            return mAdapterFields.get(fieldType.toString());
        }

        int size() {
            return mAdapterFields.size() + mFieldAdapterAccessor.size();
        }

        void addField(@NotNull TypeMirror fieldType, @NotNull String fieldName) {
            mAdapterFields.put(fieldType.toString(), fieldName);
        }

        void addTypeToAdapterAccessor(@NotNull TypeMirror typeMirror, String accessorCode) {
            mAdapterAccessor.put(typeMirror.toString(), accessorCode);
        }

        void addFieldToAccessor(@NotNull String fieldName, @NotNull String accessorCode) {
            mFieldAdapterAccessor.put(fieldName, accessorCode);
        }
    }
}