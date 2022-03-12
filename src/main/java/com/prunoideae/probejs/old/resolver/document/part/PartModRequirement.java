package com.prunoideae.probejs.old.resolver.document.part;

public class PartModRequirement {
    public String targetName;

    public PartModRequirement(String line) {
        targetName = line.substring(6, line.length() - 2);
    }
}
