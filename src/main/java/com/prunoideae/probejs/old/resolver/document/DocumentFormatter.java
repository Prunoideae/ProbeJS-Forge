package com.prunoideae.probejs.old.resolver.document;

import com.prunoideae.probejs.old.resolver.document.info.ClassDocument;
import com.prunoideae.probejs.old.resolver.document.info.FieldDocument;
import com.prunoideae.probejs.old.resolver.document.info.MethodDocument;
import com.prunoideae.probejs.old.resolver.document.part.PartTypeDecl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentFormatter {
    private final HashMap<String, List<ClassDocument>> documents;
    private final int indent;
    private final int stepIndent;

    public DocumentFormatter(HashMap<String, List<ClassDocument>> documents, int indent, int stepIndent) {
        this.documents = documents;
        this.indent = indent;
        this.stepIndent = stepIndent;
    }

    protected List<String> formatMethods(List<ClassDocument> methodDocuments) {
        List<String> lines = new ArrayList<>();
        HashMap<String, MethodDocument> resolvedDocuments = new HashMap<>();
        methodDocuments.forEach(document -> document.getMethodDocuments().forEach(method -> resolvedDocuments.put(method.getName() + String.join(",", method.getParams().values()), method)));
        resolvedDocuments.forEach((nameParam, document) -> {
            if (document.getCommentDocument() != null)
                lines.addAll(document.getCommentDocument().getCommentText(indent + stepIndent));
            String paramsString = document.getParams().entrySet().stream().map(e -> e.getKey() + ": " + Document.formatType(e.getValue())).collect(Collectors.joining(", "));
            String method = "%s(%s): %s".formatted(document.getName(), paramsString, Document.formatType(document.getReturnType()));
            if (document.isStatic())
                method = "static " + method;
            method = " ".repeat(indent + stepIndent) + method;
            lines.add(method);
        });
        return lines;
    }

    protected List<String> formatFields(List<ClassDocument> fieldDocuments) {
        List<String> lines = new ArrayList<>();
        HashMap<String, FieldDocument> resolvedFields = new HashMap<>();
        fieldDocuments.forEach(document -> document.getFieldDocuments().forEach(field -> resolvedFields.put(field.getName(), field)));
        resolvedFields.forEach((name, field) -> {
            lines.addAll(field.format(indent + stepIndent));
        });
        return lines;
    }

    public List<String> formatClass(String name, List<ClassDocument> classDocuments) {
        List<String> lines = new ArrayList<>();
        lines.add(" ".repeat(indent) + "class %s {".formatted(name));
        lines.addAll(formatFields(classDocuments));
        lines.addAll(formatMethods(classDocuments));
        lines.add(" ".repeat(indent) + "}");
        return lines;
    }

    public List<String> format() {
        List<String> lines = new ArrayList<>();
        lines.add("declare namespace Document {");
        documents.forEach((name, docs) -> lines.addAll(formatClass(name, docs)));
        lines.add("}");
        HashMap<String, PartTypeDecl> types = new HashMap<>();
        documents.forEach((name, docs) -> {

        });
        return lines;
    }
}
