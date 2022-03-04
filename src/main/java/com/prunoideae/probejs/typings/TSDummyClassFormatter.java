package com.prunoideae.probejs.typings;

import com.mojang.datafixers.util.Pair;
import com.prunoideae.probejs.toucher.ClassInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class TSDummyClassFormatter {
    public static class DummyMethodClassFormatter extends TSGlobalClassFormatter.ClassFormatter {
        private final List<Pair<String, TSGlobalClassFormatter.TypeFormatter>> pairs;
        private final String name;

        public DummyMethodClassFormatter(String name, List<Pair<String, TSGlobalClassFormatter.TypeFormatter>> pairs) {
            super(null, 0, 0, null, false);
            this.pairs = pairs;
            this.name = name;
        }

        @Override
        public String format() {
            List<String> innerLines = new ArrayList<>();
            innerLines.add(" ".repeat(this.indentation) + "class %s {".formatted(this.name));
            pairs.forEach(pair -> innerLines.add("%s%s(...args: object): %s".formatted(" ".repeat(this.indentation + this.stepIndentation), pair.getFirst(), pair.getSecond().format())));
            innerLines.add("}\n");
            return String.join("\n", innerLines);
        }
    }

    public static class DummyFieldClassFormatter extends TSGlobalClassFormatter.ClassFormatter {
        private final List<Pair<String, String>> pairs;
        private final String name;

        public DummyFieldClassFormatter(String name, List<Pair<String, String>> pairs) {
            super(null, 0, 0, null, false);
            this.pairs = pairs;
            this.name = name;
        }

        @Override
        public String format() {
            List<String> innerLines = new ArrayList<>();
            innerLines.add(" ".repeat(this.indentation) + "class %s {".formatted(this.name));
            pairs.forEach(pair -> innerLines.add("%s%s: %s".formatted(" ".repeat(this.indentation + this.stepIndentation), pair.getFirst(), pair.getSecond())));
            innerLines.add("}\n");
            return String.join("\n", innerLines);
        }
    }

    public static class RecipeEventJSFormatter extends TSGlobalClassFormatter.ClassFormatter {

        public RecipeEventJSFormatter(ClassInfo classInfo, Integer indentation, Integer stepIndentation, Predicate<String> namePredicate) {
            super(classInfo, indentation, stepIndentation, namePredicate, false);
        }

        public RecipeEventJSFormatter(ClassInfo classInfo) {
            super(classInfo, 0, 0, (s) -> true, false);
        }

        @Override
        protected List<String> compileFields(Set<String> usedMethod) {
            List<String> lines = super.compileFields(usedMethod);
            lines.add(" ".repeat(this.indentation + this.stepIndentation) + "recipes: stub.probejs.RecipeHolder;");
            return lines;
        }
    }
}
