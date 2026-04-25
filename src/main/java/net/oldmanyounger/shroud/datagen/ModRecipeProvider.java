package net.oldmanyounger.shroud.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.item.ModItems;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generates crafting, smelting, blasting, and smithing recipes for Shroud.
 *
 * <p>This provider defines recipes for core materials, utility blocks, Eventide equipment,
 * and custom wood sets while keeping generation logic centralized for maintainability.
 *
 * <p>In the broader context of the project, this class transforms registered gameplay content
 * into datapack recipe assets consumed by Minecraft at runtime.
 */
public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Creates the recipe provider using the shared registry lookup
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    // ==================================
    //  RECIPE REGISTRATION
    // ==================================

    // Registers all Shroud recipes emitted during datagen
    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        RecipeOutput craftingRecipeOutput = withPrefixedPath(recipeOutput, "crafting/");

        // Eventide smeltable source set
        List<ItemLike> eventideSmeltables = List.of(
                ModItems.RAW_EVENTIDE,
                ModBlocks.EVENTIDE_ORE,
                ModBlocks.EVENTIDE_DEEPSLATE_ORE
        );

        // Core materials and utility recipes
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GLOOMSTONE.get())
                .pattern("DD")
                .pattern("DD")
                .define('D', ModItems.GLOOMSTONE_DUST.get())
                .unlockedBy("has_gloomstone_dust", has(ModItems.GLOOMSTONE_DUST.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.EVENTIDE_BLOCK.get())
                .pattern("EEE")
                .pattern("EEE")
                .pattern("EEE")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT))
                .save(craftingRecipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.EVENTIDE_INGOT.get(), 9)
                .requires(ModBlocks.EVENTIDE_BLOCK)
                .unlockedBy("has_eventide_block", has(ModBlocks.EVENTIDE_BLOCK))
                .save(craftingRecipeOutput, "shroud:eventide_ingot_from_eventide_block");

        // Sculk stone and deepslate block chains
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_STONE_BRICKS.get(), 4)
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.SCULK_STONE.get())
                .unlockedBy("has_sculk_stone", has(ModBlocks.SCULK_STONE.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHISELED_SCULK_STONE_BRICKS.get())
                .pattern("#")
                .pattern("#")
                .define('#', ModBlocks.SCULK_STONE_BRICKS.get())
                .unlockedBy("has_sculk_stone_bricks", has(ModBlocks.SCULK_STONE_BRICKS.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_DEEPSLATE_BRICKS.get(), 4)
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.SCULK_DEEPSLATE.get())
                .unlockedBy("has_sculk_deepslate", has(ModBlocks.SCULK_DEEPSLATE.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_DEEPSLATE_TILES.get(), 4)
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.SCULK_DEEPSLATE_BRICKS.get())
                .unlockedBy("has_sculk_deepslate_bricks", has(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()))
                .save(craftingRecipeOutput);

        // Furnace conversions and cracked variants
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModBlocks.SCULK_COBBLESTONE.get()),
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.SCULK_STONE.get(),
                        0.1f,
                        200
                )
                .unlockedBy("has_sculk_cobblestone", has(ModBlocks.SCULK_COBBLESTONE.get()))
                .save(craftingRecipeOutput, Shroud.MOD_ID + ":sculk_stone_from_smelting_sculk_cobblestone");

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModBlocks.COBBLED_SCULK_DEEPSLATE.get()),
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.SCULK_DEEPSLATE.get(),
                        0.1f,
                        200
                )
                .unlockedBy("has_cobbled_sculk_deepslate", has(ModBlocks.COBBLED_SCULK_DEEPSLATE.get()))
                .save(craftingRecipeOutput, Shroud.MOD_ID + ":sculk_deepslate_from_smelting_cobbled_sculk_deepslate");

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModBlocks.SCULK_STONE_BRICKS.get()),
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.CRACKED_SCULK_STONE_BRICKS.get(),
                        0.1f,
                        200
                )
                .unlockedBy("has_sculk_stone_bricks", has(ModBlocks.SCULK_STONE_BRICKS.get()))
                .save(craftingRecipeOutput, Shroud.MOD_ID + ":cracked_sculk_stone_bricks_from_smelting");

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()),
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.CRACKED_SCULK_DEEPSLATE_BRICKS.get(),
                        0.1f,
                        200
                )
                .unlockedBy("has_sculk_deepslate_bricks", has(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()))
                .save(craftingRecipeOutput, Shroud.MOD_ID + ":cracked_sculk_deepslate_bricks_from_smelting");

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModBlocks.SCULK_DEEPSLATE_TILES.get()),
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.CRACKED_SCULK_DEEPSLATE_TILES.get(),
                        0.1f,
                        200
                )
                .unlockedBy("has_sculk_deepslate_tiles", has(ModBlocks.SCULK_DEEPSLATE_TILES.get()))
                .save(craftingRecipeOutput, Shroud.MOD_ID + ":cracked_sculk_deepslate_tiles_from_smelting");

        // Sculk structure pieces
        stairBuilder(ModBlocks.SCULK_COBBLESTONE_STAIRS.get(), Ingredient.of(ModBlocks.SCULK_COBBLESTONE.get()))
                .unlockedBy("has_sculk_cobblestone", has(ModBlocks.SCULK_COBBLESTONE.get()))
                .save(craftingRecipeOutput);
        slab(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_COBBLESTONE_SLAB.get(), ModBlocks.SCULK_COBBLESTONE.get());
        wall(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_COBBLESTONE_WALL.get(), ModBlocks.SCULK_COBBLESTONE.get());

        stairBuilder(ModBlocks.SCULK_STONE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.SCULK_STONE_BRICKS.get()))
                .unlockedBy("has_sculk_stone_bricks", has(ModBlocks.SCULK_STONE_BRICKS.get()))
                .save(craftingRecipeOutput);
        slab(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_STONE_BRICK_SLAB.get(), ModBlocks.SCULK_STONE_BRICKS.get());
        wall(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_STONE_BRICK_WALL.get(), ModBlocks.SCULK_STONE_BRICKS.get());

        stairBuilder(ModBlocks.COBBLED_SCULK_DEEPSLATE_STAIRS.get(), Ingredient.of(ModBlocks.SCULK_DEEPSLATE.get()))
                .unlockedBy("has_sculk_deepslate", has(ModBlocks.SCULK_DEEPSLATE.get()))
                .save(craftingRecipeOutput);
        slab(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.COBBLED_SCULK_DEEPSLATE_SLAB.get(), ModBlocks.SCULK_DEEPSLATE.get());
        wall(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.COBBLED_SCULK_DEEPSLATE_WALL.get(), ModBlocks.SCULK_DEEPSLATE.get());

        stairBuilder(ModBlocks.SCULK_DEEPSLATE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()))
                .unlockedBy("has_sculk_deepslate_bricks", has(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()))
                .save(craftingRecipeOutput);
        slab(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_DEEPSLATE_BRICK_SLAB.get(), ModBlocks.SCULK_DEEPSLATE_BRICKS.get());
        wall(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_DEEPSLATE_BRICK_WALL.get(), ModBlocks.SCULK_DEEPSLATE_BRICKS.get());

        stairBuilder(ModBlocks.SCULK_DEEPSLATE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.SCULK_DEEPSLATE_TILES.get()))
                .unlockedBy("has_sculk_deepslate_tiles", has(ModBlocks.SCULK_DEEPSLATE_TILES.get()))
                .save(craftingRecipeOutput);
        slab(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_DEEPSLATE_TILE_SLAB.get(), ModBlocks.SCULK_DEEPSLATE_TILES.get());
        wall(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.SCULK_DEEPSLATE_TILE_WALL.get(), ModBlocks.SCULK_DEEPSLATE_TILES.get());

        // Eventide processing recipes
        oreSmelting(craftingRecipeOutput, eventideSmeltables, RecipeCategory.MISC, ModItems.EVENTIDE_INGOT.get(), 0.25f, 200, "eventide");
        oreBlasting(craftingRecipeOutput, eventideSmeltables, RecipeCategory.MISC, ModItems.EVENTIDE_INGOT.get(), 0.25f, 100, "eventide");
        trimSmithing(craftingRecipeOutput, ModItems.EVENTIDE_SMITHING_TEMPLATE.get(), ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "eventide"));

        // Ritual infrastructure block recipes
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.CORRUPTED_RELIQUARY.get())
                .pattern("SSS")
                .pattern("TCT")
                .pattern("TTT")
                .define('S', Items.SCULK)
                .define('C', Items.SCULK_CATALYST)
                .define('T', ModBlocks.SCULK_STONE_BRICKS.get())
                .unlockedBy("has_sculk_catalyst", has(Items.SCULK_CATALYST))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.BINDING_PEDESTAL.get())
                .pattern("SSS")
                .pattern(" D ")
                .pattern("DDD")
                .define('S', Items.SCULK)
                .define('D', ModBlocks.SCULK_STONE_BRICKS.get())
                .unlockedBy("has_sculk_deepslate", has(ModBlocks.SCULK_DEEPSLATE.get()))
                .save(craftingRecipeOutput);

        // Gloam sugar
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.GLOAM_SUGAR.get())
                .requires(ModBlocks.GLOAMCANE.get())
                .unlockedBy("has_gloamcane", has(ModBlocks.GLOAMCANE.get()))
                .save(craftingRecipeOutput);

        // Eventide tools and armor recipes
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EVENTIDE_SWORD.get())
                .pattern("E")
                .pattern("E")
                .pattern("S")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.EVENTIDE_PICKAXE.get())
                .pattern("EEE")
                .pattern(" S ")
                .pattern(" S ")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.EVENTIDE_SHOVEL.get())
                .pattern("E")
                .pattern("S")
                .pattern("S")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.EVENTIDE_AXE.get())
                .pattern("EE")
                .pattern("ES")
                .pattern(" S")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.EVENTIDE_HOE.get())
                .pattern("EE")
                .pattern(" S")
                .pattern(" S")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EVENTIDE_BOW.get())
                .pattern(" ES")
                .pattern("E S")
                .pattern(" ES")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .define('S', Items.STRING)
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EVENTIDE_HELMET.get())
                .pattern("EEE")
                .pattern("E E")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EVENTIDE_CHESTPLATE.get())
                .pattern("E E")
                .pattern("EEE")
                .pattern("EEE")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EVENTIDE_LEGGINGS.get())
                .pattern("EEE")
                .pattern("E E")
                .pattern("E E")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.EVENTIDE_BOOTS.get())
                .pattern("E E")
                .pattern("E E")
                .define('E', ModItems.EVENTIDE_INGOT.get())
                .unlockedBy("has_eventide_ingot", has(ModItems.EVENTIDE_INGOT.get()))
                .save(craftingRecipeOutput);

        // Virelith wood set recipes
        ItemLike virelithPlanks = ModBlocks.VIRELITH_PLANKS.get();

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
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.VIRELITH_WOOD.get(), 3)
                .define('#', ModBlocks.VIRELITH_LOG.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_virelith_log", has(ModBlocks.VIRELITH_LOG.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRIPPED_VIRELITH_WOOD.get(), 3)
                .define('#', ModBlocks.STRIPPED_VIRELITH_LOG.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_stripped_virelith_log", has(ModBlocks.STRIPPED_VIRELITH_LOG.get()))
                .save(craftingRecipeOutput);

        stairBuilder(ModBlocks.VIRELITH_STAIRS.get(), Ingredient.of(virelithPlanks)).group("virelith")
                .unlockedBy("has_virelith_planks", has(virelithPlanks))
                .save(craftingRecipeOutput);
        slab(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.VIRELITH_SLAB.get(), virelithPlanks);

        buttonBuilder(ModBlocks.VIRELITH_BUTTON.get(), Ingredient.of(virelithPlanks)).group("virelith")
                .unlockedBy("has_virelith_planks", has(virelithPlanks))
                .save(craftingRecipeOutput);
        pressurePlate(craftingRecipeOutput, ModBlocks.VIRELITH_PRESSURE_PLATE.get(), virelithPlanks);

        fenceBuilder(ModBlocks.VIRELITH_FENCE.get(), Ingredient.of(virelithPlanks)).group("virelith")
                .unlockedBy("has_virelith_planks", has(virelithPlanks))
                .save(craftingRecipeOutput);
        fenceGateBuilder(ModBlocks.VIRELITH_FENCE_GATE.get(), Ingredient.of(virelithPlanks)).group("virelith")
                .unlockedBy("has_virelith_planks", has(virelithPlanks))
                .save(craftingRecipeOutput);
        wall(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.VIRELITH_WALL.get(), virelithPlanks);

        doorBuilder(ModBlocks.VIRELITH_DOOR.get(), Ingredient.of(virelithPlanks)).group("virelith")
                .unlockedBy("has_virelith_planks", has(virelithPlanks))
                .save(craftingRecipeOutput);
        trapdoorBuilder(ModBlocks.VIRELITH_TRAPDOOR.get(), Ingredient.of(virelithPlanks)).group("virelith")
                .unlockedBy("has_virelith_planks", has(virelithPlanks))
                .save(craftingRecipeOutput);

        // Umber wood set recipes
        ItemLike umberPlanks = ModBlocks.UMBER_PLANKS.get();

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
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.UMBER_WOOD.get(), 3)
                .define('#', ModBlocks.UMBER_LOG.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_umber_log", has(ModBlocks.UMBER_LOG.get()))
                .save(craftingRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRIPPED_UMBER_WOOD.get(), 3)
                .define('#', ModBlocks.STRIPPED_UMBER_LOG.get())
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_stripped_umber_log", has(ModBlocks.STRIPPED_UMBER_LOG.get()))
                .save(craftingRecipeOutput);

        stairBuilder(ModBlocks.UMBER_STAIRS.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(craftingRecipeOutput);
        slab(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.UMBER_SLAB.get(), umberPlanks);

        buttonBuilder(ModBlocks.UMBER_BUTTON.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(craftingRecipeOutput);
        pressurePlate(craftingRecipeOutput, ModBlocks.UMBER_PRESSURE_PLATE.get(), umberPlanks);

        fenceBuilder(ModBlocks.UMBER_FENCE.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(craftingRecipeOutput);
        fenceGateBuilder(ModBlocks.UMBER_FENCE_GATE.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(craftingRecipeOutput);
        wall(craftingRecipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.UMBER_WALL.get(), umberPlanks);

        doorBuilder(ModBlocks.UMBER_DOOR.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(craftingRecipeOutput);
        trapdoorBuilder(ModBlocks.UMBER_TRAPDOOR.get(), Ingredient.of(umberPlanks)).group("umber")
                .unlockedBy("has_umber_planks", has(umberPlanks))
                .save(craftingRecipeOutput);
    }

    // ==================================
    //  COOKING HELPERS
    // ==================================

    // Helper for generating ore smelting recipes
    protected static void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> ingredients, RecipeCategory category,
                                      ItemLike result, float experience, int cookingTime, String group) {
        oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new,
                ingredients, category, result, experience, cookingTime, group, "_from_smelting");
    }

    // Helper for generating ore blasting recipes
    protected static void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> ingredients, RecipeCategory category,
                                      ItemLike result, float experience, int cookingTime, String group) {
        oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new,
                ingredients, category, result, experience, cookingTime, group, "_from_blasting");
    }

    // Shared implementation for smelting and blasting recipe generation
    protected static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput,
                                                                       RecipeSerializer<T> cookingSerializer,
                                                                       AbstractCookingRecipe.Factory<T> factory,
                                                                       List<ItemLike> ingredients,
                                                                       RecipeCategory category,
                                                                       ItemLike result,
                                                                       float experience,
                                                                       int cookingTime,
                                                                       String group,
                                                                       String recipeNameSuffix) {
        for (ItemLike itemLike : ingredients) {
            SimpleCookingRecipeBuilder.generic(
                            Ingredient.of(itemLike),
                            category,
                            result,
                            experience,
                            cookingTime,
                            cookingSerializer,
                            factory
                    )
                    .group(group)
                    .unlockedBy(getHasName(itemLike), has(itemLike))
                    .save(recipeOutput, Shroud.MOD_ID + ":" + getItemName(result) + recipeNameSuffix + "_" + getItemName(itemLike));
        }
    }

    // ==================================
    //  OUTPUT WRAPPER
    // ==================================

    // Wraps recipe output so all recipe ids are emitted under a path prefix
    private static RecipeOutput withPrefixedPath(RecipeOutput delegate, String prefix) {
        return new RecipeOutput() {
            public void accept(ResourceLocation recipeId, Recipe<?> recipe, AdvancementHolder advancement, ICondition... conditions) {
                ResourceLocation prefixedId = ResourceLocation.fromNamespaceAndPath(
                        recipeId.getNamespace(),
                        prefix + recipeId.getPath()
                );

                delegate.accept(prefixedId, recipe, advancement, conditions);
            }

            @Override
            public Advancement.Builder advancement() {
                return delegate.advancement();
            }
        };
    }
}