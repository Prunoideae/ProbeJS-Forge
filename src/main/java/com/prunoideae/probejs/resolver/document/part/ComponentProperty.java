package com.prunoideae.probejs.resolver.document.part;

public class ComponentProperty {
    private String property;

    public ComponentProperty(String line) {
        this.property = line;
    }

    public String getProperty() {
        return property;
    }
}
