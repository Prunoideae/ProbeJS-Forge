package com.prunoideae.probejs.formatter.formatter;

import com.prunoideae.probejs.formatter.NameResolver;
import com.prunoideae.probejs.info.TypeInfo;

import java.util.stream.Collectors;

public class FormatterType {
    private final TypeInfo typeInfo;

    public FormatterType(TypeInfo typeInfo) {
        this.typeInfo = typeInfo;
    }

    public String format(Integer indent, Integer stepIndent) {
        if (typeInfo.isClazz())
            return NameResolver.getResolvedName(typeInfo.getTypeName()).getFullName();
        if (typeInfo.isVariable())
            return typeInfo.getTypeName();
        if (typeInfo.isWildcard())
            return new FormatterType(typeInfo.getWildcardBound()).format(indent, stepIndent);
        if (typeInfo.isArray())
            return new FormatterType(typeInfo.getComponent()).format(indent, stepIndent) + "[]";
        if (typeInfo.isParameterized())
            return new FormatterType(new TypeInfo(typeInfo.getRawType())).format(indent, stepIndent) +
                    "<%s>".formatted(typeInfo
                            .getParameterizedInfo()
                            .stream()
                            .map(p -> new FormatterType(p).format(indent, stepIndent))
                            .collect(Collectors.joining(", ")));
        return "any";
    }
}
