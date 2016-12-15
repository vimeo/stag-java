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
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.processor.generators.model.AnnotatedClass;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.generators.model.SupportedTypesModel;
import com.vimeo.stag.processor.utils.FileGenUtils;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

@SuppressWarnings("StringConcatenationMissingWhitespace")
public class TypeAdapterGenerator extends AdapterGenerator {

    private static final String TYPE_ADAPTER_FIELD_PREFIX = "mTypeAdapter";
    @NotNull
    private final ClassInfo mInfo;

    private boolean mGsonVariableUsed;
    private boolean mStagFactoryUsed;

    public TypeAdapterGenerator(@NotNull ClassInfo info) {
        mInfo = info;
    }

    private static class AdapterFieldInfo {

        @NotNull
        private final Map<String, String> mAdapterFields;

        @Nullable
        private Map<String, String> mKnownAdapterStagFunctionCalls;

        AdapterFieldInfo(int capacity) {
            mAdapterFields = new HashMap<>(capacity);
        }

        /**
         * Used to get the stag adapter for a typeMirror if it is already generated in Stag.Factory
         */
        @Nullable
        String getKnownAdapterStagFunctionCalls(TypeMirror typeMirror) {
            return mKnownAdapterStagFunctionCalls != null ? mKnownAdapterStagFunctionCalls.get(
                    typeMirror.toString()) : null;
        }

        /**
         * Add the getter method name against a field name
         */
        void addTypeToFunctionName(String name, String functionName) {
            if (null == mKnownAdapterStagFunctionCalls) {
                mKnownAdapterStagFunctionCalls = new HashMap<>();
            }
            mKnownAdapterStagFunctionCalls.put(name, functionName);
        }

        String getAdapter(@NotNull TypeMirror typeMirror) {
            String typeName = typeMirror.toString();
            String result = null != mKnownAdapterStagFunctionCalls ? mKnownAdapterStagFunctionCalls.get(
                    typeName) : null;
            if (null == result) {
                result = mAdapterFields.get(typeName);
            }
            return result;
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
    }

    @NotNull
    private AdapterFieldInfo addAdapterFields(@NotNull TypeSpec.Builder adapterBuilder,
                                              @NotNull MethodSpec.Builder constructorBuilder,
                                              @NotNull Map<Element, TypeMirror> memberVariables, @NotNull
                                                      TypeTokenConstantsGenerator typeTokenConstantsGenerator,
                                              @NotNull Map<TypeVariable, String> typeVarsMap,
                                              @NotNull StagGenerator stagGenerator) {

        HashSet<TypeMirror> typeSet = new HashSet<>(memberVariables.values());
        AdapterFieldInfo result = new AdapterFieldInfo(typeSet.size());
        HashSet<TypeMirror> exclusiveTypeSet = new HashSet<>();

        for (TypeMirror fieldType : typeSet) {
            if (isSupportedNative(fieldType.toString())) {
                continue;
            }
            if (isArray(fieldType)) {
                fieldType = getArrayInnerType(fieldType);
                if (isSupportedNative(fieldType.toString())) {
                    continue;
                }
            } else if (isMap(fieldType) && fieldType instanceof DeclaredType) {
                List<? extends TypeMirror> typeArguments = ((DeclaredType) fieldType).getTypeArguments();
                if (typeArguments.size() == 2 && isSupportedNative(typeArguments.get(0).toString())) {
                    fieldType = typeArguments.get(1);
                    if (isSupportedNative(fieldType.toString())) {
                        continue;
                    }
                }
            }
            exclusiveTypeSet.add(fieldType);
        }

        for (TypeMirror fieldType : exclusiveTypeSet) {
            String getterField = stagGenerator.getClassAdapterFactoryMethod(fieldType);
            if (null != getterField) {
                /**
                 * If we already have the adapter generated for the fieldType in Stag.Factory class
                 */
                result.addTypeToFunctionName(fieldType.toString(),
                                             "mStagFactory.get" + getterField + "(mGson)");
                mGsonVariableUsed = true;
                mStagFactoryUsed = true;
            } else if (TypeUtils.isConcreteType(fieldType)) {
                getterField = stagGenerator.addFieldType(fieldType);
                result.addTypeToFunctionName(fieldType.toString(),
                                             "mStagFactory.get" + getterField + "(mGson)");
                mGsonVariableUsed = true;
                mStagFactoryUsed = true;
            } else {
                String fieldName = result.getFieldName(fieldType);
                if (null == fieldName) {
                    fieldName = TYPE_ADAPTER_FIELD_PREFIX + result.size();
                    result.addField(fieldType, fieldName);
                    String originalFieldName = FileGenUtils.unescapeEscapedString(fieldName);
                    TypeName typeName = getAdapterFieldTypeName(fieldType);
                    adapterBuilder.addField(typeName, originalFieldName, Modifier.PRIVATE, Modifier.FINAL);
                    constructorBuilder.addStatement(
                            fieldName + " = (TypeAdapter<" + fieldType + ">) gson.getAdapter(" +
                            getTypeTokenCode(fieldType, typeVarsMap, typeTokenConstantsGenerator) + ")");
                }
            }
        }

        return result;
    }

    @Nullable
    private static String getTypeTokenCode(@NotNull TypeMirror fieldType,
                                           @NotNull Map<TypeVariable, String> typeVarsMap,
                                           @NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator) {
        String result = null;
        if (!TypeUtils.isConcreteType(fieldType)) {
            if (fieldType.getKind() == TypeKind.TYPEVAR) {
                result = " com.google.gson.reflect.TypeToken.get(" + typeVarsMap.get(fieldType) + ")";
            } else if (fieldType instanceof DeclaredType) {
                /**
                 * If it is of ParameterizedType, {@link com.vimeo.stag.utils.ParameterizedTypeUtil} is used to get the
                 * type token of the parameter type.
                 */
                DeclaredType declaredFieldType = (DeclaredType) fieldType;
                List<? extends TypeMirror> typeMirrors = ((DeclaredType) fieldType).getTypeArguments();
                result = "com.google.gson.reflect.TypeToken.getParameterized(" +
                         declaredFieldType.asElement().toString() + ".class";
                /**
                 * Iterate through all the types from the typeArguments and generate type token code accordingly
                 */
                for (TypeMirror parameterTypeMirror : typeMirrors) {
                    if (isSupportedNative(parameterTypeMirror.toString())) {
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

    static boolean isSupportedPrimitive(@NotNull String type) {
        return type.equals(long.class.getName()) || type.equals(double.class.getName()) ||
               type.equals(boolean.class.getName()) || type.equals(float.class.getName()) ||
               type.equals(int.class.getName());
    }

    private static boolean isNativeArray(@NotNull TypeMirror type) {
        return (type instanceof ArrayType);
    }

    static boolean isArray(@Nullable TypeMirror type) {
        if (type == null) {
            return false;
        }
        if (isNativeArray(type)) {
            return true;
        }
        String outerClassType = TypeUtils.getOuterClassType(type);
        return outerClassType.equals(ArrayList.class.getName()) ||
               outerClassType.equals(List.class.getName()) ||
               outerClassType.equals(Collection.class.getName());
    }

    static boolean isMap(@Nullable TypeMirror type) {
        if (type == null) {
            return false;
        }
        String outerClassType = TypeUtils.getOuterClassType(type);
        return outerClassType.equals(Map.class.getName()) ||
               outerClassType.equals(HashMap.class.getName()) ||
               outerClassType.equals(LinkedHashMap.class.getName());
    }


    @NotNull
    private static TypeName getAdapterFieldTypeName(@NotNull TypeMirror type) {
        TypeName typeName = TypeVariableName.get(type);
        return ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeName);
    }

    static boolean isSupportedNative(@NotNull String type) {
        return isSupportedPrimitive(type) || type.equals(String.class.getName()) ||
               type.equals(Long.class.getName()) || type.equals(Integer.class.getName()) ||
               type.equals(Boolean.class.getName()) || type.equals(Double.class.getName()) ||
               type.equals(Float.class.getName()) || type.equals(Number.class.getName());
    }

    /**
     * Check if the type is one of the numbers
     */
    static boolean isNumberType(@NotNull String typeString) {
        return typeString.equals(long.class.getName()) || typeString.equals(Long.class.getName()) ||
               typeString.equals(double.class.getName()) || typeString.equals(Double.class.getName()) ||
               typeString.equals(int.class.getName()) || typeString.equals(Integer.class.getName()) ||
               typeString.equals(float.class.getName()) || typeString.equals(Float.class.getName());
    }

    @Nullable
    private static String getReadTokenType(@NotNull TypeMirror type) {
        String typeString = type.toString();
        if (isNumberType(typeString)) {
            return "com.google.gson.stream.JsonToken.NUMBER";
        } else if (type.toString().equals(boolean.class.getName())) {
            return "com.google.gson.stream.JsonToken.BOOLEAN";
        } else if (type.toString().equals(String.class.getName())) {
            return "com.google.gson.stream.JsonToken.STRING";
        } else if (isArray(type)) {
            return "com.google.gson.stream.JsonToken.BEGIN_ARRAY";
        } else {
            return null;
        }
    }

    @NotNull
    private static TypeMirror getArrayInnerType(@NotNull TypeMirror type) {
        return (type instanceof ArrayType) ? ((ArrayType) type).getComponentType() : ((DeclaredType) type).getTypeArguments()
                .get(0);
    }

    @NotNull
    private static MethodSpec getWriteMethodSpec(@NotNull TypeName typeName,
                                                 @NotNull Map<Element, TypeMirror> memberVariables,
                                                 @NotNull AdapterFieldInfo adapterFieldInfo) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("write")
                .addParameter(JsonWriter.class, "writer")
                .addParameter(typeName, "object")
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class);

        builder.addCode("\twriter.beginObject();\n" +
                        "\tif (object == null) {\n" +
                        "\t\twriter.endObject();\n" +
                        "\t\treturn;\n" +
                        "\t}\n");

        for (Map.Entry<Element, TypeMirror> element : memberVariables.entrySet()) {
            String name = getJsonName(element.getKey());
            String variableName = element.getKey().getSimpleName().toString();
            String variableType = element.getValue().toString();

            boolean isPrimitive = isSupportedPrimitive(variableType);

            String prefix = isPrimitive ? "\t" : "\t\t";
            if (!isPrimitive) {
                builder.addCode("\tif (object." + variableName + " != null) {\n");
            }
            builder.addCode(getWriteCode(prefix, element.getValue(), name, "object." + variableName,
                                         adapterFieldInfo));
            if (!isPrimitive) {
                builder.addCode("\t}\n");
            }

            /**
             * If the element is annotated with NonNull annotation, throw {@link IOException} if it is null.
             */
            for (AnnotationMirror annotationMirror : element.getKey().getAnnotationMirrors()) {
                switch (annotationMirror.toString()) {
                    case "@android.support.annotation.NonNull":
                        builder.addCode("\n\telse if (object." + variableName + " == null) {");
                        builder.addCode("\n\t\tthrow new java.io.IOException(\"" + variableName +
                                        " cannot be null\");");
                        builder.addCode("\n\t}\n\n");
                        break;
                }
            }
        }

        builder.addCode("\twriter.endObject();\n");

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
        mGsonVariableUsed = false;
        mStagFactoryUsed = false;
        TypeMirror typeMirror = mInfo.getType();
        TypeName typeVariableName = TypeVariableName.get(typeMirror);

        List<? extends TypeMirror> typeArguments = mInfo.getTypeArguments();

        TypeVariableName stagFactoryTypeName = stagGenerator.getGeneratedClassName();
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson")
                .addParameter(stagFactoryTypeName, "stagFactory");

        String className = FileGenUtils.unescapeEscapedString(mInfo.getTypeAdapterClassName());
        TypeSpec.Builder adapterBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeVariableName));

        Map<TypeVariable, String> typeVarsMap = new HashMap<>();

        int idx = 0;
        if (null != typeArguments) {

            for (TypeMirror argType : typeArguments) {
                if (argType.getKind() == TypeKind.TYPEVAR) {
                    TypeVariable typeVariable = (TypeVariable) argType;
                    String simpleName = typeVariable.asElement().getSimpleName().toString();
                    adapterBuilder.addTypeVariable(TypeVariableName.get(simpleName, TypeVariableName.get(
                            typeVariable.getUpperBound())));

                    String paramName = "type" + "[" + String.valueOf(idx) + "]";
                    typeVarsMap.put(typeVariable, paramName);
                    idx++;
                }
            }

            if (idx > 0) {
                constructorBuilder.addParameter(Type[].class, "type");
                constructorBuilder.varargs(true);
            }
        }

        AnnotatedClass annotatedClass = SupportedTypesModel.getInstance().getSupportedType(typeMirror);
        Map<Element, TypeMirror> memberVariables = annotatedClass.getMemberVariables();

        AdapterFieldInfo adapterFieldInfo =
                addAdapterFields(adapterBuilder, constructorBuilder, memberVariables,
                                 typeTokenConstantsGenerator, typeVarsMap, stagGenerator);

        MethodSpec writeMethod = getWriteMethodSpec(typeVariableName, memberVariables, adapterFieldInfo);
        MethodSpec readMethod = getReadMethodSpec(typeVariableName, memberVariables, adapterFieldInfo);

        if (mGsonVariableUsed) {
            adapterBuilder.addField(Gson.class, "mGson", Modifier.FINAL, Modifier.PRIVATE);
            constructorBuilder.addStatement("this.mGson = gson");
        }

        if (mStagFactoryUsed) {
            adapterBuilder.addField(stagFactoryTypeName, "mStagFactory", Modifier.FINAL, Modifier.PRIVATE);
            constructorBuilder.addStatement("this.mStagFactory = stagFactory");
        }

        adapterBuilder.addMethod(constructorBuilder.build());
        adapterBuilder.addMethod(writeMethod);
        adapterBuilder.addMethod(readMethod);

        return adapterBuilder.build();
    }

    @NotNull
    private static String getArrayListType(@NotNull TypeMirror innerArrayType) {
        String innerArrayTypeString = innerArrayType.toString();
        if (innerArrayTypeString.equals(long.class.getName())) {
            return Long.class.getName();
        }
        if (innerArrayTypeString.equals(double.class.getName())) {
            return Double.class.getName();
        }
        if (innerArrayTypeString.equals(Boolean.class.getName())) {
            return Boolean.class.getName();
        }
        if (innerArrayTypeString.equals(float.class.getName())) {
            return Float.class.getName();
        }
        if (innerArrayTypeString.equals(int.class.getName())) {
            return Integer.class.getName();
        } else {
            return innerArrayType.toString();
        }
    }

    @NotNull
    private static String getReadCode(@NotNull String prefix, @NotNull String variableName,
                                      @NotNull TypeMirror type, @NotNull AdapterFieldInfo adapterFieldInfo) {
        if (isArray(type)) {
            TypeMirror innerType = getArrayInnerType(type);
            boolean isNativeArray = isNativeArray(type);
            String innerRead = getReadType(type, innerType, adapterFieldInfo);
            String arrayListVariableName = isNativeArray ? "tmpArray" : "object." + variableName;
            String stagGetterName = adapterFieldInfo.getKnownAdapterStagFunctionCalls(innerType);
            String result = prefix + "reader.beginArray();\n" +
                            prefix + (isNativeArray ?
                    "java.util.ArrayList<" + getArrayListType(innerType) + "> " : "") +
                            arrayListVariableName + " = new java.util.ArrayList<>();\n" +
                            (stagGetterName != null ?
                                    prefix + "TypeAdapter<" + innerType + "> adapter = " + stagGetterName +
                                    ";\n" : "") +
                            prefix + "while (reader.hasNext()) {\n" +
                            prefix + "\t" + arrayListVariableName + ".add(" + innerRead + ");\n" +
                            prefix + "}\n" +
                            prefix + "reader.endArray();\n";
            if (isNativeArray) {
                result += prefix + "object." + variableName + "= new " + innerType.toString() + "[" +
                          arrayListVariableName + ".size()];\n";
                result += prefix + "for(int idx = 0; idx < " + arrayListVariableName + ".size(); idx++) {\n";
                result += prefix + "\tobject." + variableName + "[idx] = " + arrayListVariableName +
                          ".get(idx);\n";
                result += prefix + "}\n";
            }
            return result;
        } else {
            if (isMap(type) && type instanceof DeclaredType) {
                List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
                if (typeArguments.size() == 2 && isSupportedNative(typeArguments.get(0).toString())) {
                    String keyRead = getReadType(type, typeArguments.get(0), adapterFieldInfo);
                    String valueRead = getReadType(type, typeArguments.get(1), adapterFieldInfo);

                    return prefix +
                           "if(reader.peek() == com.google.gson.stream.JsonToken.BEGIN_OBJECT) {\n" +
                           prefix + "\tobject." + variableName + "= new " +
                           (TypeUtils.getOuterClassType(type)
                                   .equals(Map.class.getName()) ? "java.util.LinkedHashMap<>" : type.toString()) +
                           "();\n" +
                           prefix + "\treader.beginObject();\n" +
                           prefix + "\twhile (reader.hasNext()) {\n" +
                           prefix +
                           "\t\tcom.google.gson.internal.JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);\n" +
                           prefix + "\t\t" + typeArguments.get(0).toString() + " key = " + keyRead +
                           ";\n" +
                           prefix + "\t\t" + typeArguments.get(1).toString() + " value = " +
                           valueRead + ";\n" +
                           prefix + "\t\t" + typeArguments.get(1).toString() +
                           " replaced = object." + variableName + ".put(key, value);\n" +
                           prefix + "\t\tif (replaced != null) {\n" +
                           prefix +
                           "\t\t\tthrow new com.google.gson.JsonSyntaxException(\"duplicate key: \" + key);\n" +
                           prefix + "\t\t}\n" +
                           prefix + "\t}\n" +
                           prefix + "\treader.endObject();\n" +
                           prefix +
                           "} else if(reader.peek() == com.google.gson.stream.JsonToken.BEGIN_ARRAY) {\n" +
                           prefix + "\tobject." + variableName + "= new " +
                           (TypeUtils.getOuterClassType(type)
                                   .equals(Map.class.getName()) ? "java.util.LinkedHashMap<>" : type.toString()) +
                           "();\n" +
                           prefix + "\treader.beginArray();\n" +
                           prefix + "\twhile (reader.hasNext()) {\n" +
                           prefix + "\t\treader.beginArray();\n" +
                           prefix + "\t\t" + typeArguments.get(0).toString() + " key = " + keyRead +
                           ";\n" +
                           prefix + "\t\t" + typeArguments.get(1).toString() + " value = " +
                           valueRead + ";\n" +
                           prefix + "\t\t" + typeArguments.get(1).toString() +
                           " replaced = object." + variableName + ".put(key, value);\n" +
                           prefix + "\t\tif (replaced != null) {\n" +
                           prefix +
                           "\t\t\tthrow new com.google.gson.JsonSyntaxException(\"duplicate key: \" + key);\n" +
                           prefix + "\t\t}\n" +
                           prefix + "\t\treader.endArray();\n" +
                           prefix + "\t}\n" +
                           prefix + "\treader.endArray();\n" +
                           prefix + "} else {\n" +
                           prefix + "\treader.skipValue();\n" +
                           prefix + "}";
                }
            }
            return prefix + "object." + variableName + " = " +
                   getReadType(type, type, adapterFieldInfo) + ";";
        }
    }

    @NotNull
    private static String getReadType(@NotNull TypeMirror parentType, @NotNull TypeMirror type,
                                      @NotNull AdapterFieldInfo adapterFieldInfo) {
        String typeString = type.toString();
        if (typeString.equals(long.class.getName()) || typeString.equals(Long.class.getName())) {
            return "reader.nextLong()";
        } else if (typeString.equals(double.class.getName()) || typeString.equals(Double.class.getName())) {
            return "reader.nextDouble()";
        } else if (typeString.equals(boolean.class.getName()) || typeString.equals(Boolean.class.getName())) {
            return "reader.nextBoolean()";
        } else if (typeString.equals(String.class.getName())) {
            return "reader.nextString()";
        } else if (typeString.equals(int.class.getName()) || typeString.equals(Integer.class.getName())) {
            return "reader.nextInt()";
        } else if (typeString.equals(float.class.getName()) || typeString.equals(Float.class.getName())) {
            return "(float) reader.nextDouble()";
        } else {
            return getAdapterRead(parentType, type, adapterFieldInfo);
        }
    }

    @NotNull
    private static String getWriteCode(@NotNull String prefix, @NotNull TypeMirror type,
                                       @NotNull String jsonName, @NotNull String variableName,
                                       @NotNull AdapterFieldInfo adapterFieldInfo) {
        if (isArray(type)) {
            TypeMirror innerType = getArrayInnerType(type);
            String innerWrite = getWriteType(innerType, "item", adapterFieldInfo);
            return prefix + "writer.name(\"" + jsonName + "\");\n" +
                   prefix + "writer.beginArray();\n" +
                   prefix + "for (" + innerType + " item : " + variableName + ") {\n" +
                   prefix + "\t" + innerWrite + "\n" +
                   prefix + "}\n" +
                   prefix + "writer.endArray();\n";
        } else {
            if (isMap(type) && type instanceof DeclaredType) {
                List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
                if (typeArguments.size() == 2 && isSupportedNative(typeArguments.get(0).toString())) {
                    String valueWrite =
                            getWriteType(typeArguments.get(1), "entry.getValue()", adapterFieldInfo);
                    return prefix + "writer.name(\"" + jsonName + "\");\n" +
                           prefix + "writer.beginObject();\n" +
                           prefix + "for (" + type.toString().replaceFirst("<", ".Entry<") + " entry : " +
                           variableName + ".entrySet()) {\n" +
                           prefix + "\twriter.name(String.valueOf(entry.getKey()));\n" +
                           prefix + "\t" + valueWrite + "\n" +
                           prefix + "}\n" +
                           prefix + "writer.endObject();\n";
                }
            }
            return prefix + "writer.name(\"" + jsonName + "\");\n" +
                   prefix + getWriteType(type, variableName, adapterFieldInfo) + '\n';
        }
    }

    @NotNull
    private static String getWriteType(@NotNull TypeMirror type, @NotNull String variableName,
                                       @NotNull AdapterFieldInfo adapterFieldInfo) {
        if (isSupportedNative(type.toString())) {
            return "writer.value(" + variableName + ");";
        } else {
            return getAdapterWrite(type, variableName, adapterFieldInfo) + ";";
        }
    }

    @NotNull
    private static String getAdapterWrite(@NotNull TypeMirror type, @NotNull String variableName,
                                          @NotNull AdapterFieldInfo adapterFieldInfo) {
        String adapterField = adapterFieldInfo.getAdapter(type);
        return adapterField + ".write(writer, " + variableName + ")";
    }

    @NotNull
    private static String getAdapterRead(@NotNull TypeMirror parentType, @NotNull TypeMirror type,
                                         @NotNull AdapterFieldInfo adapterFieldInfo) {
        String adapterCode;
        if (adapterFieldInfo.getKnownAdapterStagFunctionCalls(type) != null && isArray(parentType)) {
            adapterCode = "adapter.read(reader)";
        } else {
            adapterCode = adapterFieldInfo.getAdapter(type) + ".read(reader)";
        }
        return adapterCode;
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

        List<String> nonNullFields = new ArrayList<>();

        for (Map.Entry<Element, TypeMirror> element : elements.entrySet()) {
            String name = getJsonName(element.getKey());
            String variableName = element.getKey().getSimpleName().toString();
            String jsonTokenType = getReadTokenType(element.getValue());

            if (jsonTokenType != null) {
                builder.addCode("\t\t\tcase \"" + name + "\":\n" +
                                "\t\t\t\tif (jsonToken == " + jsonTokenType +
                                ") {\n" +
                                getReadCode("\t\t\t\t\t", variableName, element.getValue(),
                                            adapterFieldInfo) +
                                "\n\t\t\t\t} else {" +
                                "\n\t\t\t\t\treader.skipValue();" +
                                "\n\t\t\t\t}");
            } else {
                builder.addCode("\t\t\tcase \"" + name + "\":\n" +
                                getReadCode("\t\t\t\t\t", variableName, element.getValue(),
                                            adapterFieldInfo));
            }

            builder.addCode("\n\t\t\t\tbreak;\n");
            for (AnnotationMirror annotationMirror : element.getKey().getAnnotationMirrors()) {
                switch (annotationMirror.toString()) {
                    case "@android.support.annotation.NonNull":
                        nonNullFields.add(variableName);
                        break;
                }
            }
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
}