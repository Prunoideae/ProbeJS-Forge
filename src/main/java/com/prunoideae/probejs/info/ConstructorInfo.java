package com.prunoideae.probejs.info;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConstructorInfo {
    private final Constructor<?> constructor;

    public ConstructorInfo(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    public List<MethodInfo.ParamInfo> getParams() {
        return Arrays.stream(constructor.getParameters()).map(MethodInfo.ParamInfo::new).collect(Collectors.toList());
    }
}
