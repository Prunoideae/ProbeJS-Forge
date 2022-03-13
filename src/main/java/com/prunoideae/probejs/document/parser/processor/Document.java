package com.prunoideae.probejs.document.parser.processor;

import com.prunoideae.probejs.document.parser.handler.AbstractStackedMachine;

public class Document extends AbstractStackedMachine<String> {
    public Document() {
        this.stack.add(new DocumentHandler());
    }
}
