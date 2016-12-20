package com.vimeo.stag.processor.utils;

import com.vimeo.stag.KnownTypeAdapters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.lang.model.type.TypeMirror;

public class KnownTypeAdapterUtils {
    @NotNull
    private static final HashMap<Type, Class> mKnownPrimitiveTypeAdapters = new HashMap<>();
    @NotNull
    private static final HashMap<Type, Class> mKnownCollectionTypeAdapters = new HashMap<>();
    @NotNull
    private static final HashMap<String, String> mKnownTypeAdaptersMethodNames = new HashMap<>();

    @NotNull
    public static HashMap<Type, Class> getKnownCollectionTypeAdapters() {
        return mKnownCollectionTypeAdapters;
    }

    @NotNull
    public static HashMap<Type, Class> getKnownPrimitiveTypeAdapters() {
        return mKnownPrimitiveTypeAdapters;
    }

    @Nullable
    public static String getKnownTypeAdaptersMethodNames(String type) {
        return mKnownTypeAdaptersMethodNames.get(type);
    }

    public static void initialize() {
        KnownTypeAdapterUtils.registerPrimitiveTypeAdapters(Integer.class, KnownTypeAdapters.IntegerTypeAdapter.class);
        KnownTypeAdapterUtils.registerPrimitiveTypeAdapters(String.class, KnownTypeAdapters.StringTypeAdapter.class);
        KnownTypeAdapterUtils.registerPrimitiveTypeAdapters(Double.class, KnownTypeAdapters.DoubleTypeAdapter.class);
        KnownTypeAdapterUtils.registerPrimitiveTypeAdapters(Float.class, KnownTypeAdapters.FloatTypeAdapter.class);
        KnownTypeAdapterUtils.registerPrimitiveTypeAdapters(Boolean.class, KnownTypeAdapters.BooleanTypeAdapter.class);

        mKnownCollectionTypeAdapters.put(List.class, KnownTypeAdapters.ListTypeAdapter.class);
        mKnownCollectionTypeAdapters.put(Map.class, KnownTypeAdapters.MapTypeAdapter.class);
    }

    private static void registerPrimitiveTypeAdapters(Class type, Class typeAdapterClass) {
        mKnownPrimitiveTypeAdapters.put(type, typeAdapterClass);
        if (mKnownTypeAdaptersMethodNames.get(type.toString()) == null) {
            String adapterName = FileGenUtils.unescapeEscapedString(typeAdapterClass.getSimpleName());
            String getAdapterFactoryMethodName = "get" + adapterName;
            mKnownTypeAdaptersMethodNames.put(type.getName(), getAdapterFactoryMethodName);
        }
    }

    @NotNull
    public static String getListInstantiater(@NotNull TypeMirror typeMirror) {
        String outerClassType = TypeUtils.getOuterClassType(typeMirror);
        if (outerClassType.equals(ArrayList.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.ArrayListInstantiater";
        } else if (outerClassType.equals(List.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.ListInstantiater";
        } else if (outerClassType.equals(Collection.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.CollectionInstantiater";
        } else {
            return "com.vimeo.stag.KnownTypeAdapters.ArrayListInstantiater";
        }
    }

    @NotNull
    public static String getMapInstantiater(@NotNull TypeMirror typeMirror) {
        String outerClassType = TypeUtils.getOuterClassType(typeMirror);
        if (outerClassType.equals(Map.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.MapInstantiater";
        } else if (outerClassType.equals(HashMap.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.HashMapInstantiater";
        } else if (outerClassType.equals(LinkedHashMap.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.LinkedHashMapInstantiater";
        } else if (outerClassType.equals(ConcurrentHashMap.class.getName())) {
            return "com.vimeo.stag.KnownTypeAdapters.ConcurrentHashMapInstantiater";
        } else {
            return "com.vimeo.stag.KnownTypeAdapters.HashMapInstantiater";
        }
    }
}