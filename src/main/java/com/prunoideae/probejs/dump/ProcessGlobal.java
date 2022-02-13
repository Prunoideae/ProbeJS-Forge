package com.prunoideae.probejs.dump;

import com.google.gson.JsonArray;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ProcessGlobal {

    public static Class<?> getClassOrComponent(Class<?> clazz) {
        if (clazz.isArray())
            return clazz.getComponentType();
        else
            return clazz;
    }

    private static Set<Class<?>> touchClasses(Set<Class<?>> currentClasses) {
        HashSet<Class<?>> collected = new HashSet<>();
        currentClasses.forEach(clazz -> {
            collected.addAll(Arrays.stream(clazz.getFields()).map(Field::getType).map(ProcessGlobal::getClassOrComponent).collect(Collectors.toList()));
            collected.addAll(Arrays.stream(clazz.getMethods()).map(Method::getReturnType).map(ProcessGlobal::getClassOrComponent).collect(Collectors.toList()));
            collected.addAll(Arrays.stream(clazz.getMethods()).flatMap(method -> Arrays.stream(method.getParameterTypes())).map(ProcessGlobal::getClassOrComponent).collect(Collectors.toList()));
            collected.addAll(Arrays.stream(clazz.getConstructors()).flatMap(constructor -> Arrays.stream(constructor.getParameterTypes())).map(ProcessGlobal::getClassOrComponent).collect(Collectors.toList()));
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != clazz)
                collected.add(clazz.getSuperclass());
        });
        return collected;
    }


    public static JsonArray dumpAccessibleClasses(Set<Class<?>> currentClasses) {
        JsonArray classesJson = new JsonArray();

        Set<Class<?>> dumpedClasses = new HashSet<>();

        while (!currentClasses.isEmpty()) {
            currentClasses.forEach(clazz -> classesJson.add(ClassSerializer.serializeClass(clazz)));
            dumpedClasses.addAll(currentClasses);
            currentClasses = touchClasses(currentClasses).stream().filter(clazz -> !dumpedClasses.contains(clazz)).collect(Collectors.toSet());
        }
        return classesJson;
    }
}
