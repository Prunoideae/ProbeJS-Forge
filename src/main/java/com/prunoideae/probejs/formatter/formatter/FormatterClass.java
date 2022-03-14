package com.prunoideae.probejs.formatter.formatter;

import com.prunoideae.probejs.document.DocumentClass;
import com.prunoideae.probejs.document.DocumentComment;
import com.prunoideae.probejs.document.DocumentField;
import com.prunoideae.probejs.document.DocumentMethod;
import com.prunoideae.probejs.document.comment.special.CommentHidden;
import com.prunoideae.probejs.formatter.NameResolver;
import com.prunoideae.probejs.info.ClassInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FormatterClass extends DocumentedFormatter<DocumentClass> implements IFormatter {
    private final ClassInfo classInfo;
    private final Map<String, FormatterField> fieldFormatters = new HashMap<>();
    private final Map<String, List<FormatterMethod>> methodFormatters = new HashMap<>();
    private final List<DocumentField> fieldAdditions = new ArrayList<>();
    private final List<DocumentMethod> methodAdditions = new ArrayList<>();

    public FormatterClass(ClassInfo classInfo) {
        this.classInfo = classInfo;
        classInfo.getMethodInfo().forEach(methodInfo -> methodFormatters.computeIfAbsent(methodInfo.getName(), s -> new ArrayList<>()).add(new FormatterMethod(methodInfo)));
        classInfo.getFieldInfo().forEach(fieldInfo -> fieldFormatters.put(fieldInfo.getName(), new FormatterField(fieldInfo)));
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<String> formatted = new ArrayList<>();
        DocumentComment comment = document.getComment();
        if (comment != null) {
            if (comment.getSpecialComment(CommentHidden.class) != null)
                return formatted;
            formatted.addAll(comment.format(indent, stepIndent));
        }

        // First line
        List<String> firstLine = new ArrayList<>();
        if (classInfo.isInterface())
            firstLine.add("interface");
        else
            firstLine.add("class");

        firstLine.add(NameResolver.getResolvedName(classInfo.getClazz().getName()).getLastName());
        if (classInfo.getSuperClass() != null) {
            firstLine.add("extends");
            firstLine.add(NameResolver.getResolvedName(classInfo.getSuperClass().getClazz().getName()).getFullName());
        }
        if (!classInfo.getInterfaces().isEmpty()) {
            firstLine.add("implements");
            firstLine.add("%s".formatted(classInfo.getInterfaces().stream().map(i -> NameResolver.getResolvedName(i.getClazz().getName()).getFullName()).collect(Collectors.joining(", "))));
        }
        firstLine.add("{");
        formatted.add(" ".repeat(indent) + String.join(" ", firstLine));
        // Fields, methods
        methodFormatters.values().forEach(m -> m.forEach(mf -> formatted.addAll(mf.format(indent + stepIndent, stepIndent))));
        fieldFormatters.entrySet().stream().filter(e -> !methodFormatters.containsKey(e.getKey())).forEach(f -> formatted.addAll(f.getValue().format(indent + stepIndent, stepIndent)));

        // beans

        // additions
        fieldAdditions.forEach(fieldDoc -> formatted.addAll(fieldDoc.format(indent + stepIndent, stepIndent)));
        methodAdditions.forEach(methodDoc -> formatted.addAll(methodDoc.format(indent + stepIndent, stepIndent)));

        formatted.add(" ".repeat(indent) + "}");
        return formatted;
    }

    @Override
    public void setDocument(DocumentClass document) {
        super.setDocument(document);
        document.getFields().forEach(documentField -> {
            if (fieldFormatters.containsKey(documentField.getName()))
                fieldFormatters.get(documentField.getName()).setDocument(documentField);
            else
                fieldAdditions.add(documentField);
        });

        document.getMethods().forEach(documentMethod -> {
            if (methodFormatters.containsKey(documentMethod.getName()))
                methodFormatters.get(documentMethod.getName()).forEach(formatterMethod -> {
                    if (documentMethod.testMethod(formatterMethod.getMethodInfo()))
                        formatterMethod.setDocument(documentMethod);
                });
            else
                methodAdditions.add(documentMethod);
        });
    }
}
