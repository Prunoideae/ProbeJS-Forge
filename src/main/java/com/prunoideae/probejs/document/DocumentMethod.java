package com.prunoideae.probejs.document;

import com.prunoideae.probejs.document.parser.processor.IDocumentProvider;
import com.prunoideae.probejs.document.type.IType;
import com.prunoideae.probejs.document.type.Resolver;
import com.prunoideae.probejs.util.StringUtil;

import java.util.List;
import java.util.stream.Collectors;

public class DocumentMethod extends DocumentProperty implements IDocumentProvider<DocumentMethod> {
    @Override
    public DocumentMethod provide() {
        return this;
    }

    private static class DocumentParam {
        private final String name;
        private final IType type;

        private DocumentParam(String name, IType type) {
            this.name = name;
            this.type = type;
        }

        public IType getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }

    private final boolean isStatic;
    private final String name;
    private final IType returnType;
    private final List<DocumentParam> params;

    public DocumentMethod(String line) {
        line = line.strip();
        if (line.startsWith("static")) {
            line = line.substring(6).strip();
            isStatic = true;
        } else {
            isStatic = false;
        }
        System.out.println(line);
        int nameIndex = line.indexOf("(");
        int methodIndex = line.indexOf(")");

        name = line.substring(0, nameIndex).strip();
        String paramsString = line.substring(nameIndex + 1, methodIndex);
        String remainedString = line.substring(methodIndex + 1).replace(":", "").strip();
        params = StringUtil.splitLayer(paramsString, "<", ">", ",")
                .stream()
                .map(String::strip)
                .filter(s -> !s.isEmpty()).map(s -> {
                    String[] nameType = s.split(":");
                    return new DocumentParam(nameType[0].strip(), Resolver.resolveType(nameType[1]));
                })
                .collect(Collectors.toList());
        returnType = Resolver.resolveType(remainedString);
    }

    public boolean isStatic() {
        return isStatic;
    }

    public String getName() {
        return name;
    }

    public IType getReturnType() {
        return returnType;
    }

    public List<DocumentParam> getParams() {
        return params;
    }
}
