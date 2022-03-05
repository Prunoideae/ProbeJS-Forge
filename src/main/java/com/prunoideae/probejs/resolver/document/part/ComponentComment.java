package com.prunoideae.probejs.resolver.document.part;

import com.prunoideae.probejs.resolver.document.Document;
import com.prunoideae.probejs.resolver.handler.IStateHandler;

import java.util.ArrayList;
import java.util.List;

public class ComponentComment implements IStateHandler<String> {
    private final List<String> comments = new ArrayList<>();

    public ComponentComment(String line) {
        ;
        comments.add(Document.removeBlank(line));
    }


    @Override
    public void trial(String element, List<IStateHandler<String>> stack) {
        comments.add(Document.removeBlank(element));
        if (element.contains("*/")) {
            stack.remove(this);
        }
    }

    public List<String> getComments() {
        return comments;
    }
}
