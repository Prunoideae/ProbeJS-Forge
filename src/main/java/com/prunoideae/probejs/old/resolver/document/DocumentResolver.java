package com.prunoideae.probejs.old.resolver.document;

import com.prunoideae.probejs.document.parser.handler.AbstractStackedMachine;

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
