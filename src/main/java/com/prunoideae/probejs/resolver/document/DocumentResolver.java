package com.prunoideae.probejs.resolver.document;

import com.prunoideae.probejs.resolver.handler.AbstractStackedMachine;

public class DocumentResolver extends AbstractStackedMachine<String> {
    private final Document document;

    public DocumentResolver() {
        this.document = new Document();
        this.stack.add(document);
    }

    public Document getDocument() {
        return document;
    }
}
