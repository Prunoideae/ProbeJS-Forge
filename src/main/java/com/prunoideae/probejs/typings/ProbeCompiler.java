package com.prunoideae.probejs.typings;

import com.google.common.primitives.Primitives;
import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import com.prunoideae.probejs.ProbeJS;
import com.prunoideae.probejs.plugin.WrappedEventHandler;
import com.prunoideae.probejs.resolver.document.DocumentFormatter;
import com.prunoideae.probejs.resolver.document.DocumentManager;
import com.prunoideae.probejs.toucher.ClassInfo;
import com.prunoideae.probejs.toucher.ClassToucher;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.recipe.RecipeTypeJS;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.kubejs.util.KubeJSPlugins;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProbeCompiler {
    private static void resolveClassname(Set<Class<?>> globalClasses, Set<Class<?>> exportedClasses) {
        Set<String> usedNames = new HashSet<>();
        for (Class<?> clazz : globalClasses) {
            if (TSGlobalClassFormatter.resolvedClassName.containsKey(clazz.getName()))
                continue;
            String fullName = clazz.getName();
            String[] paths = fullName.split("\\.");
            String resolvedName = exportedClasses.contains(clazz) ? "" : "Internal.";
            resolvedName += usedNames.contains(resolvedName + paths[paths.length - 1]) ? NameGuard.compileClasspath(paths) : paths[paths.length - 1];
            usedNames.add(resolvedName);
            TSGlobalClassFormatter.resolvedClassName.put(clazz.getName(), resolvedName);
        }
    }

    public static Set<Class<?>> compileGlobal(Path outFile, Map<ResourceLocation, RecipeTypeJS> typeMap, DummyBindingEvent bindingEvent, Set<Class<?>> cachedClasses) {
        Set<Class<?>> touchableClasses = new HashSet<>(bindingEvent.getClassDumpMap().values());


        bindingEvent.getClassDumpMap().forEach((k, v) -> {
            TSGlobalClassFormatter.resolvedClassName.put(v.getName(), k);
            touchableClasses.add(v);
        });

        touchableClasses.addAll(cachedClasses);
        touchableClasses.addAll(typeMap.values().stream().map(recipeTypeJS -> recipeTypeJS.factory.get().getClass()).collect(Collectors.toList()));
        touchableClasses.addAll(bindingEvent.getConstantDumpMap().values().stream().map(Object::getClass).collect(Collectors.toList()));
        touchableClasses.addAll(WrappedEventHandler.capturedEvents.values());

        ProbeJS.LOGGER.info("Querying all classes accessible...");
        Set<Class<?>> globalClasses = new HashSet<>(touchableClasses);
        touchableClasses.forEach(clazz -> globalClasses.addAll(new ClassToucher(clazz).touchClassRecursive()));
        resolveClassname(globalClasses, new HashSet<>(bindingEvent.getClassDumpMap().values()));

        HashMap<String, List<TSGlobalClassFormatter.ClassFormatter>> namespacedClasses = new HashMap<>();

        ProbeJS.LOGGER.info("Compiling classes to declarations...");
        try (BufferedWriter writer = Files.newBufferedWriter(outFile)) {
            globalClasses
                    .stream()
                    .filter(clazz -> !Primitives.allPrimitiveTypes().contains(clazz))
                    .filter(clazz -> !Primitives.allWrapperTypes().contains(clazz))
                    .forEach(clazz -> {
                        ClassInfo info = new ClassInfo(clazz);
                        TSGlobalClassFormatter.ClassFormatter formatter = new TSGlobalClassFormatter.ClassFormatter(info, 0, 4, s -> !Pattern.matches("^[fm]_[\\d_]+$", s));
                        if (TSGlobalClassFormatter.resolvedClassName.get(clazz.getName()).contains(".")) {
                            String fullName = TSGlobalClassFormatter.resolvedClassName.get(clazz.getName());
                            String[] paths = fullName.split("\\.");
                            String pathName = String.join(".", Arrays.copyOfRange(paths, 0, paths.length - 1));
                            namespacedClasses.computeIfAbsent(pathName, p -> new ArrayList<>()).add(formatter);
                        } else {
                            try {
                                writer.write(formatter.format());
                                if (info.getClazz().isInterface())
                                    writer.write("declare const %s: %s;\n".formatted(TSGlobalClassFormatter.resolvedClassName.get(clazz.getName()), TSGlobalClassFormatter.resolvedClassName.get(clazz.getName())));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

            namespacedClasses.forEach((k, v) -> {
                try {
                    writer.write(new TSGlobalClassFormatter.NamespaceFormatter(k, v, 4, 0, false).format());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
            writer.write(String.join("\n", new DocumentFormatter(DocumentManager.classAddition, 4, 4).format()));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ProbeJS.LOGGER.info("Done!");
        return globalClasses;
    }

    public static void compileEvent(Path outFile, Map<String, Class<? extends EventJS>> cachedEvents) {
        ProbeJS.LOGGER.info("Compiling captured events...");
        try (BufferedWriter writer = Files.newBufferedWriter(outFile)) {
            writer.write("/// <reference path=\"./globals.d.ts\" />\n");
            cachedEvents.putAll(WrappedEventHandler.capturedEvents);
            cachedEvents.forEach(
                    (capture, event) -> {
                        try {
                            writer.write("declare function onEvent(name: \"%s\", handler: (event: %s) => void);\n".formatted(capture, TSGlobalClassFormatter.resolvedClassName.get(event.getName())));
                            writer.write("declare function captureEvent(name: \"%s\", handler: (event: %s) => void);\n".formatted(capture, TSGlobalClassFormatter.resolvedClassName.get(event.getName())));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void compileConstants(Path outFile, DummyBindingEvent bindingEvent) {
        ProbeJS.LOGGER.info("Compiling constants definitions...");
        try (BufferedWriter writer = Files.newBufferedWriter(outFile)) {
            writer.write("/// <reference path=\"./globals.d.ts\" />\n");
            bindingEvent.getConstantDumpMap().forEach(
                    (name, value) -> {
                        try {

                            if (TSGlobalClassFormatter.transformValue(value) != null) {
                                writer.write("declare const %s: %s;\n".formatted(name, TSGlobalClassFormatter.transformValue(value)));
                                return;
                            }
                            writer.write("declare const %s: %s;\n".formatted(name, TSGlobalClassFormatter.resolvedClassName.get(value.getClass().getName())));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void compileJava(Path outFile, Set<Class<?>> classes) {
        ProbeJS.LOGGER.info("Compiling java() definitions...");
        try (BufferedWriter writer = Files.newBufferedWriter(outFile)) {
            writer.write("/// <reference path=\"./globals.d.ts\" />\n");
            classes.stream()
                    .filter(clazz -> !clazz.isInterface())
                    .filter(clazz -> ServerScriptManager.instance.scriptManager.isClassAllowed(clazz.getName()))
                    .forEach(clazz -> {
                        try {
                            writer.write("declare function java(name: \"%s\"): typeof %s;\n".formatted(clazz.getName(), TSGlobalClassFormatter.resolvedClassName.get(clazz.getName())));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void compileIndex(Path outFile) {
        try (BufferedWriter writer = Files.newBufferedWriter(outFile)) {
            writer.write("/// <reference path=\"./globals.d.ts\" />\n");
            writer.write("/// <reference path=\"./events.d.ts\" />\n");
            writer.write("/// <reference path=\"./constants.d.ts\" />\n");
            writer.write("/// <reference path=\"./java.d.ts\" />\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void compileJSConfig(Path outFile) {
        try (BufferedWriter writer = Files.newBufferedWriter(outFile)) {
            writer.write("""
                    {
                        "compilerOptions": {
                            "lib": ["ES5", "ES2015"],
                            "typeRoots": ["kubetypings"]
                        }
                    }""");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void compileDeclarations() throws IOException {
        Path typingDir = KubeJSPaths.DIRECTORY.resolve("kubetypings");
        if (Files.notExists(typingDir))
            Files.createDirectories(typingDir);
        DummyBindingEvent bindingEvent = new DummyBindingEvent(ServerScriptManager.instance.scriptManager);
        Map<ResourceLocation, RecipeTypeJS> typeMap = new HashMap<>();
        RegisterRecipeHandlersEvent recipeEvent = new RegisterRecipeHandlersEvent(typeMap);

        KubeJSPlugins.forEachPlugin(plugin -> plugin.addRecipes(recipeEvent));
        KubeJSPlugins.forEachPlugin(plugin -> plugin.addBindings(bindingEvent));

        Map<String, Class<? extends EventJS>> cachedEvents = new HashMap<>();
        Path cachedEventPath = KubeJSPaths.EXPORTED.resolve("cachedEvents.json");
        if (Files.exists(cachedEventPath)) {
            Map<?, ?> cachedMap = new Gson().fromJson(Files.newBufferedReader(cachedEventPath), Map.class);
            cachedMap.forEach((k, v) -> {
                if (k instanceof String && v instanceof String) {
                    try {
                        Class<?> clazz = Class.forName((String) v);
                        if (EventJS.class.isAssignableFrom(clazz))
                            cachedEvents.put((String) k, (Class<? extends EventJS>) clazz);
                    } catch (ClassNotFoundException e) {
                        ProbeJS.LOGGER.warn("Class %s was in the cache, but disappeared in packages now.".formatted(v));
                    }
                }
            });
        }

        Set<Class<?>> cachedClasses = new HashSet<>(cachedEvents.values());
        Set<Class<?>> touchedClasses = compileGlobal(typingDir.resolve("globals.d.ts"), typeMap, bindingEvent, cachedClasses);
        compileEvent(typingDir.resolve("events.d.ts"), cachedEvents);
        compileConstants(typingDir.resolve("constants.d.ts"), bindingEvent);
        compileJava(typingDir.resolve("java.d.ts"), touchedClasses);
        compileIndex(typingDir.resolve("index.d.ts"));
        try (BufferedWriter writer = Files.newBufferedWriter(cachedEventPath)) {
            Map<String, String> eventsCache = new HashMap<>();
            cachedEvents.forEach((k, v) -> eventsCache.put(k, v.getName()));
            new Gson().toJson(eventsCache, writer);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Files.notExists(KubeJSPaths.DIRECTORY.resolve("jsconfig.json")))
            compileJSConfig(KubeJSPaths.DIRECTORY.resolve("jsconfig.json"));
        ProbeJS.LOGGER.info("All done!");
    }

}
