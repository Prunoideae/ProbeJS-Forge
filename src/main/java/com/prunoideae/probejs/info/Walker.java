package com.prunoideae.probejs.info;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Walker {
    private final Set<Class<?>> initial;
    private boolean walkMethod = true;
    private boolean walkField = true;
    private boolean walkSuper = true;
    private boolean walkType = true;


    public Walker(Set<Class<?>> initial) {
        this.initial = initial;
    }

    public void setWalkField(boolean walkField) {
        this.walkField = walkField;
    }

    public void setWalkMethod(boolean walkMethod) {
        this.walkMethod = walkMethod;
    }

    public void setWalkSuper(boolean walkSuper) {
        this.walkSuper = walkSuper;
    }

    public void setWalkType(boolean walkType) {
        this.walkType = walkType;
    }

    private Set<Class<?>> walkType(TypeInfo type) {
        Set<Class<?>> result = new HashSet<>();
        if (walkType) {

        }
        if (type.isClazz() || type.isParameterized() || type.isArray())
            result.add((Class<?>) type.getRawType());

        return result;
    }

    private Set<Class<?>> touch(Set<Class<?>> classes) {
        Set<Class<?>> result = new HashSet<>();
        for (Class<?> clazz : classes) {
            ClassInfo info = new ClassInfo(clazz);

            if (walkSuper) {
                ClassInfo superclass = info.getSuperClass();
                if (superclass != null)
                    result.add(superclass.getClazz());
                info.getInterfaces().stream().map(ClassInfo::getClazz).forEach(result::add);
            }
            if (walkField)
                info.getFieldInfo().forEach(f -> result.addAll(walkType(f.getType())));
            if (walkMethod)
                info.getMethodInfo().forEach(m -> {
                    result.addAll(walkType(m.getReturnType()));
                    m.getParams().forEach(p -> result.addAll(walkType(p.getType())));
                });

        }
        return result;
    }

    public Set<Class<?>> walk() {
        Set<Class<?>> result = new HashSet<>(initial);
        Set<Class<?>> current = touch(result);

        while (!current.isEmpty()) {
            result.addAll(current);
            current = touch(current)
                    .stream()
                    .filter(c -> !result.contains(c))
                    .collect(Collectors.toSet());
        }
        return result;
    }
}
