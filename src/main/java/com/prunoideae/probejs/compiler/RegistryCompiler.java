package com.prunoideae.probejs.compiler;

import com.google.gson.Gson;
import com.prunoideae.probejs.ProbePaths;
import com.prunoideae.probejs.formatter.formatter.FormatterClass;
import com.prunoideae.probejs.formatter.formatter.FormatterNamespace;
import com.prunoideae.probejs.formatter.formatter.IFormatter;
import com.prunoideae.probejs.info.type.TypeInfoClass;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class RegistryCompiler {
    public static Set<Class<?>> getRegistryClasses() {
        Set<Class<?>> result = new HashSet<>();
        result.add(RegistryObjectBuilderTypes.class);
        result.add(RegistryObjectBuilderTypes.RegistryEventJS.class);
        RegistryObjectBuilderTypes.MAP.values().forEach(v -> v.types.values().forEach(v1 -> result.add(v1.builderClass())));
        return result;
    }

    public static void compileEventRegistries(BufferedWriter writer) throws IOException {
        Gson stringG = new Gson();
        for (var types : RegistryObjectBuilderTypes.MAP.values()) {
            String fullName = types.registryKey.location().getNamespace() + "." + types.registryKey.location().getPath().replace('/', '.') + ".registry";
            String registryName = FormatterRegistry.getFormattedRegistryName(types);
            writer.write("declare function onEvent(name: %s, handler: (event: Registry.%s) => void);\n".formatted(stringG.toJson(fullName), registryName));
            if (types.registryKey.location().getNamespace().equals("minecraft")) {
                String shortName = types.registryKey.location().getPath().replace('/', '.') + ".registry";
                writer.write("declare function onEvent(name: %s, handler: (event: Registry.%s) => void);\n".formatted(stringG.toJson(shortName), registryName));
            }
        }
    }

    public static void compileRegistries() throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("registries.d.ts"));
        writer.write("/// <reference path=\"./globals.d.ts\" />\n");
        IFormatter namespace = new FormatterNamespace("Registry", RegistryObjectBuilderTypes.MAP.values().stream().map(FormatterRegistry::new).collect(Collectors.toList()));
        writer.write(String.join("\n", namespace.format(0, 4)));
        writer.flush();
    }

    private static class FormatterRegistry implements IFormatter {
        RegistryObjectBuilderTypes<?> types;
        String name;

        private static String getFormattedRegistryName(RegistryObjectBuilderTypes<?> types) {
            return Arrays.stream(types.registryKey.location().getPath().split("/")).map(str -> str.substring(0, 1).toUpperCase() + str.substring(1)).collect(Collectors.joining(""));
        }

        private FormatterRegistry(RegistryObjectBuilderTypes<?> types) {
            this.types = types;
            this.name = getFormattedRegistryName(types);
        }

        @Override
        public List<String> format(Integer indent, Integer stepIndent) {
            List<String> formatted = new ArrayList<>();
            int stepped = indent + stepIndent;
            Gson stringG = new Gson();
            formatted.add(" ".repeat(indent) + "class %s extends %s {".formatted(name, FormatterClass.formatTypeParameterized(new TypeInfoClass(RegistryObjectBuilderTypes.RegistryEventJS.class))));
            for (RegistryObjectBuilderTypes.BuilderType<?> builder : types.types.values()) {
                formatted.add(" ".repeat(stepped) + "create(id: string, type: %s): %s;".formatted(stringG.toJson(builder.type()), FormatterClass.formatTypeParameterized(new TypeInfoClass(builder.builderClass()))));
            }
            formatted.add(" ".repeat(indent) + "}");
            return formatted;
        }
    }
}
