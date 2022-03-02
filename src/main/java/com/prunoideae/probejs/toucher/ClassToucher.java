package com.prunoideae.probejs.toucher;

import com.google.common.primitives.Primitives;

import javax.lang.model.SourceVersion;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class ClassToucher {
    private final Class<?> baseClass;
    private boolean dumpSuper;
    private boolean dumpMethods;
    private boolean dumpFields;
    private boolean dumpConstructors;

    public static boolean isClassName(String name) {
        return name.contains(".") && SourceVersion.isName(name) && !SourceVersion.isKeyword(name);
    }

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

    private static List<Class<?>> touchType(ClassInfo.TypeInfo info) {
        List<Class<?>> touched = new ArrayList<>();
        touched.add(info.getTypeClass());
        if (info.getTypeArguments() != null)
            touched.addAll(info.getTypeArguments().stream().map(Type::getTypeName).filter(ClassToucher::isClassName).map(className -> {
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList()));
        return touched;
    }

    public Set<Class<?>> touchClass() {
        Set<Class<?>> touched = new HashSet<>();
        if (this.dumpSuper && this.baseClass.getSuperclass() != Object.class)
            touched.add(this.baseClass.getSuperclass());
        ClassInfo baseInfo = new ClassInfo(this.baseClass);
        if (this.dumpFields)
            baseInfo.getFields().forEach(fieldInfo -> touched.addAll(touchType(fieldInfo.getTypeInfo())));
        if (this.dumpMethods)
            baseInfo.getMethods().forEach(methodInfo -> {
                methodInfo.getParamsInfo().forEach(paramInfo -> touched.addAll(touchType(paramInfo)));
                touched.addAll(touchType(methodInfo.getReturnTypeInfo()));
            });
        if (this.dumpConstructors)
            baseInfo.getConstructors().forEach(constructorInfo -> constructorInfo.getParamsInfo().forEach(paramInfo -> touched.addAll(touchType(paramInfo))));

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
