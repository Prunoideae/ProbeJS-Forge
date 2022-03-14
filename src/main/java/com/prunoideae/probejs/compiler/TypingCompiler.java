package com.prunoideae.probejs.compiler;

import com.google.gson.Gson;
import com.prunoideae.probejs.ProbeJS;
import com.prunoideae.probejs.formatter.NameResolver;
import com.prunoideae.probejs.info.Walker;
import com.prunoideae.probejs.plugin.WrappedEventHandler;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.recipe.RecipeTypeJS;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.kubejs.util.KubeJSPlugins;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TypingCompiler {

    public static Map<String, Class<?>> readCachedEvents() throws IOException {
        Map<String, Class<?>> cachedEvents = new HashMap<>();
        Path cachedEventPath = KubeJSPaths.EXPORTED.resolve("cachedEvents.json");
        if (Files.exists(cachedEventPath)) {
            Map<?, ?> cachedMap = new Gson().fromJson(Files.newBufferedReader(cachedEventPath), Map.class);
            cachedMap.forEach((k, v) -> {
                if (k instanceof String && v instanceof String) {
                    try {
                        Class<?> clazz = Class.forName((String) v);
                        if (EventJS.class.isAssignableFrom(clazz))
                            cachedEvents.put((String) k, clazz);
                    } catch (ClassNotFoundException e) {
                        ProbeJS.LOGGER.warn("Class %s was in the cache, but disappeared in packages now.".formatted(v));
                    }
                }
            });
        }
        return cachedEvents;
    }

    public static Set<Class<?>> fetchClasses(Map<ResourceLocation, RecipeTypeJS> typeMap, DummyBindingEvent bindingEvent, Set<Class<?>> cachedClasses) {
        Set<Class<?>> touchableClasses = new HashSet<>(bindingEvent.getClassDumpMap().values());
        touchableClasses.addAll(cachedClasses);
        touchableClasses.addAll(typeMap.values().stream().map(recipeTypeJS -> recipeTypeJS.factory.get().getClass()).collect(Collectors.toList()));
        touchableClasses.addAll(bindingEvent.getConstantDumpMap().values().stream().map(Object::getClass).collect(Collectors.toList()));
        touchableClasses.addAll(WrappedEventHandler.capturedEvents.values());

        Walker walker = new Walker(touchableClasses);
        return walker.walk();
    }

    public static void compileGlobal() throws IOException {
        DummyBindingEvent bindingEvent = new DummyBindingEvent(ServerScriptManager.instance.scriptManager);
        Map<ResourceLocation, RecipeTypeJS> typeMap = new HashMap<>();
        RegisterRecipeHandlersEvent recipeEvent = new RegisterRecipeHandlersEvent(typeMap);

        KubeJSPlugins.forEachPlugin(plugin -> plugin.addRecipes(recipeEvent));
        KubeJSPlugins.forEachPlugin(plugin -> plugin.addBindings(bindingEvent));

        Map<String, Class<?>> cachedEvents = readCachedEvents();
        Set<Class<?>> globalClasses = fetchClasses(typeMap, bindingEvent, new HashSet<>(cachedEvents.values()));
        NameResolver.resolveNames(globalClasses);

        ProbeJS.LOGGER.info(globalClasses.size());
    }
}
