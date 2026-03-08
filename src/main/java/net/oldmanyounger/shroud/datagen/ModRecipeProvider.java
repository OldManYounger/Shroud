package net.oldmanyounger.shroud.datagen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.oldmanyounger.shroud.item.ModItems;

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

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GLOOMSTONE.get())
                .pattern("DD")
                .pattern("DD")
                .define('D', ModItems.GLOOMSTONE_DUST.get())
                .unlockedBy("has_gloomstone_dust", has(ModItems.GLOOMSTONE_DUST.get()))
                .save(recipeOutput);

        List<ItemLike> EVENTIDE_SMELTABLES = List.of(
                ModItems.RAW_EVENTIDE,
                ModBlocks.EVENTIDE_ORE,
                ModBlocks.EVENTIDE_DEEPSLATE_ORE);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.EVENTIDE_BLOCK.get())
                .pattern("EEE")
                .pattern("EEE")
                .pattern("EEE")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.EVENTIDE_INGOT.get(), 9)
                .requires(ModBlocks.EVENTIDE_BLOCK)
                .unlockedBy("has_eventide_block", has(ModBlocks.EVENTIDE_BLOCK))
                .save(recipeOutput, "shroud:eventide_ingot_from_eventide_block");

        oreSmelting(recipeOutput, EVENTIDE_SMELTABLES, RecipeCategory.MISC, ModItems.EVENTIDE_INGOT.get(), 0.25f, 200, "eventide");
        oreBlasting(recipeOutput, EVENTIDE_SMELTABLES, RecipeCategory.MISC, ModItems.EVENTIDE_INGOT.get(), 0.25f, 100, "eventide");
        trimSmithing(recipeOutput, ModItems.EVENTIDE_SMITHING_TEMPLATE.get(), ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "eventide"));

        // Tools (Eventide)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EVENTIDE_SWORD.get())
                .pattern("E")
                .pattern("E")
                .pattern("S")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.EVENTIDE_PICKAXE.get())
                .pattern("EEE")
                .pattern(" S ")
                .pattern(" S ")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.EVENTIDE_SHOVEL.get())
                .pattern("E")
                .pattern("S")
                .pattern("S")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.EVENTIDE_AXE.get())
                .pattern("EE")
                .pattern("ES")
                .pattern(" S")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.EVENTIDE_HOE.get())
                .pattern("EE")
                .pattern(" S")
                .pattern(" S")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(recipeOutput);

        // Bow (Eventide)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EVENTIDE_BOW.get())
                .pattern(" ES")
                .pattern("E S")
                .pattern(" ES")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .define('S', Items.STRING)
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(recipeOutput);

        // Armor (Eventide)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EVENTIDE_HELMET.get())
                .pattern("EEE")
                .pattern("E E")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EVENTIDE_CHESTPLATE.get())
                .pattern("E E")
                .pattern("EEE")
                .pattern("EEE")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EVENTIDE_LEGGINGS.get())
                .pattern("EEE")
                .pattern("E E")
                .pattern("E E")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EVENTIDE_BOOTS.get())
                .pattern("E E")
                .pattern("E E")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(recipeOutput);

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

        // Convenience local variable holding the Umber planks block
        var umberPlanks = ModBlocks.UMBER_PLANKS.get();

        // Registers shapeless recipes converting Umber logs and wood (stripped and non-stripped) into Umber planks
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.UMBER_PLANKS.get(), 4)
                .requires(Ingredient.of(
                        ModBlocks.UMBER_LOG.get(),
                        ModBlocks.UMBER_WOOD.get(),
                        ModBlocks.STRIPPED_UMBER_LOG.get(),
                        ModBlocks.STRIPPED_UMBER_WOOD.get()
                ))
                .unlockedBy("has_umber_log", has(ModBlocks.UMBER_LOG.get()))
                .unlockedBy("has_umber_wood", has(ModBlocks.UMBER_WOOD.get()))
                .unlockedBy("has_stripped_umber_log", has(ModBlocks.STRIPPED_UMBER_LOG.get()))
                .unlockedBy("has_stripped_umber_wood", has(ModBlocks.STRIPPED_UMBER_WOOD.get()))
                .save(recipeOutput);

        // Registers shaped recipe converting Umber logs into Umber wood
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.UMBER_WOOD.get(), 3)
                .define('#', ModBlocks.UMBER_LOG.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_umber_log", has(ModBlocks.UMBER_LOG.get()))
                .save(recipeOutput);

        // Registers shaped recipe converting stripped Umber logs into stripped Umber wood
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRIPPED_UMBER_WOOD.get(), 3)
                .define('#', ModBlocks.STRIPPED_UMBER_LOG.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_stripped_umber_log", has(ModBlocks.STRIPPED_UMBER_LOG.get()))
                .save(recipeOutput);

        // Registers Umber stair and slab recipes based on Umber planks
        stairBuilder(ModBlocks.UMBER_STAIRS.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(recipeOutput);
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.UMBER_SLAB.get(), umberPlanks);

        // Registers Umber button and pressure plate recipes
        buttonBuilder(ModBlocks.UMBER_BUTTON.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(recipeOutput);
        pressurePlate(recipeOutput, ModBlocks.UMBER_PRESSURE_PLATE.get(), umberPlanks);

        // Registers Umber fence, fence gate, and wall recipes
        fenceBuilder(ModBlocks.UMBER_FENCE.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(recipeOutput);
        fenceGateBuilder(ModBlocks.UMBER_FENCE_GATE.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(recipeOutput);
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.UMBER_WALL.get(), umberPlanks);

        // Registers Umber door and trapdoor recipes
        doorBuilder(ModBlocks.UMBER_DOOR.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(recipeOutput);
        trapdoorBuilder(ModBlocks.UMBER_TRAPDOOR.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
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
