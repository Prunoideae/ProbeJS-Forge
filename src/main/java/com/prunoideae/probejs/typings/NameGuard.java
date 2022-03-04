package com.prunoideae.probejs.typings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class NameGuard {
    private static final Set<String> keywords = new HashSet<>(Arrays.asList("with", "in", "function", "java", "debugger"));

    public static String getSafeName(String name) {
        return keywords.contains(name) ? "_" + name : name;
    }

    public static String compileClasspath(String[] path) {
        return Arrays.stream(path).map(name -> keywords.contains(name) ? "_" + name : name).collect(Collectors.joining("."));
    }

}
