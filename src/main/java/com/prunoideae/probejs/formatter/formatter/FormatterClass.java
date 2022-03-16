package com.prunoideae.probejs.formatter.formatter;

import com.prunoideae.probejs.ProbeConfig;
import com.prunoideae.probejs.document.DocumentClass;
import com.prunoideae.probejs.document.DocumentComment;
import com.prunoideae.probejs.document.DocumentField;
import com.prunoideae.probejs.document.DocumentMethod;
import com.prunoideae.probejs.document.comment.special.CommentHidden;
import com.prunoideae.probejs.formatter.NameResolver;
import com.prunoideae.probejs.info.ClassInfo;
import com.prunoideae.probejs.info.TypeInfo;

import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;

public class FormatterClass extends DocumentedFormatter<DocumentClass> implements IFormatter {
    private final ClassInfo classInfo;
    private final Map<String, FormatterField> fieldFormatters = new HashMap<>();
    private final Map<String, List<FormatterMethod>> methodFormatters = new HashMap<>();
    private final List<DocumentField> fieldAdditions = new ArrayList<>();
    private final List<DocumentMethod> methodAdditions = new ArrayList<>();
    private boolean internal = false;


    public FormatterClass(ClassInfo classInfo) {
        this.classInfo = classInfo;
        classInfo.getMethodInfo().forEach(methodInfo -> methodFormatters.computeIfAbsent(methodInfo.getName(), s -> new ArrayList<>()).add(new FormatterMethod(methodInfo)));
        classInfo.getFieldInfo().forEach(fieldInfo -> fieldFormatters.put(fieldInfo.getName(), new FormatterField(fieldInfo)));
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<String> formatted = new ArrayList<>();
        DocumentComment comment = document == null ? null : document.getComment();
        if (comment != null) {
            if (comment.getSpecialComment(CommentHidden.class) != null)
                return formatted;
            formatted.addAll(comment.format(indent, stepIndent));
        }

        // First line
        List<String> firstLine = new ArrayList<>();

        if (!internal)
            firstLine.add("declare");

        if (classInfo.isAbstract())
            firstLine.add("abstract");

        if (classInfo.isInterface())
            firstLine.add("interface");
        else
            firstLine.add("class");

        firstLine.add(NameResolver.getResolvedName(classInfo.getClazz().getName()).getLastName());
        if (classInfo.getClazz().getTypeParameters().length != 0) {
            firstLine.add("<%s>".formatted(Arrays.stream(classInfo.getClazz().getTypeParameters()).map(TypeVariable::getName).collect(Collectors.joining(", "))));
        }
        if (classInfo.getSuperClass() != null) {
            firstLine.add("extends");
            firstLine.add(new FormatterType(new TypeInfo(classInfo.getClazz().getGenericSuperclass()), false).format(0, 0));
        }
        if (!classInfo.getInterfaces().isEmpty()) {
            firstLine.add("implements");
            firstLine.add("%s".formatted(Arrays.stream(classInfo.getClazz().getGenericInterfaces()).map(TypeInfo::new).map(i -> new FormatterType(i, false)).map(f -> f.format(0, 0)).collect(Collectors.joining(", "))));
        }
        firstLine.add("{");
        formatted.add(" ".repeat(indent) + String.join(" ", firstLine));

        // Fields, methods
        methodFormatters.values().forEach(m -> m.stream().filter(mf -> ProbeConfig.INSTANCE.dumpMethod || (mf.getBean() == null || fieldFormatters.containsKey(mf.getBean()) || methodFormatters.containsKey(mf.getBean()))).forEach(mf -> {
            if (classInfo.isInterface() && mf.getMethodInfo().isStatic() && internal)
                return;
            mf.setInterface(classInfo.isInterface());
            formatted.addAll(mf.format(indent + stepIndent, stepIndent));
        }));
        fieldFormatters.entrySet().stream().filter(e -> !methodFormatters.containsKey(e.getKey())).forEach(f -> {
            if (classInfo.isInterface() && f.getValue().getFieldInfo().isStatic() && internal)
                return;
            f.getValue().setInterface(classInfo.isInterface());
            formatted.addAll(f.getValue().format(indent + stepIndent, stepIndent));
        });

        // beans
        methodFormatters.values().forEach(ml -> ml.forEach(m -> {
            String beanName = m.getBean();
            if (beanName != null && Character.isAlphabetic(beanName.charAt(0)))
                if (!fieldFormatters.containsKey(beanName) && !methodFormatters.containsKey(beanName))
                    formatted.addAll(m.formatBean(indent + stepIndent, stepIndent));
        }));

        // constructors
        if (internal) {
            formatted.add(" ".repeat(indent + stepIndent) + "/**");
            formatted.add(" ".repeat(indent + stepIndent) + "* Internal constructor, this means that it's not valid and you will get an error if you use it.");
            formatted.add(" ".repeat(indent + stepIndent) + "*/");
            formatted.add(" ".repeat(indent + stepIndent) + "private constructor();");
        } else {
            classInfo.getConstructorInfo().stream().map(FormatterConstructor::new).forEach(f -> formatted.addAll(f.format(indent + stepIndent, stepIndent)));
        }
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
