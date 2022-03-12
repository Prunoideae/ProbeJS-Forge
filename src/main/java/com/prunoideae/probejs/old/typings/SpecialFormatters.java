package com.prunoideae.probejs.old.typings;

import com.google.gson.Gson;
import com.prunoideae.probejs.old.toucher.ClassInfo;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpecialFormatters {

    private static void putResolvedNames(String name, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            TSGlobalClassFormatter.resolvedClassName.put(clazz.getName(), name);
        }
    }

    private static void putTypeFormatter(Class<?> clazz, Function<ClassInfo.TypeInfo, String> transformer) {
        TSGlobalClassFormatter.specialTypeFormatter.put(clazz, transformer);
    }

    private static Function<ClassInfo.TypeInfo, String> generateTypedFunction(int paramCount, String returnType) {
        return typeInfo -> {
            List<Type> s = typeInfo.getTypeArguments();
            if (s.size() != paramCount)
                return TSGlobalClassFormatter.resolvedClassName.get(typeInfo.getTypeClass().getName());
            List<String> formatted = s.stream().map(TSGlobalClassFormatter::serializeType).collect(Collectors.toList());
            return "(%s) => %s".formatted(
                    IntStream.range(0, formatted.size())
                            .mapToObj(index -> "arg%d: %s".formatted(index, formatted.get(index)))
                            .collect(Collectors.joining(", ")),
                    returnType
            );
        };
    }

    private static Function<ClassInfo.TypeInfo, String> generateParamFunction(int paramCount) {
        return typeInfo -> {
            List<Type> s = typeInfo.getTypeArguments();
            if (s.size() != paramCount)
                return TSGlobalClassFormatter.resolvedClassName.get(typeInfo.getType().getTypeName());
            List<String> formatted = s.stream().map(TSGlobalClassFormatter::serializeType).collect(Collectors.toList());
            return "(%s) => %s".formatted(
                    IntStream.range(0, formatted.size() - 1)
                            .mapToObj(index -> "arg%d: %s".formatted(index, formatted.get(index)))
                            .collect(Collectors.joining(", ")),
                    formatted.get(formatted.size() - 1)
            );
        };
    }

    private static String formatMapKV(Object obj) {
        //Only Map<string, Object> is allowed
        //Others are discarded, if there are others
        Map<?, ?> map = (Map<?, ?>) obj;
        if (map.keySet().stream().allMatch(o -> o instanceof String || o instanceof Number)) {
            return "{%s}".formatted(map.entrySet()
                    .stream()
                    .map(entry -> {
                        if (TSGlobalClassFormatter.FieldFormatter.formatValue(entry.getValue()) != null) {
                            return "%s: %s".formatted(new Gson().toJson(entry.getKey()), TSGlobalClassFormatter.FieldFormatter.formatValue(entry.getValue()));
                        }
                        return "%s: %s".formatted(new Gson().toJson(entry.getKey()), new TSGlobalClassFormatter.TypeFormatter(new ClassInfo.TypeInfo(entry.getValue().getClass(), entry.getValue().getClass())).format());
                    })
                    .collect(Collectors.joining(", ")));
        } else {
            return null;
        }
    }

    private static void putStaticValueTransformer(Function<Object, String> transformer, Class<?>... types) {
        for (Class<?> type : types)
            TSGlobalClassFormatter.staticValueTransformer.put(type, transformer);
    }

    public static void init() {
        putTypeFormatter(BiConsumer.class, generateTypedFunction(2, "void"));
        putTypeFormatter(BiFunction.class, generateParamFunction(3));
        putTypeFormatter(BiPredicate.class, generateTypedFunction(2, "boolean"));
        putTypeFormatter(Consumer.class, generateTypedFunction(1, "void"));
        putTypeFormatter(Function.class, generateParamFunction(2));
        putTypeFormatter(Predicate.class, generateTypedFunction(1, "boolean"));
        putTypeFormatter(Supplier.class, generateParamFunction(1));

        putResolvedNames("boolean", Boolean.TYPE, Boolean.class);
        putResolvedNames("number", Byte.TYPE, Byte.class);
        putResolvedNames("string", Character.TYPE, Character.class);
        putResolvedNames("double", Double.TYPE, Double.class);
        putResolvedNames("float", Float.TYPE, Float.class);
        putResolvedNames("integer", Integer.TYPE, Integer.class);
        putResolvedNames("long", Long.TYPE, Long.class);
        putResolvedNames("short", Short.TYPE, Short.class);
        putResolvedNames("void", Void.TYPE, Void.class);
        putResolvedNames("string", String.class);
        putResolvedNames("object", Object.class);

        putStaticValueTransformer(
                Object::toString,
                Boolean.TYPE, Boolean.class,
                Double.TYPE, Double.class,
                Float.TYPE, Float.class,
                Integer.TYPE, Integer.class,
                Long.TYPE, Long.class,
                Short.TYPE, Short.class,
                Void.TYPE, Void.class);
        putStaticValueTransformer(o -> new Gson().toJson(((ResourceLocation) o).toString()), ResourceLocation.class);
        putStaticValueTransformer(SpecialFormatters::formatMapKV, HashMap.class, Map.class);
        putStaticValueTransformer(o -> new Gson().toJson(o), String.class, Character.TYPE, Character.class);
    }
}