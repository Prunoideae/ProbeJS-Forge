package com.prunoideae.probejs.document.parser.processor;

import com.prunoideae.probejs.document.DocumentComment;
import com.prunoideae.probejs.document.parser.handler.IStateHandler;

import java.util.ArrayList;
import java.util.List;

public class ProviderComment implements IStateHandler<String>, IDocumentProvider<DocumentComment> {
    private final List<String> comments = new ArrayList<>();

    @Override
    public void trial(String element, List<IStateHandler<String>> stack) {
        comments.add(element);
        if (element.strip().endsWith("*/"))
            stack.remove(this);
    }

    @Override
    public DocumentComment provide() {
        return new DocumentComment(comments);
    }
}
