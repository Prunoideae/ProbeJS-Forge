package com.prunoideae.probejs.formatter;

import com.prunoideae.probejs.formatter.formatter.FormatterType;

import java.util.function.*;

public class SpecialTypes {
    public static void init() {
        NameResolver.putTypeFormatter(Consumer.class, t -> {
            if (t.isParameterized() && t.getParameterizedInfo().size() == 1) {
                String inner = new FormatterType(t.getParameterizedInfo().get(0)).format(0, 0);
                return "(arg0: %s) => void".formatted(inner);
            }
            return new FormatterType(t, false).format(0, 0);
        });
        NameResolver.putTypeFormatter(Predicate.class, t -> {
            if (t.isParameterized() && t.getParameterizedInfo().size() == 1) {
                String inner = new FormatterType(t.getParameterizedInfo().get(0)).format(0, 0);
                return "(arg0: %s) => boolean".formatted(inner);
            }
            return new FormatterType(t, false).format(0, 0);
        });
        NameResolver.putTypeFormatter(Supplier.class, t -> {
            if (t.isParameterized() && t.getParameterizedInfo().size() == 1) {
                String inner = new FormatterType(t.getParameterizedInfo().get(0)).format(0, 0);
                return "() => %s".formatted(inner);
            }
            return new FormatterType(t, false).format(0, 0);
        });
        NameResolver.putTypeFormatter(Function.class, t -> {
            if (t.isParameterized() && t.getParameterizedInfo().size() == 2) {
                String first = new FormatterType(t.getParameterizedInfo().get(0)).format(0, 0);
                String second = new FormatterType(t.getParameterizedInfo().get(1)).format(0, 0);
                return "(arg0: %s) => %s".formatted(first, second);
            }
            return new FormatterType(t, false).format(0, 0);
        });
        NameResolver.putTypeFormatter(BiConsumer.class, t -> {
            if (t.isParameterized() && t.getParameterizedInfo().size() == 2) {
                String first = new FormatterType(t.getParameterizedInfo().get(0)).format(0, 0);
                String second = new FormatterType(t.getParameterizedInfo().get(1)).format(0, 0);
                return "(arg0: %s, arg1: %s) => void".formatted(first, second);
            }
            return new FormatterType(t, false).format(0, 0);
        });
        NameResolver.putTypeFormatter(BiFunction.class, t -> {
            if (t.isParameterized() && t.getParameterizedInfo().size() == 3) {
                String first = new FormatterType(t.getParameterizedInfo().get(0)).format(0, 0);
                String second = new FormatterType(t.getParameterizedInfo().get(1)).format(0, 0);
                String third = new FormatterType(t.getParameterizedInfo().get(2)).format(0, 0);
                return "(arg0: %s, arg1: %s) => %s".formatted(first, second, third);
            }
            return new FormatterType(t, false).format(0, 0);
        });
    }
}
