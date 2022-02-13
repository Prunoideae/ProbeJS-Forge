package com.prunoideae.probejs.dump;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.RecipeTypeJS;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ProcessRecipe {
    public static JsonArray fetchRecipeData(Map<ResourceLocation, RecipeTypeJS> typeMap) {
        JsonArray recipesJson = new JsonArray();

        typeMap.forEach((key, value) -> {
            JsonObject recipeJson = new JsonObject();
            recipeJson.addProperty("resourceLocation", key.toString());
            recipeJson.addProperty("recipeClass", value.factory.get().getClass().getName());
            recipesJson.add(recipeJson);
        });

        return recipesJson;
    }
}
