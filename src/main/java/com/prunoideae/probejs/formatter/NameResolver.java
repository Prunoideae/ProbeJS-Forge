package com.prunoideae.probejs.formatter;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

public class NameResolver {
    public static class ResolvedName {
        public static final ResolvedName UNRESOLVED = new ResolvedName(List.of("unknown"));
        private final List<String> names;

        private ResolvedName(List<String> names) {
            this.names = names;
        }

        public String getFullName() {
            return String.join(".", names);
        }

        public String getNamespace() {
            return String.join(".", names.subList(0, names.size() - 1));
        }

        public String getLastName() {
            return names.get(names.size() - 1);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResolvedName that = (ResolvedName) o;
            return names.equals(that.names);
        }

        @Override
        public int hashCode() {
            return Objects.hash(names);
        }

        @Override
        public String toString() {
            return "ResolvedName{" +
                    "names=" + names +
                    '}';
        }
    }

    public static final HashMap<String, ResolvedName> resolvedNames = new HashMap<>();
    public static final HashMap<String, Function<Type, String>> specialTypeFormatters = new HashMap<>();

    public static void putResolvedName(String className, String resolvedName) {
        putResolvedName(className, new ResolvedName(Arrays.stream(resolvedName.split("\\.")).toList()));
    }

    public static void putResolvedName(String className, ResolvedName resolvedName) {
        if (!resolvedNames.containsKey(className))
            resolvedNames.put(className, resolvedName);
    }

    public static ResolvedName getResolvedName(String className) {
        return resolvedNames.getOrDefault(className, ResolvedName.UNRESOLVED);
    }

    public static void putTypeFormatter(String className, Function<Type, String> formatter) {
        specialTypeFormatters.put(className, formatter);
    }

    public static void resolveNames(Set<Class<?>> classes) {
        Set<ResolvedName> usedNames = new HashSet<>(resolvedNames.values());
        for (Class<?> clazz : classes) {
            ResolvedName resolved = new ResolvedName(Arrays.stream(clazz.getName().split("\\.")).toList());
            ResolvedName internal = new ResolvedName(List.of("Internal", resolved.getLastName()));
            if (usedNames.contains(internal))
                putResolvedName(clazz.getName(), resolved);
            else {
                putResolvedName(clazz.getName(), internal);
                usedNames.add(internal);
            }
        }
    }
}
