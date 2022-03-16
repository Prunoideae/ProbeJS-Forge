package com.prunoideae.probejs.info;

import com.prunoideae.probejs.formatter.ClassResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassInfo {
    private final Class<?> clazz;

    public ClassInfo(Class<?> clazz) {
        this.clazz = clazz;
    }

    public boolean isInterface() {
        return clazz.isInterface();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public ClassInfo getSuperClass() {
        if (isInterface() || clazz.getSuperclass() == Object.class || clazz.getSuperclass() == null)
            return null;
        return new ClassInfo(clazz.getSuperclass());
    }

    public List<ClassInfo> getInterfaces() {
        return Arrays.stream(clazz.getInterfaces()).map(ClassInfo::new).toList();
    }

    public List<FieldInfo> getFieldInfo() {
        return Arrays.stream(clazz.getFields())
                .map(FieldInfo::new)
                .filter(field -> ClassResolver.acceptField(field.getName()))
                .filter(field -> !field.shouldHide())
                .collect(Collectors.toList());
    }

    public List<ConstructorInfo> getConstructorInfo() {
        return Arrays.stream(clazz.getConstructors()).map(ConstructorInfo::new).collect(Collectors.toList());
    }

    private Set<Method> getAllSuperMethods() {
        Set<Method> methods = new HashSet<>();
        if (!clazz.isInterface()) {
            if (clazz.getSuperclass() != null)
                methods.addAll(List.of(clazz.getSuperclass().getMethods()));
        } else {
            for (Class<?> c : clazz.getInterfaces()) {
                methods.addAll(List.of(c.getMethods()));
            }
        }
        return methods;
    }

    public List<MethodInfo> getMethodInfo() {
        Set<Method> m = getAllSuperMethods();
        return Arrays.stream(clazz.getMethods())
                .filter(method -> !m.contains(method) || (!(method.getGenericReturnType() instanceof ParameterizedType) && Arrays.stream(method.getGenericParameterTypes()).noneMatch(t -> t instanceof ParameterizedType)))
                .map(MethodInfo::new)
                .filter(method -> ClassResolver.acceptMethod(method.getName()))
                .filter(method -> !method.shouldHide())
                .collect(Collectors.toList());
    }
}
