package com.prunoideae.probejs.info;

import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapForJS;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MethodInfo {
    private final Method method;

    public MethodInfo(Method method) {
        this.method = method;
    }

    public String getName() {
        if (method.getAnnotation(RemapForJS.class) != null)
            return method.getAnnotation(RemapForJS.class).value();
        return method.getName();
    }

    public boolean shouldHide() {
        return method.getAnnotation(HideFromJS.class) != null;
    }

    public boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }

    public TypeInfo getReturnType() {
        return new TypeInfo(method.getGenericReturnType());
    }

    public List<ParamInfo> getParams() {
        return Arrays.stream(method.getParameters()).map(ParamInfo::new).collect(Collectors.toList());
    }

    public List<TypeInfo> getTypeVariables() {
        return Arrays.stream(method.getTypeParameters()).map(TypeInfo::new).collect(Collectors.toList());
    }


    public static class ParamInfo {
        private final Parameter parameter;

        public ParamInfo(Parameter parameter) {
            this.parameter = parameter;
        }

        public String getName() {
            return parameter.getName();
        }

        public TypeInfo getType() {
            return new TypeInfo(parameter.getParameterizedType());
        }

    }
}
