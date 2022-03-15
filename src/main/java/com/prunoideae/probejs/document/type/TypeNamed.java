package com.prunoideae.probejs.document.type;

import com.prunoideae.probejs.formatter.NameResolver;

public class TypeNamed implements IType {
    private final String typeName;

    public TypeNamed(String typeName) {
        this.typeName = typeName;
    }

    public String getRawTypeName() {
        return typeName;
    }

    @Override
    public String getTypeName() {
        NameResolver.ResolvedName resolved = NameResolver.resolvedNames.get(typeName);
        if (resolved == null)
            return typeName;
        return resolved.getFullName();
    }
}
