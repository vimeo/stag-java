package com.vimeo.stag.processor.generators.typeadapter;

import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.vimeo.stag.processor.generators.TypeAdapterGenerator;
import com.vimeo.stag.processor.generators.model.accessor.FieldAccessor;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class WriteSpecGenerator {

    @NotNull
    public static MethodSpec getWriteMethodSpec(@NotNull TypeName typeName, @NotNull Map<FieldAccessor, TypeMirror> memberVariables,
                                                @NotNull TypeAdapterGenerator.AdapterFieldInfo adapterFieldInfo, boolean serializeNulls) {
        final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("write")
                .addParameter(JsonWriter.class, "writer")
                .addParameter(typeName, "object")
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class);

        methodBuilder.beginControlFlow("if (object == null)");
        methodBuilder.addStatement("writer.nullValue()");
        methodBuilder.addStatement("return");
        methodBuilder.endControlFlow();
        methodBuilder.addStatement("writer.beginObject()");

        for (Map.Entry<FieldAccessor, TypeMirror> element : memberVariables.entrySet()) {
            FieldAccessor fieldAccessor = element.getKey();
            final String getterCode = fieldAccessor.createGetterCode();

            String name = fieldAccessor.getJsonName();
            String variableType = element.getValue().toString();

            boolean isPrimitive = TypeUtils.isSupportedPrimitive(variableType);
            if (serializeNulls) {
                specForSerializedNullsEnabled(methodBuilder, element, adapterFieldInfo, fieldAccessor, getterCode, name, isPrimitive);
            } else {
                specForSerializedNullsDisabled(methodBuilder, element, adapterFieldInfo, fieldAccessor, getterCode, name, isPrimitive);
            }
        }

        methodBuilder.addCode("\n");
        methodBuilder.addStatement("writer.endObject()");
        return methodBuilder.build();
    }

    private static void specForSerializedNullsDisabled(@NotNull MethodSpec.Builder methodBuilder, @NotNull Map.Entry<FieldAccessor, TypeMirror> element,
                                                       @NotNull TypeAdapterGenerator.AdapterFieldInfo adapterFieldInfo, @NotNull FieldAccessor fieldAccessor,
                                                       @NotNull String getterCode, @NotNull String name, boolean isPrimitive) {
        methodBuilder.addCode("\n");
        if (!isPrimitive) {
            methodBuilder.beginControlFlow("if (object." + getterCode + " != null) ");
        }
        methodBuilder.addStatement("writer.name(\"" + name + "\")");
        if (!isPrimitive) {
            methodBuilder.addStatement(
                    adapterFieldInfo.getAdapterAccessor(element.getValue(), name) + ".write(writer, object." +
                            getterCode + ")");
            /*
             * If the element is annotated with NotNull annotation, throw {@link IOException} if it is null.
             */
            if (fieldAccessor.doesRequireNotNull()) {
                methodBuilder.endControlFlow();
                methodBuilder.beginControlFlow("else if (object." + getterCode + " == null)");
                methodBuilder.addStatement("throw new java.io.IOException(\"" + getterCode +
                        " cannot be null\")");
            }
            methodBuilder.endControlFlow();
        } else {
            methodBuilder.addStatement("writer.value(object." + getterCode + ")");
        }
    }

    private static void specForSerializedNullsEnabled(@NotNull MethodSpec.Builder methodBuilder, @NotNull Map.Entry<FieldAccessor, TypeMirror> element,
                                                      @NotNull TypeAdapterGenerator.AdapterFieldInfo adapterFieldInfo,
                                                      @NotNull FieldAccessor fieldAccessor, @NotNull String getterCode, @NotNull String name, boolean isPrimitive) {
        methodBuilder.addCode("\n");
        methodBuilder.addStatement("writer.name(\"" + name + "\")");
        if (!isPrimitive) {
            methodBuilder.beginControlFlow("if (object." + getterCode + " != null) ");
        }
        if (!isPrimitive) {
            methodBuilder.addStatement(
                    adapterFieldInfo.getAdapterAccessor(element.getValue(), name) + ".write(writer, object." +
                            getterCode + ")");
            /*
             * If the element is annotated with NotNull annotation, throw {@link IOException} if it is null.
             */
            methodBuilder.endControlFlow();
            methodBuilder.beginControlFlow("else");
            if (fieldAccessor.doesRequireNotNull()) {
                //throw exception in case the field is annotated as NotNull
                methodBuilder.addStatement("throw new java.io.IOException(\"" + getterCode + " cannot be null\")");
            } else {
                //write null value to the writer if the field is null
                methodBuilder.addStatement("writer.nullValue()");
            }
            methodBuilder.endControlFlow();
        } else {
            methodBuilder.addStatement("writer.value(object." + getterCode + ")");
        }
    }
}
