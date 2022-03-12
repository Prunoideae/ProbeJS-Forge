package com.prunoideae.probejs.info;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassInfo {
    private final Class<?> clazz;

    public ClassInfo(Class<?> clazz) {
        this.clazz = clazz;
    }

    public boolean isInterface() {
        return clazz.isInterface();
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public ClassInfo getSuperClass() {
        if (isInterface() || clazz.getSuperclass() == Object.class)
            return null;
        return new ClassInfo(clazz.getSuperclass());
    }

    public List<ClassInfo> getInterfaces() {
        return Arrays.stream(clazz.getInterfaces()).map(ClassInfo::new).toList();
    }

    public List<FieldInfo> getFieldInfo() {
        return Arrays.stream(clazz.getFields()).map(FieldInfo::new).collect(Collectors.toList());
    }

    public List<MethodInfo> getMethodInfo() {
        return Arrays.stream(clazz.getMethods()).map(MethodInfo::new).collect(Collectors.toList());
    }
}
