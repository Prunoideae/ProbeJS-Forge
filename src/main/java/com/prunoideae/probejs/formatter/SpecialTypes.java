package com.prunoideae.probejs.formatter;

import com.prunoideae.probejs.formatter.formatter.FormatterType;
import com.prunoideae.probejs.info.type.TypeInfoParameterized;
import dev.latvian.mods.kubejs.util.BuilderBase;
import net.minecraft.nbt.CompoundTag;

import java.util.function.*;

public class SpecialTypes {
    public static void init() {
        NameResolver.putTypeFormatter(Consumer.class, t -> {
            if (t instanceof TypeInfoParameterized parType && parType.getParamTypes().size() == 1) {
                String inner = new FormatterType(parType.getParamTypes().get(0)).format(0, 0);
                return "(arg0: %s) => void".formatted(inner);
            }
            return "(arg0: any) => void";
        });
        NameResolver.putTypeFormatter(Predicate.class, t -> {
            if (t instanceof TypeInfoParameterized parType && parType.getParamTypes().size() == 1) {
                String inner = new FormatterType(parType.getParamTypes().get(0)).format(0, 0);
                return "(arg0: %s) => boolean".formatted(inner);
            }
            return "(arg0: any) => boolean";
        });
        NameResolver.putTypeFormatter(Supplier.class, t -> {
            if (BuilderBase.class.isAssignableFrom(t.getResolvedClass())) {
                return new FormatterType(t, false).format(0, 0);
            }
            if (t instanceof TypeInfoParameterized parType && parType.getParamTypes().size() == 1) {
                String inner = new FormatterType(parType.getParamTypes().get(0)).format(0, 0);
                return "() => %s".formatted(inner);
            }
            return "() => any";
        });
        NameResolver.putTypeFormatter(Function.class, t -> {
            if (t instanceof TypeInfoParameterized parType && parType.getParamTypes().size() == 2) {
                String first = new FormatterType(parType.getParamTypes().get(0)).format(0, 0);
                String second = new FormatterType(parType.getParamTypes().get(1)).format(0, 0);
                return "(arg0: %s) => %s".formatted(first, second);
            }
            return "(arg0: any) => any";
        });
        NameResolver.putTypeFormatter(BiConsumer.class, t -> {
            if (t instanceof TypeInfoParameterized parType && parType.getParamTypes().size() == 2) {
                String first = new FormatterType(parType.getParamTypes().get(0)).format(0, 0);
                String second = new FormatterType(parType.getParamTypes().get(1)).format(0, 0);
                return "(arg0: %s, arg1: %s) => void".formatted(first, second);
            }
            return "(arg0: any, arg1: any) => void";
        });
        NameResolver.putTypeFormatter(BiFunction.class, t -> {
            if (t instanceof TypeInfoParameterized parType && parType.getParamTypes().size() == 3) {
                String first = new FormatterType(parType.getParamTypes().get(0)).format(0, 0);
                String second = new FormatterType(parType.getParamTypes().get(1)).format(0, 0);
                String third = new FormatterType(parType.getParamTypes().get(2)).format(0, 0);
                return "(arg0: %s, arg1: %s) => %s".formatted(first, second, third);
            }
            return "(arg0: any, arg1: any) => any";
        });
    }
}
