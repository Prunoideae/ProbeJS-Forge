package com.prunoideae.probejs.document;

import com.prunoideae.probejs.document.parser.processor.IDocumentProvider;
import com.prunoideae.probejs.document.type.IType;
import com.prunoideae.probejs.document.type.Resolver;

public class DocumentField extends DocumentProperty implements IDocumentProvider<DocumentField> {
    private final boolean isFinal;
    private final boolean isStatic;
    private final String name;
    private final IType type;

    public DocumentField(String line) {
        line = line.strip();
        boolean f = false;
        boolean s = false;
        boolean flag = true;
        while (flag) {
            if (line.startsWith("readonly")) {
                line = line.substring(8).strip();
                f = true;
            } else if (line.startsWith("static")) {
                line = line.substring(6).strip();
                s = true;
            } else {
                flag = false;
            }
        }
        String[] parts = line.split(":");
        name = parts[0].strip();
        type = Resolver.resolveType(parts[1].strip());

        isFinal = f;
        isStatic = s;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public String getName() {
        return name;
    }

    public IType getType() {
        return type;
    }

    @Override
    public DocumentField provide() {
        return this;
    }
}
