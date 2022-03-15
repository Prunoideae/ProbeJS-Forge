package com.prunoideae.probejs.formatter.formatter;

import com.prunoideae.probejs.formatter.NameResolver;
import com.prunoideae.probejs.info.TypeInfo;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FormatterType {
    private final TypeInfo typeInfo;
    private final boolean useSpecial;

    public FormatterType(TypeInfo typeInfo, boolean useSpecial) {
        this.typeInfo = typeInfo;
        this.useSpecial = useSpecial;
    }

    public FormatterType(TypeInfo typeInfo) {
        this(typeInfo, true);
    }

    public String format(Integer indent, Integer stepIndent) {
        if (useSpecial) {
            Class<?> rawClass = (Class<?>) typeInfo.getRawType();
            if (NameResolver.specialTypeFormatters.containsKey(rawClass)) {
                return NameResolver.specialTypeFormatters.get(rawClass).apply(this.typeInfo);
            } else {
                for (Map.Entry<Class<?>, Function<TypeInfo, String>> entry : NameResolver.specialTypeFormatters.entrySet()) {
                    Class<?> clazz = entry.getKey();
                    Function<TypeInfo, String> formatter = entry.getValue();
                    if (clazz.isAssignableFrom(rawClass))
                        return formatter.apply(typeInfo);
                }
            }
        }

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
