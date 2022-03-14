package com.prunoideae.probejs.document.type;

import com.prunoideae.probejs.formatter.NameResolver;

public class TypeNamed implements IType {
    private final String typeName;

    public TypeNamed(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String getTypeName() {
        return NameResolver.resolvedNames.getOrDefault(typeName, typeName);
    }
}
