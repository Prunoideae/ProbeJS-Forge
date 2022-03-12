package com.prunoideae.probejs.old.resolver.document.info;

import com.prunoideae.probejs.old.resolver.document.part.PartTypeDecl;

import java.util.List;

public class ClassDocuments {
    private final List<ClassDocument> classDocuments;
    private final List<PartTypeDecl> typeDeclDocuments;

    public ClassDocuments(List<ClassDocument> classDocuments, List<PartTypeDecl> typeDeclDocuments) {
        this.classDocuments = classDocuments;
        this.typeDeclDocuments = typeDeclDocuments;
    }

    public List<ClassDocument> getClassDocuments() {
        return classDocuments;
    }

    public List<PartTypeDecl> getTypeDeclDocuments() {
        return typeDeclDocuments;
    }
}
