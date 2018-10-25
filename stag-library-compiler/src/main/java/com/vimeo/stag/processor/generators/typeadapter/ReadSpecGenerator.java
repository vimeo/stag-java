package com.vimeo.stag.processor.generators.typeadapter;

import com.google.gson.stream.JsonReader;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.vimeo.stag.processor.generators.TypeAdapterGenerator;
import com.vimeo.stag.processor.generators.model.accessor.FieldAccessor;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class ReadSpecGenerator {
    @NotNull
    public static MethodSpec getReadMethodSpec(@NotNull TypeName typeName,
                                               @NotNull Map<FieldAccessor, TypeMirror> elements,
                                               @NotNull TypeAdapterGenerator.AdapterFieldInfo adapterFieldInfo) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("read")
                .addParameter(JsonReader.class, "reader")
                .returns(typeName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class);

        builder.addStatement("com.google.gson.stream.JsonToken peek = reader.peek()");

        builder.beginControlFlow("if (com.google.gson.stream.JsonToken.NULL == peek)");
        builder.addStatement("reader.nextNull()");
        builder.addStatement("return null");
        builder.endControlFlow();


        builder.beginControlFlow("if (com.google.gson.stream.JsonToken.BEGIN_OBJECT != peek)");
        builder.addStatement("reader.skipValue()");
        builder.addStatement("return null");
        builder.endControlFlow();

        builder.addStatement("reader.beginObject()");
        builder.addStatement(typeName + " object = new " + typeName + "()");

        builder.beginControlFlow("while (reader.hasNext())");
        builder.addStatement("String name = reader.nextName()");
        builder.beginControlFlow("switch (name)");


        final List<FieldAccessor> nonNullFields = new ArrayList<>();

        for (Map.Entry<FieldAccessor, TypeMirror> element : elements.entrySet()) {
            final FieldAccessor fieldAccessor = element.getKey();
            String name = fieldAccessor.getJsonName();

            final TypeMirror elementValue = element.getValue();

            builder.addCode("case \"" + name + "\":\n");

            String[] alternateJsonNames = fieldAccessor.getAlternateJsonNames();
            if (alternateJsonNames != null && alternateJsonNames.length > 0) {
                for (String alternateJsonName : alternateJsonNames) {
                    builder.addCode("case \"" + alternateJsonName + "\":\n");
                }
            }

            String variableType = element.getValue().toString();
            boolean isPrimitive = TypeUtils.isSupportedPrimitive(variableType);

            if (isPrimitive) {
                builder.addStatement("\tobject." +
                        fieldAccessor.createSetterCode(adapterFieldInfo.getAdapterAccessor(elementValue, name) +
                                ".read(reader, object." + fieldAccessor.createGetterCode() + ")"));

            } else {
                builder.addStatement("\tobject." + fieldAccessor.createSetterCode(adapterFieldInfo.getAdapterAccessor(elementValue, name) +
                        ".read(reader)"));
            }


            builder.addStatement("\tbreak");
            if (fieldAccessor.doesRequireNotNull()) {
                if (!TypeUtils.isSupportedPrimitive(elementValue.toString())) {
                    nonNullFields.add(fieldAccessor);
                }
            }
        }

        builder.addCode("default:\n");
        builder.addStatement("reader.skipValue()");
        builder.addStatement("break");
        builder.endControlFlow();
        builder.endControlFlow();

        builder.addStatement("reader.endObject()");

        for (FieldAccessor nonNullField : nonNullFields) {
            builder.beginControlFlow("if (object." + nonNullField.createGetterCode() + " == null)");
            builder.addStatement("throw new java.io.IOException(\"" + nonNullField.createGetterCode() + " cannot be null\")");
            builder.endControlFlow();
        }

        builder.addStatement("return object");

        return builder.build();
    }
}
