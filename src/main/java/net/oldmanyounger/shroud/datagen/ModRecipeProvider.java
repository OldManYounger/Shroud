package net.oldmanyounger.shroud.datagen;

import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Generates crafting and cooking recipes for Shroud blocks and items */
public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    /** Creates the recipe provider using the shared registry lookup */
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    /** Defines all Shroud recipes that should be emitted by datagen */
    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        // Convenience local variable holding the Sculk planks block
        var sculkPlanks = ModBlocks.SCULK_PLANKS.get();

        // Registers shapeless recipes converting Sculk logs and wood (stripped and non-stripped) into Sculk planks
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_PLANKS.get(), 4)
                .requires(Ingredient.of(
                        ModBlocks.SCULK_LOG.get(),
                        ModBlocks.SCULK_WOOD.get(),
                        ModBlocks.STRIPPED_SCULK_LOG.get(),
                        ModBlocks.STRIPPED_SCULK_WOOD.get()
                ))
                .unlockedBy("has_sculk_log", has(ModBlocks.SCULK_LOG.get()))
                .unlockedBy("has_sculk_wood", has(ModBlocks.SCULK_WOOD.get()))
                .unlockedBy("has_stripped_sculk_log", has(ModBlocks.STRIPPED_SCULK_LOG.get()))
                .unlockedBy("has_stripped_sculk_wood", has(ModBlocks.STRIPPED_SCULK_WOOD.get()))
                .save(recipeOutput);

        // Registers shaped recipe converting Sculk logs into Sculk wood
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_WOOD.get(), 3)
                .define('#', ModBlocks.SCULK_LOG.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_sculk_log", has(ModBlocks.SCULK_LOG.get()))
                .save(recipeOutput);

        // Registers shaped recipe converting stripped Sculk logs into stripped Sculk wood
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRIPPED_SCULK_WOOD.get(), 3)
                .define('#', ModBlocks.STRIPPED_SCULK_LOG.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_stripped_sculk_log", has(ModBlocks.STRIPPED_SCULK_LOG.get()))
                .save(recipeOutput);

        // Registers Sculk stair and slab recipes based on Sculk planks
        stairBuilder(ModBlocks.SCULK_STAIRS.get(), Ingredient.of(sculkPlanks)).group("sculk")
                .unlockedBy("has_sculk_planks", has(sculkPlanks))
                .save(recipeOutput);
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_SLAB.get(), sculkPlanks);

        // Registers Sculk button and pressure plate recipes
        buttonBuilder(ModBlocks.SCULK_BUTTON.get(), Ingredient.of(sculkPlanks)).group("sculk")
                .unlockedBy("has_sculk_planks", has(sculkPlanks))
                .save(recipeOutput);
        pressurePlate(recipeOutput, ModBlocks.SCULK_PRESSURE_PLATE.get(), sculkPlanks);

        // Registers Sculk fence, fence gate, and wall recipes
        fenceBuilder(ModBlocks.SCULK_FENCE.get(), Ingredient.of(sculkPlanks)).group("sculk")
                .unlockedBy("has_sculk_planks", has(sculkPlanks))
                .save(recipeOutput);
        fenceGateBuilder(ModBlocks.SCULK_FENCE_GATE.get(), Ingredient.of(sculkPlanks)).group("sculk")
                .unlockedBy("has_sculk_planks", has(sculkPlanks))
                .save(recipeOutput);
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_WALL.get(), sculkPlanks);

        // Registers Sculk door and trapdoor recipes
        doorBuilder(ModBlocks.SCULK_DOOR.get(), Ingredient.of(sculkPlanks)).group("sculk")
                .unlockedBy("has_sculk_planks", has(sculkPlanks))
                .save(recipeOutput);
        trapdoorBuilder(ModBlocks.SCULK_TRAPDOOR.get(), Ingredient.of(sculkPlanks)).group("sculk")
                .unlockedBy("has_sculk_planks", has(sculkPlanks))
                .save(recipeOutput);
    }

    /** Helper for registering smelting recipes for ore-like inputs */
    protected static void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory,
                                      ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new,
                pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    /** Helper for registering blasting recipes for ore-like inputs */
    protected static void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory,
                                      ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new,
                pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    /** Shared implementation for all ore cooking recipes across smelting and blasting */
    protected static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput,
                                                                       RecipeSerializer<T> pCookingSerializer,
                                                                       AbstractCookingRecipe.Factory<T> factory,
                                                                       List<ItemLike> pIngredients,
                                                                       RecipeCategory pCategory,
                                                                       ItemLike pResult,
                                                                       float pExperience,
                                                                       int pCookingTime,
                                                                       String pGroup,
                                                                       String pRecipeName) {
        for (ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime,
                            pCookingSerializer, factory)
                    .group(pGroup)
                    .unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(recipeOutput,
                            Shroud.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
}
