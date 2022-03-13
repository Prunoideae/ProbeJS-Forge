package com.prunoideae.probejs.document.parser.processor;

import com.prunoideae.probejs.document.DocumentClass;
import com.prunoideae.probejs.document.parser.handler.IStateHandler;

import java.util.List;

public class ProviderClass implements IStateHandler<String>, IDocumentProvider<DocumentClass> {
    @Override
    public void trial(String element, List<IStateHandler<String>> stack) {

    }

    @Override
    public DocumentClass provide() {
        return null;
    }
}
