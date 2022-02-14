package com.prunoideae.probejs.dump;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.prunoideae.probejs.plugin.WrappedEventHandler;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.block.BlockRegistryEventJS;
import dev.latvian.mods.kubejs.item.ItemRegistryEventJS;
import dev.latvian.mods.kubejs.recipe.RecipeEventJS;
import dev.latvian.mods.kubejs.recipe.RecipeTypeJS;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.kubejs.util.KubeJSPlugins;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import static com.prunoideae.probejs.dump.ProcessBinding.*;
import static com.prunoideae.probejs.dump.ProcessEvent.fetchEventData;
import static com.prunoideae.probejs.dump.ProcessRecipe.fetchRecipeData;

public class ProbeDump {

    public static int dump(ScriptType type) {
        ScriptManager manager = switch (type) {
            case STARTUP -> KubeJS.startupScriptManager;
            case CLIENT -> KubeJS.clientScriptManager;
            case SERVER -> ServerScriptManager.instance.scriptManager;
        };

        DummyBindingEvent bindingEvent = new DummyBindingEvent(manager);
        Map<ResourceLocation, RecipeTypeJS> typeMap = new HashMap<>();
        RegisterRecipeHandlersEvent recipeEvent = new RegisterRecipeHandlersEvent(typeMap);

        KubeJSPlugins.forEachPlugin(plugin -> plugin.addBindings(bindingEvent));
        KubeJSPlugins.forEachPlugin(plugin -> plugin.addRecipes(recipeEvent));

        JsonObject finalData = new JsonObject();
        finalData.add("constants", fetchConstantData(bindingEvent));
        finalData.add("classes", fetchClassData(bindingEvent));
        finalData.add("functions", fetchFunctionData(bindingEvent));
        finalData.add("recipes", fetchRecipeData(typeMap));
        finalData.add("events", fetchEventData());

        HashSet<Class<?>> touchableClasses = new HashSet<>(bindingEvent.getClassDumpMap().values());
        touchableClasses.addAll(typeMap.values().stream().map(recipeTypeJS -> recipeTypeJS.factory.get().getClass()).collect(Collectors.toList()));
        touchableClasses.addAll(bindingEvent.getConstantDumpMap().values().stream().map(Object::getClass).collect(Collectors.toList()));
        touchableClasses.addAll(WrappedEventHandler.capturedEvents.values());
        touchableClasses.addAll(Lists.newArrayList(
                RecipeEventJS.class, ItemRegistryEventJS.class, BlockRegistryEventJS.class
        ));


        finalData.add("globalClasses",
                ProcessGlobal.dumpAccessibleClasses(
                        touchableClasses
                                .stream()
                                .map(ProcessGlobal::getClassOrComponent)
                                .collect(Collectors.toSet())));

        GsonBuilder builder = new GsonBuilder().serializeNulls();
        Gson gson = builder.create();

        try (BufferedWriter writer = Files.newBufferedWriter(KubeJSPaths.EXPORTED.resolve("probejs-%s-export.json".formatted(type.name)))) {
            writer.write(gson.toJson(finalData));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }
}
