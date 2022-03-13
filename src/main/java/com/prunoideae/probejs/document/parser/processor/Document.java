package com.prunoideae.probejs.document.parser.processor;

import com.prunoideae.probejs.document.parser.handler.AbstractStackedMachine;

public class Document extends AbstractStackedMachine<String> {
    private final DocumentHandler document;

    public Document() {
        document = new DocumentHandler();
        this.stack.add(document);
    }

    public DocumentHandler getDocument() {
        return document;
    }
}
