package com.prunoideae.probejs.document.type;

import java.util.List;
import java.util.stream.Collectors;

public class TypeParameterized implements IType {
    private final IType rawType;
    private final List<IType> paramTypes;

    public TypeParameterized(IType rawType, List<IType> paramTypes) {
        this.rawType = rawType;
        this.paramTypes = paramTypes;
    }

    public IType getRawType() {
        return rawType;
    }

    public List<IType> getParamTypes() {
        return paramTypes;
    }

    @Override
    public String getTypeName() {
        return "%s<%s>".formatted(rawType.getTypeName(), paramTypes.stream().map(IType::getTypeName).collect(Collectors.joining(", ")));
    }
}
