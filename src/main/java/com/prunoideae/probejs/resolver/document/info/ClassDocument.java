package com.prunoideae.probejs.resolver.document.info;

import com.prunoideae.probejs.resolver.document.part.ComponentComment;
import com.prunoideae.probejs.resolver.document.part.ComponentProperty;
import com.prunoideae.probejs.resolver.document.part.PartClass;

import java.util.ArrayList;
import java.util.List;

public class ClassDocument {
    private String target = null;
    private final String name;
    private CommentDocument comment = null;
    private final List<FieldDocument> fieldDocuments = new ArrayList<>();
    private final List<MethodDocument> methodDocuments = new ArrayList<>();

    public ClassDocument(PartClass part) {
        name = part.getName();
        ComponentComment comment = null;
        for (Object o : part.getClassComponents()) {
            if (o instanceof ComponentComment) {
                comment = (ComponentComment) o;
            } else if (o instanceof ComponentProperty) {
                if (isMethod(((ComponentProperty) o).getProperty())) {
                    MethodDocument method = new MethodDocument(((ComponentProperty) o).getProperty());
                    if (comment != null)
                        method.setComment(new CommentDocument(comment));
                    methodDocuments.add(method);
                } else {
                    FieldDocument field = new FieldDocument(((ComponentProperty) o).getProperty());
                    if (comment != null)
                        field.setComment(new CommentDocument(comment));
                    fieldDocuments.add(field);
                }
                comment = null;
            }
        }

    }

    public void setComment(CommentDocument comment) {
        this.comment = comment;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    /**
     * Returns if the property is a method,
     * if false then the property is a field.
     *
     * @param s {String}
     * @return {boolean}
     */
    private static boolean isMethod(String s) {
        if (!s.contains("("))
            return false;
        return s.indexOf("(") < s.indexOf(":");
    }

    @Override
    public String toString() {
        return "ClassDocument{" +
                "target='" + target + '\'' +
                ", comment=" + comment +
                ", fieldDocuments=" + fieldDocuments +
                ", methodDocuments=" + methodDocuments +
                ", name=" + name +
                '}';
    }

    public CommentDocument getComment() {
        return comment;
    }

    public String getName() {
        return name;
    }

    public List<FieldDocument> getFieldDocuments() {
        return fieldDocuments;
    }

    public List<MethodDocument> getMethodDocuments() {
        return methodDocuments;
    }
}
