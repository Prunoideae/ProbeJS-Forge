package com.prunoideae.probejs.old.resolver.document.part;

public class PartTarget {
    public String targetName;

    public PartTarget(String line) {
        targetName = line.substring(9, line.length() - 2);
    }
}
