package com.prunoideae.probejs.info;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TypeInfo {
    private final Type type;

    public TypeInfo(Type type) {
        this.type = type;
    }

    public boolean isParameterized() {
        return this.type instanceof ParameterizedType;
    }

    public boolean isWildcard() {
        return this.type instanceof WildcardType;
    }

    public boolean isVariable() {
        return this.type instanceof TypeVariable;
    }

    public boolean isArray() {
        return (type instanceof Class<?> clazz && clazz.isArray()) || (this.type instanceof GenericArrayType);
    }

    public boolean isClazz() {
        return this.type instanceof Class<?> clazz && !clazz.isArray();
    }

    public Type getRawType() {
        if (isParameterized())
            return ((ParameterizedType) type).getRawType();
        if (isClazz())
            return type;
        if (isArray())
            return getComponent().getRawType();
        if (isWildcard())
            return getWildcardBound().getRawType();
        return Object.class;
    }

    public List<TypeInfo> getParameterizedInfo() {
        if (!isParameterized())
            return null;
        return Arrays.stream(((ParameterizedType) type).getActualTypeArguments()).map(TypeInfo::new).collect(Collectors.toList());
    }

    public TypeInfo getWildcardBound() {
        if (!this.isWildcard())
            return null;
        WildcardType wildcard = (WildcardType) type;
        Type[] upper = wildcard.getUpperBounds();
        Type[] lower = wildcard.getLowerBounds();
        if (upper[0] != Object.class) {
            return new TypeInfo(upper[0]);
        }
        if (lower.length != 0)
            return new TypeInfo(lower[0]);
        return new TypeInfo(Object.class);
    }

    public String getTypeName() {
        if (!isVariable() && !isClazz())
            return null;
        return type.getTypeName();
    }

    public TypeInfo getComponent() {
        if (!this.isArray())
            return null;
        if (type instanceof Class<?>)
            return new TypeInfo(((Class<?>) type).getComponentType());
        return new TypeInfo(((GenericArrayType) type).getGenericComponentType());
    }

}
