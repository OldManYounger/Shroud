package net.oldmanyounger.shroud.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.item.ModItems;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generates crafting, smelting, blasting, and smithing recipes for Shroud.
 *
 * <p>This provider defines the recipes for core materials, utility items,
 * Eventide equipment, and the custom Sculk and Umber wood sets. It keeps all of
 * the mod's recipe generation in one place, which makes it easier to maintain
 * progression and content relationships as the project grows.
 *
 * <p>In the broader context of the project, this class is part of the gameplay
 * data layer that turns Shroud's registered content into usable crafting and
 * processing recipes during normal play.
 */
public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    // Creates the recipe provider using the shared registry lookup
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    // Registers all Shroud recipes emitted during datagen
    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // Core materials and utility recipes
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GLOOMSTONE.get())
                .pattern("DD")
                .pattern("DD")
                .define('D', ModItems.GLOOMSTONE_DUST.get())
                .unlockedBy("has_gloomstone_dust", has(ModItems.GLOOMSTONE_DUST.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.TOTEM_OF_LAST_BREATH.get())
                .pattern("EGE")
                .pattern("PTP")
                .pattern("EGE")
                .define('G', ModItems.GLOOMSTONE_DUST.get())
                .define('E', Items.ECHO_SHARD)
                .define('P', ModItems.SCULK_PEARL.get())
                .define('T', Items.TOTEM_OF_UNDYING)
                .unlockedBy("has_totem_of_undying", has(Items.TOTEM_OF_UNDYING))
                .save(recipeOutput);

        // Eventide materials and block recipes
        List<ItemLike> EVENTIDE_SMELTABLES = List.of(
                ModItems.RAW_EVENTIDE,
                ModBlocks.EVENTIDE_ORE,
                ModBlocks.EVENTIDE_DEEPSLATE_ORE
        );

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.EVENTIDE_BLOCK.get())
                .pattern("EEE")
                .pattern("EEE")
                .pattern("EEE")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT))
                .save(recipeOutput);

        // Sculk stone and deepslate variant recipes
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_STONE_BRICKS.get(), 4)
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.SCULK_STONE.get())
                .unlockedBy("has_sculk_stone", has(ModBlocks.SCULK_STONE.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHISELED_SCULK_STONE_BRICKS.get())
                .pattern("#")
                .pattern("#")
                .define('#', ModBlocks.SCULK_STONE_BRICKS.get())
                .unlockedBy("has_sculk_stone_bricks", has(ModBlocks.SCULK_STONE_BRICKS.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_DEEPSLATE_BRICKS.get(), 4)
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.COBBLED_SCULK_DEEPSLATE.get())
                .unlockedBy("has_cobbled_sculk_deepslate", has(ModBlocks.COBBLED_SCULK_DEEPSLATE.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_DEEPSLATE_TILES.get(), 4)
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.SCULK_DEEPSLATE_BRICKS.get())
                .unlockedBy("has_sculk_deepslate_bricks", has(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()))
                .save(recipeOutput);

        // Furnace recipes: cobbled -> smooth
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModBlocks.SCULK_COBBLESTONE.get()),
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.SCULK_STONE.get(),
                        0.1f,
                        200
                )
                .unlockedBy("has_sculk_cobblestone", has(ModBlocks.SCULK_COBBLESTONE.get()))
                .save(recipeOutput, Shroud.MOD_ID + ":sculk_stone_from_smelting_sculk_cobblestone");

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModBlocks.COBBLED_SCULK_DEEPSLATE.get()),
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.SCULK_DEEPSLATE.get(),
                        0.1f,
                        200
                )
                .unlockedBy("has_cobbled_sculk_deepslate", has(ModBlocks.COBBLED_SCULK_DEEPSLATE.get()))
                .save(recipeOutput, Shroud.MOD_ID + ":sculk_deepslate_from_smelting_cobbled_sculk_deepslate");

        // Cracked variants via smelting
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModBlocks.SCULK_STONE_BRICKS.get()),
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.CRACKED_SCULK_STONE_BRICKS.get(),
                        0.1f,
                        200
                )
                .unlockedBy("has_sculk_stone_bricks", has(ModBlocks.SCULK_STONE_BRICKS.get()))
                .save(recipeOutput, Shroud.MOD_ID + ":cracked_sculk_stone_bricks_from_smelting");

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()),
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.CRACKED_SCULK_DEEPSLATE_BRICKS.get(),
                        0.1f,
                        200
                )
                .unlockedBy("has_sculk_deepslate_bricks", has(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()))
                .save(recipeOutput, Shroud.MOD_ID + ":cracked_sculk_deepslate_bricks_from_smelting");

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModBlocks.SCULK_DEEPSLATE_TILES.get()),
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.CRACKED_SCULK_DEEPSLATE_TILES.get(),
                        0.1f,
                        200
                )
                .unlockedBy("has_sculk_deepslate_tiles", has(ModBlocks.SCULK_DEEPSLATE_TILES.get()))
                .save(recipeOutput, Shroud.MOD_ID + ":cracked_sculk_deepslate_tiles_from_smelting");

        // Sculk cobblestone structural recipes
        stairBuilder(ModBlocks.SCULK_COBBLESTONE_STAIRS.get(), Ingredient.of(ModBlocks.SCULK_COBBLESTONE.get()))
                .unlockedBy("has_sculk_cobblestone", has(ModBlocks.SCULK_COBBLESTONE.get()))
                .save(recipeOutput);
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_COBBLESTONE_SLAB.get(), ModBlocks.SCULK_COBBLESTONE.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_COBBLESTONE_WALL.get(), ModBlocks.SCULK_COBBLESTONE.get());

        // Sculk stone brick structural recipes
        stairBuilder(ModBlocks.SCULK_STONE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.SCULK_STONE_BRICKS.get()))
                .unlockedBy("has_sculk_stone_bricks", has(ModBlocks.SCULK_STONE_BRICKS.get()))
                .save(recipeOutput);
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_STONE_BRICK_SLAB.get(), ModBlocks.SCULK_STONE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_STONE_BRICK_WALL.get(), ModBlocks.SCULK_STONE_BRICKS.get());

        // Cobbled sculk deepslate structural recipes
        stairBuilder(ModBlocks.COBBLED_SCULK_DEEPSLATE_STAIRS.get(), Ingredient.of(ModBlocks.COBBLED_SCULK_DEEPSLATE.get()))
                .unlockedBy("has_cobbled_sculk_deepslate", has(ModBlocks.COBBLED_SCULK_DEEPSLATE.get()))
                .save(recipeOutput);
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.COBBLED_SCULK_DEEPSLATE_SLAB.get(), ModBlocks.COBBLED_SCULK_DEEPSLATE.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.COBBLED_SCULK_DEEPSLATE_WALL.get(), ModBlocks.COBBLED_SCULK_DEEPSLATE.get());

        // Sculk deepslate brick structural recipes
        stairBuilder(ModBlocks.SCULK_DEEPSLATE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()))
                .unlockedBy("has_sculk_deepslate_bricks", has(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()))
                .save(recipeOutput);
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_DEEPSLATE_BRICK_SLAB.get(), ModBlocks.SCULK_DEEPSLATE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_DEEPSLATE_BRICK_WALL.get(), ModBlocks.SCULK_DEEPSLATE_BRICKS.get());

        // Sculk deepslate tile structural recipes
        stairBuilder(ModBlocks.SCULK_DEEPSLATE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.SCULK_DEEPSLATE_TILES.get()))
                .unlockedBy("has_sculk_deepslate_tiles", has(ModBlocks.SCULK_DEEPSLATE_TILES.get()))
                .save(recipeOutput);
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_DEEPSLATE_TILE_SLAB.get(), ModBlocks.SCULK_DEEPSLATE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_DEEPSLATE_TILE_WALL.get(), ModBlocks.SCULK_DEEPSLATE_TILES.get());

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.EVENTIDE_INGOT.get(), 9)
                .requires(ModBlocks.EVENTIDE_BLOCK)
                .unlockedBy("has_eventide_block", has(ModBlocks.EVENTIDE_BLOCK))
                .save(recipeOutput, "shroud:eventide_ingot_from_eventide_block");

        oreSmelting(recipeOutput, EVENTIDE_SMELTABLES, RecipeCategory.MISC, ModItems.EVENTIDE_INGOT.get(), 0.25f, 200, "eventide");
        oreBlasting(recipeOutput, EVENTIDE_SMELTABLES, RecipeCategory.MISC, ModItems.EVENTIDE_INGOT.get(), 0.25f, 100, "eventide");
        trimSmithing(recipeOutput, ModItems.EVENTIDE_SMITHING_TEMPLATE.get(),
                ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "eventide"));

        // Eventide tools
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

        // Eventide bow
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EVENTIDE_BOW.get())
                .pattern(" ES")
                .pattern("E S")
                .pattern(" ES")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .define('S', Items.STRING)
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(recipeOutput);

        // Eventide armor
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

        // Virelith wood set recipes
        var virelithPlanks = ModBlocks.VIRELITH_PLANKS.get();

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.VIRELITH_PLANKS.get(), 4)
                .requires(Ingredient.of(
                        ModBlocks.VIRELITH_LOG.get(),
                        ModBlocks.VIRELITH_WOOD.get(),
                        ModBlocks.STRIPPED_VIRELITH_LOG.get(),
                        ModBlocks.STRIPPED_VIRELITH_WOOD.get()
                ))
                .unlockedBy("has_virelith_log", has(ModBlocks.VIRELITH_LOG.get()))
                .unlockedBy("has_virelith_wood", has(ModBlocks.VIRELITH_WOOD.get()))
                .unlockedBy("has_stripped_virelith_log", has(ModBlocks.STRIPPED_VIRELITH_LOG.get()))
                .unlockedBy("has_stripped_virelith_wood", has(ModBlocks.STRIPPED_VIRELITH_WOOD.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.VIRELITH_WOOD.get(), 3)
                .define('#', ModBlocks.VIRELITH_LOG.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_virelith_log", has(ModBlocks.VIRELITH_LOG.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRIPPED_VIRELITH_WOOD.get(), 3)
                .define('#', ModBlocks.STRIPPED_VIRELITH_LOG.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_stripped_virelith_log", has(ModBlocks.STRIPPED_VIRELITH_LOG.get()))
                .save(recipeOutput);

        stairBuilder(ModBlocks.VIRELITH_STAIRS.get(), Ingredient.of(virelithPlanks)).group("virelith")
                .unlockedBy("has_virelith_planks", has(virelithPlanks))
                .save(recipeOutput);
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.VIRELITH_SLAB.get(), virelithPlanks);

        buttonBuilder(ModBlocks.VIRELITH_BUTTON.get(), Ingredient.of(virelithPlanks)).group("virelith")
                .unlockedBy("has_virelith_planks", has(virelithPlanks))
                .save(recipeOutput);
        pressurePlate(recipeOutput, ModBlocks.VIRELITH_PRESSURE_PLATE.get(), virelithPlanks);

        fenceBuilder(ModBlocks.VIRELITH_FENCE.get(), Ingredient.of(virelithPlanks)).group("virelith")
                .unlockedBy("has_virelith_planks", has(virelithPlanks))
                .save(recipeOutput);
        fenceGateBuilder(ModBlocks.VIRELITH_FENCE_GATE.get(), Ingredient.of(virelithPlanks)).group("virelith")
                .unlockedBy("has_virelith_planks", has(virelithPlanks))
                .save(recipeOutput);
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.VIRELITH_WALL.get(), virelithPlanks);

        doorBuilder(ModBlocks.VIRELITH_DOOR.get(), Ingredient.of(virelithPlanks)).group("virelith")
                .unlockedBy("has_virelith_planks", has(virelithPlanks))
                .save(recipeOutput);
        trapdoorBuilder(ModBlocks.VIRELITH_TRAPDOOR.get(), Ingredient.of(virelithPlanks)).group("virelith")
                .unlockedBy("has_virelith_planks", has(virelithPlanks))
                .save(recipeOutput);

        // Umber wood set recipes
        var umberPlanks = ModBlocks.UMBER_PLANKS.get();

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

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.UMBER_WOOD.get(), 3)
                .define('#', ModBlocks.UMBER_LOG.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_umber_log", has(ModBlocks.UMBER_LOG.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRIPPED_UMBER_WOOD.get(), 3)
                .define('#', ModBlocks.STRIPPED_UMBER_LOG.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_stripped_umber_log", has(ModBlocks.STRIPPED_UMBER_LOG.get()))
                .save(recipeOutput);

        stairBuilder(ModBlocks.UMBER_STAIRS.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(recipeOutput);
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.UMBER_SLAB.get(), umberPlanks);

        buttonBuilder(ModBlocks.UMBER_BUTTON.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(recipeOutput);
        pressurePlate(recipeOutput, ModBlocks.UMBER_PRESSURE_PLATE.get(), umberPlanks);

        fenceBuilder(ModBlocks.UMBER_FENCE.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(recipeOutput);
        fenceGateBuilder(ModBlocks.UMBER_FENCE_GATE.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(recipeOutput);
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.UMBER_WALL.get(), umberPlanks);

        doorBuilder(ModBlocks.UMBER_DOOR.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(recipeOutput);
        trapdoorBuilder(ModBlocks.UMBER_TRAPDOOR.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(recipeOutput);
    }

    // Helper for generating ore smelting recipes
    protected static void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory,
                                      ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new,
                pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    // Helper for generating ore blasting recipes
    protected static void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory,
                                      ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new,
                pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    // Shared implementation for smelting and blasting recipe generation
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