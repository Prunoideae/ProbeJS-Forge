package com.prunoideae.probejs.info;

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

    public boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }

    public TypeInfo getReturnType() {
        return new TypeInfo(method.getGenericReturnType());
    }

    public List<ParamInfo> getParams() {
        return Arrays.stream(method.getParameters()).map(ParamInfo::new).collect(Collectors.toList());
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
