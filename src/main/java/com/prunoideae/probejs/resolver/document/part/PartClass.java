package com.prunoideae.probejs.resolver.document.part;

import com.prunoideae.probejs.resolver.document.Document;
import com.prunoideae.probejs.resolver.handler.IStateHandler;

import java.util.ArrayList;
import java.util.List;

public class PartClass implements IStateHandler<String> {
    private final List<Object> classComponents = new ArrayList<>();
    private final String name;

    public PartClass(String line) {
        String[] components = line.split(" ");
        if (components.length != 3)
            throw new RuntimeException("Wrongly formatted class, line: %s".formatted(line));
        this.name = components[1];
    }

    @Override
    public void trial(String element, List<IStateHandler<String>> stack) {
        //When at the end of string, remove everything
        if (Document.removeBlank(element).equals("}")) {
            stack.remove(this);
            return;
        }

        if (element.contains("/**")) {
            ComponentComment comment = new ComponentComment(element);
            this.classComponents.add(comment);
            stack.add(comment);
        } else if (!Document.removeBlank(element).isEmpty()) {
            element = Document.removeBlank(element);
            this.classComponents.add(new ComponentProperty(element));
        }
    }

    public List<Object> getClassComponents() {
        return classComponents;
    }

    public String getName() {
        return name;
    }
}
