package com.prunoideae.probejs.document;

import java.util.ArrayList;
import java.util.List;

public class DocumentClass implements IConcrete {
    private DocumentComment document;
    private final List<DocumentField> fields = new ArrayList<>();
    private final List<DocumentMethod> methods = new ArrayList<>();
    private final List<IDecorative> decorates = new ArrayList<>();

    public void setDocument(DocumentComment document) {
        this.document = document;
    }

    public DocumentComment getDocument() {
        return document;
    }

    public void addField(DocumentField field) {
        this.fields.add(field);
    }

    public List<DocumentField> getFields() {
        return fields;
    }

    public void addMethod(DocumentMethod method) {
        this.methods.add(method);
    }

    public List<DocumentMethod> getMethods() {
        return methods;
    }

    @Override
    public void acceptDeco(List<IDecorative> decorates) {
        this.decorates.addAll(decorates);
    }
}
