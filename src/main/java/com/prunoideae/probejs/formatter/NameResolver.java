package com.prunoideae.probejs.formatter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.function.Function;

public class NameResolver {
    public static final HashMap<String, String> resolvedNames = new HashMap<>();
    public static final HashMap<String, Function<Type, String>> specialTypeFormatters = new HashMap<>();
}
