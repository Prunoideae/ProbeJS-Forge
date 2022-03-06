
@Mod("create")
@Mod("kubejs_create")
class RecipeHolder {
    /**
     * All recipes from Create.
     */
    readonly create: Document.CreateRecipes;
}

@Mod("create")
@Mod("kubejs_create")
class CreateRecipes {
    /**
     * Creates a recipe for Crushing Wheels.
     * 
     * Specifying chances on outputs will make them output with chance.
     */
    crushing(outputs: dev.latvian.mods.kubejs.item.ItemStackJS[], input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Millstone.
     * 
     * Specifying chances on outputs will make them output with chance.
     */
    milling(outputs: dev.latvian.mods.kubejs.item.ItemStackJS[], input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Compacting.
     * 
     * Different from Pressing, Compacting uses a Basin as container, can have multiple items as inputs, and up to 2 fluid inputs.
     */
    compacting(output: dev.latvian.mods.kubejs.item.ItemStackJS, inputs: dev.latvian.mods.kubejs.item.ingredient.IngredientJS[]): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Pressing.
     * 
     * Pressing uses Depot or Belt as container, and can only have 1 item slot as input.
     * 
     * Pressing is available as an Assembly step.
     */
    pressing(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Deploying.
     * 
     * Deploying is available as an Assembly step.
     */
    deploying(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Cutting.
     * 
     * Cutting is available as an Assembly step.
     */
    cutting(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;
    /**
     * Creates a recipe for Filling.
     * 
     * Filling is available as an Assembly step.
     */
    filling(output: dev.latvian.mods.kubejs.item.ItemStackJS, input: dev.latvian.mods.kubejs.item.ingredient.IngredientJS): dev.latvian.mods.kubejs.create.ProcessingRecipeJS;

}