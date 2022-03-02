package com.prunoideae.probejs.typings;

import com.google.gson.Gson;
import com.prunoideae.probejs.toucher.ClassInfo;
import com.prunoideae.probejs.toucher.ClassToucher;
import dev.latvian.mods.kubejs.recipe.RecipeEventJS;
import net.minecraft.nbt.CompoundTag;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpecialFormatters {

    private static void putResolvedNames(String name, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            TSGlobalClassFormatter.resolvedClassName.put(clazz, name);
        }
    }

    private static void putTypeFormatter(Class<?> clazz, Function<ClassInfo.TypeInfo, String> transformer) {
        TSGlobalClassFormatter.specialTypeFormatter.put(clazz, transformer);
    }

    private static Function<ClassInfo.TypeInfo, String> generateTypedFunction(int paramCount, String returnType) {
        return typeInfo -> {
            List<Type> s = typeInfo.getTypeArguments();
            if (s.size() != paramCount)
                return TSGlobalClassFormatter.resolvedClassName.get(typeInfo.getTypeClass());
            List<String> formatted = s.stream().map(arg -> {
                if (ClassToucher.isClassName(arg.getTypeName())) {
                    try {
                        return new TSGlobalClassFormatter.TypeFormatter(new ClassInfo.TypeInfo(arg, Class.forName(arg.getTypeName()))).format();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return "any";
            }).collect(Collectors.toList());
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
                return TSGlobalClassFormatter.resolvedClassName.get(typeInfo.getTypeClass());
            List<String> formatted = s.stream().map(arg -> {
                if (ClassToucher.isClassName(arg.getTypeName())) {
                    try {
                        return new TSGlobalClassFormatter.TypeFormatter(new ClassInfo.TypeInfo(arg, Class.forName(arg.getTypeName()))).format();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return "any";
            }).collect(Collectors.toList());
            return "(%s) => %s".formatted(
                    IntStream.range(0, formatted.size() - 1)
                            .mapToObj(index -> "arg%d: %s".formatted(index, formatted.get(index)))
                            .collect(Collectors.joining(", ")),
                    formatted.get(formatted.size() - 1)
            );
        };
    }

    private static void putStaticValueTransformer(Function<Object, String> transformer, Class<?>... types) {
        for (Class<?> type : types)
            TSGlobalClassFormatter.staticValueTransformer.put(type, transformer);
    }

    public static void init() {
        TSGlobalClassFormatter.specialClassFormatter.put(RecipeEventJS.class, TSDummyClassFormatter.RecipeEventJSFormatter.class);

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
        putResolvedNames("number", Double.TYPE, Double.class);
        putResolvedNames("number", Float.TYPE, Float.class);
        putResolvedNames("number", Integer.TYPE, Integer.class);
        putResolvedNames("number", Long.TYPE, Long.class);
        putResolvedNames("number", Short.TYPE, Short.class);
        putResolvedNames("void", Void.TYPE, Void.class);
        putResolvedNames("string", String.class);
        putResolvedNames("object", Object.class);
        putResolvedNames("object", CompoundTag.class);

        putStaticValueTransformer(
                Object::toString,
                Boolean.TYPE, Boolean.class,
                Double.TYPE, Double.class,
                Float.TYPE, Float.class,
                Integer.TYPE, Integer.class,
                Long.TYPE, Long.class,
                Short.TYPE, Short.class,
                Void.TYPE, Void.class);
        putStaticValueTransformer(o -> new Gson().toJson(o), String.class, Character.TYPE, Character.class);
    }
}
