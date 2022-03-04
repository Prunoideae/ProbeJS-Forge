package com.prunoideae.probejs.toucher;

import com.google.common.primitives.Primitives;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class ClassToucher {
    private final Class<?> baseClass;
    private boolean dumpSuper;
    private boolean dumpMethods;
    private boolean dumpFields;
    private boolean dumpConstructors;

    private static Class<?> getClassOrComponent(Class<?> clazz) {
        if (clazz == null)
            return null;

        while (clazz.isArray())
            clazz = clazz.getComponentType();
        return clazz;
    }

    public ClassToucher(Class<?> baseClass) {
        this(baseClass, true, true, true, true);
    }

    public ClassToucher(Class<?> baseClass, boolean dumpSuper, boolean dumpFields, boolean dumpMethods, boolean dumpConstructors) {
        this.baseClass = baseClass;
        this.dumpConstructors = dumpConstructors;
        this.dumpSuper = dumpSuper;
        this.dumpFields = dumpFields;
        this.dumpMethods = dumpMethods;
    }

    public ClassToucher setDumpSuper(boolean dumpSuper) {
        this.dumpSuper = dumpSuper;
        return this;
    }

    public ClassToucher setDumpMethod(boolean dumpMethods) {
        this.dumpMethods = dumpMethods;
        return this;
    }

    public ClassToucher setDumpField(boolean dumpFields) {
        this.dumpFields = dumpFields;
        return this;
    }

    public ClassToucher setDumpConstructor(boolean dumpConstructors) {
        this.dumpConstructors = dumpConstructors;
        return this;
    }

    private static List<Class<?>> touchType(Type info) {
        if (info instanceof TypeVariable)
            return new ArrayList<>();
        if (info instanceof WildcardType) {
            List<Type> bounds = new ArrayList<>(List.of(((WildcardType) info).getLowerBounds()));
            bounds.addAll(List.of(((WildcardType) info).getUpperBounds()));
            return bounds.stream().map(ClassToucher::touchType).flatMap(Collection::stream).collect(Collectors.toList());
        }
        if (info instanceof GenericArrayType)
            return touchType(((GenericArrayType) info).getGenericComponentType());
        if (info instanceof ParameterizedType) {
            List<Type> types = new ArrayList<>();
            types.add(((ParameterizedType) info).getRawType());
            types.addAll(List.of(((ParameterizedType) info).getActualTypeArguments()));
            return types.stream().map(ClassToucher::touchType).flatMap(Collection::stream).collect(Collectors.toList());
        }
        if (info instanceof Class) {
            return List.of((Class<?>) info);
        }
        throw new UnsupportedOperationException("Unknown type! %s (%s)".formatted(info.getTypeName(), info.getClass()));
    }

    public Set<Class<?>> touchClass() {
        Set<Class<?>> touched = new HashSet<>();
        ClassInfo baseInfo = new ClassInfo(this.baseClass);

        if (this.dumpSuper) {
            baseInfo.getSuperTypes().forEach(type -> touched.addAll(touchType(type)));
        }

        if (this.dumpFields)
            baseInfo.getFields().forEach(fieldInfo -> touched.addAll(touchType(fieldInfo.getTypeInfo().getType())));
        if (this.dumpMethods)
            baseInfo.getMethods().forEach(methodInfo -> {
                methodInfo.getParamsInfo().forEach(paramInfo -> touched.addAll(touchType(paramInfo.getType())));
                touched.addAll(touchType(methodInfo.getReturnTypeInfo().getType()));
            });
        if (this.dumpConstructors)
            baseInfo.getConstructors().forEach(constructorInfo -> constructorInfo.getParamsInfo().forEach(paramInfo -> touched.addAll(touchType(paramInfo.getType()))));

        return touched
                .stream()
                .filter(Objects::nonNull)
                .map(ClassToucher::getClassOrComponent)
                .filter(clazz -> !Primitives.allPrimitiveTypes().contains(clazz))
                .collect(Collectors.toSet());
    }

    public Set<Class<?>> touchClassRecursive() {
        Set<Class<?>> touched = this.touchClass();
        Set<Class<?>> currentTouched = this.touchClass();
        while (!currentTouched.isEmpty()) {
            Set<Class<?>> nextTouched = new HashSet<>();
            currentTouched.forEach(
                    clazz -> new ClassToucher(clazz)
                            .touchClass()
                            .stream()
                            .map(ClassToucher::getClassOrComponent)
                            .forEach(nextTouched::add));
            currentTouched = nextTouched.stream().filter(clazz -> !touched.contains(clazz)).collect(Collectors.toSet());
            touched.addAll(currentTouched);
        }
        return touched;
    }
}
