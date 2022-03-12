package com.prunoideae.probejs.document;

import java.util.ArrayList;
import java.util.List;

public class ClassDocument {
    private CommentDocument document;
    private List<FieldDocument> fields = new ArrayList<>();
    private List<MethodDocument> methods = new ArrayList<>();

    public void setDocument(CommentDocument document) {
        this.document = document;
    }

    public CommentDocument getDocument() {
        return document;
    }

    public void addField(FieldDocument field) {
        this.fields.add(field);
    }

    public List<FieldDocument> getFields() {
        return fields;
    }

    public void addMethod(MethodDocument method) {
        this.methods.add(method);
    }

    public List<MethodDocument> getMethods() {
        return methods;
    }
}
