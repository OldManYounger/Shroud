package net.oldmanyounger.shroud.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

import java.util.function.Supplier;

import static net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB;

/**
 * Declares and registers the Creative Mode tabs used by Shroud.
 *
 * <p>This class defines the block-focused and item-focused tabs, controls their
 * icon/title ordering, and populates each tab with the mod's content in a
 * deliberate progression-friendly layout.
 *
 * <p>In the broader context of the project, this class is part of the content
 * presentation layer that makes Shroud's registered blocks and items discoverable
 * in-game through the Creative inventory UI.
 */
public class ModCreativeModeTabs {

    // Central registry for all Creative Mode tabs owned by the Shroud mod.
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(CREATIVE_MODE_TAB, Shroud.MOD_ID);

    // Main Shroud tab displaying all block content.
    public static final Supplier<CreativeModeTab> SHROUD_BLOCK_TAB = CREATIVE_MODE_TABS.register("shroud_blocks",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.VIRELITH_LOG.get()))
                    .title(Component.translatable("creativetab.shroud.shroud_blocks"))
                    .displayItems((params, output) -> {

                        // Sculk terrain basics.
                        output.accept(ModBlocks.SCULK_GRASS.get());
                        output.accept(ModBlocks.SCULK_GRAVEL.get());
                        output.accept(ModBlocks.SCULK_STONE.get());

                        // Sculk cobblestone set.
                        output.accept(ModBlocks.SCULK_COBBLESTONE.get());
                        output.accept(ModBlocks.SCULK_COBBLESTONE_STAIRS.get());
                        output.accept(ModBlocks.SCULK_COBBLESTONE_SLAB.get());
                        output.accept(ModBlocks.SCULK_COBBLESTONE_WALL.get());

                        // Sculk stone brick set.
                        output.accept(ModBlocks.SCULK_STONE_BRICKS.get());
                        output.accept(ModBlocks.CRACKED_SCULK_STONE_BRICKS.get());
                        output.accept(ModBlocks.CHISELED_SCULK_STONE_BRICKS.get());
                        output.accept(ModBlocks.SCULK_STONE_BRICK_STAIRS.get());
                        output.accept(ModBlocks.SCULK_STONE_BRICK_SLAB.get());
                        output.accept(ModBlocks.SCULK_STONE_BRICK_WALL.get());

                        // Sculk deepslate base and structural sets.
                        output.accept(ModBlocks.SCULK_DEEPSLATE.get());

                        output.accept(ModBlocks.COBBLED_SCULK_DEEPSLATE.get());
                        output.accept(ModBlocks.COBBLED_SCULK_DEEPSLATE_STAIRS.get());
                        output.accept(ModBlocks.COBBLED_SCULK_DEEPSLATE_SLAB.get());
                        output.accept(ModBlocks.COBBLED_SCULK_DEEPSLATE_WALL.get());

                        output.accept(ModBlocks.SCULK_DEEPSLATE_BRICKS.get());
                        output.accept(ModBlocks.CRACKED_SCULK_DEEPSLATE_BRICKS.get());
                        output.accept(ModBlocks.SCULK_DEEPSLATE_BRICK_STAIRS.get());
                        output.accept(ModBlocks.SCULK_DEEPSLATE_BRICK_SLAB.get());
                        output.accept(ModBlocks.SCULK_DEEPSLATE_BRICK_WALL.get());

                        output.accept(ModBlocks.SCULK_DEEPSLATE_TILES.get());
                        output.accept(ModBlocks.CRACKED_SCULK_DEEPSLATE_TILES.get());
                        output.accept(ModBlocks.SCULK_DEEPSLATE_TILE_STAIRS.get());
                        output.accept(ModBlocks.SCULK_DEEPSLATE_TILE_SLAB.get());
                        output.accept(ModBlocks.SCULK_DEEPSLATE_TILE_WALL.get());

                        // Sculk flora and utility blocks.
                        output.accept(ModBlocks.SCULK_BULB.get());
                        output.accept(ModBlocks.GHOST_BLOOM.get());
                        output.accept(ModBlocks.GLOAMCANE.get());
                        output.accept(ModBlocks.GLOOMSTONE.get());
                        output.accept(ModBlocks.SCULK_EMITTER.get());

                        // Eventide ores and storage block.
                        output.accept(ModBlocks.EVENTIDE_ORE.get());
                        output.accept(ModBlocks.EVENTIDE_DEEPSLATE_ORE.get());
                        output.accept(ModBlocks.SCULK_STONE_EVENTIDE_ORE.get());
                        output.accept(ModBlocks.SCULK_DEEPSLATE_EVENTIDE_ORE.get());
                        output.accept(ModBlocks.EVENTIDE_BLOCK.get());

                        // Virelith wood set.
                        output.accept(ModBlocks.VIRELITH_LOG.get());
                        output.accept(ModBlocks.VIRELITH_WOOD.get());
                        output.accept(ModBlocks.STRIPPED_VIRELITH_LOG.get());
                        output.accept(ModBlocks.STRIPPED_VIRELITH_WOOD.get());
                        output.accept(ModBlocks.VIRELITH_PLANKS.get());
                        output.accept(ModBlocks.VIRELITH_LEAVES.get());
                        output.accept(ModBlocks.VIRELITH_SAPLING.get());
                        output.accept(ModBlocks.VIRELITH_STAIRS.get());
                        output.accept(ModBlocks.VIRELITH_SLAB.get());
                        output.accept(ModBlocks.VIRELITH_FENCE.get());
                        output.accept(ModBlocks.VIRELITH_FENCE_GATE.get());
                        output.accept(ModBlocks.VIRELITH_WALL.get());
                        output.accept(ModBlocks.VIRELITH_DOOR.get());
                        output.accept(ModBlocks.VIRELITH_TRAPDOOR.get());
                        output.accept(ModBlocks.VIRELITH_PRESSURE_PLATE.get());
                        output.accept(ModBlocks.VIRELITH_BUTTON.get());

                        // Umber wood set.
                        output.accept(ModBlocks.UMBER_LOG.get());
                        output.accept(ModBlocks.UMBER_WOOD.get());
                        output.accept(ModBlocks.STRIPPED_UMBER_LOG.get());
                        output.accept(ModBlocks.STRIPPED_UMBER_WOOD.get());
                        output.accept(ModBlocks.UMBER_PLANKS.get());
                        output.accept(ModBlocks.UMBER_LEAVES.get());
                        output.accept(ModBlocks.UMBER_SAPLING.get());
                        output.accept(ModBlocks.UMBER_STAIRS.get());
                        output.accept(ModBlocks.UMBER_SLAB.get());
                        output.accept(ModBlocks.UMBER_FENCE.get());
                        output.accept(ModBlocks.UMBER_FENCE_GATE.get());
                        output.accept(ModBlocks.UMBER_WALL.get());
                        output.accept(ModBlocks.UMBER_DOOR.get());
                        output.accept(ModBlocks.UMBER_TRAPDOOR.get());
                        output.accept(ModBlocks.UMBER_PRESSURE_PLATE.get());
                        output.accept(ModBlocks.UMBER_BUTTON.get());

                        // Reliquary and pedestal for ritual crafting
                        output.accept(ModBlocks.CORRUPTED_RELIQUARY.get());
                        output.accept(ModBlocks.BINDING_PEDESTAL.get());

                        // Limbo structural set.
                        output.accept(ModBlocks.LIMBO_WALLPAPER_DIAMOND.get());
                        output.accept(ModBlocks.LIMBO_WALLPAPER_SEGMENTED.get());
                        output.accept(ModBlocks.LIMBO_CARPET.get());
                        output.accept(ModBlocks.LIMBO_CARPET_STAIRS.get());
                        output.accept(ModBlocks.LIMBO_CARPET_SLAB.get());
                        output.accept(ModBlocks.LIMBO_CEILING_TILE.get());
                        output.accept(ModBlocks.LIMBO_FLUORESCENT_LIGHT.get());
                    })
                    .build()
    );

    // Shroud tab displaying custom items, tools, armor, and spawn eggs.
    public static final Supplier<CreativeModeTab> SHROUD_ITEMS_TAB = CREATIVE_MODE_TABS.register("shroud_items",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.LIVING_SCULK_SPAWN_EGG.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "shroud_blocks"))
                    .title(Component.translatable("creativetab.shroud.shroud_items"))
                    .displayItems((params, output) -> {

                        // Core sculk materials and utility items.
                        output.accept(ModItems.SCULK_PEARL.get());
                        output.accept(ModItems.GLOOMSTONE_DUST.get());
                        output.accept(ModItems.TOTEM_OF_LAST_BREATH.get());
                        output.accept(ModItems.GLOOM_PULP.get());
                        output.accept(ModItems.GLOAM_SUGAR.get());

                        // Eventide raw and refined materials.
                        output.accept(ModItems.RAW_EVENTIDE.get());
                        output.accept(ModItems.EVENTIDE_INGOT.get());

                        // Eventide tools and weaponry.
                        output.accept(ModItems.EVENTIDE_SWORD.get());
                        output.accept(ModItems.EVENTIDE_PICKAXE.get());
                        output.accept(ModItems.EVENTIDE_SHOVEL.get());
                        output.accept(ModItems.EVENTIDE_AXE.get());
                        output.accept(ModItems.EVENTIDE_HOE.get());
                        output.accept(ModItems.EVENTIDE_BOW.get());

                        // Eventide armor set.
                        output.accept(ModItems.EVENTIDE_HELMET.get());
                        output.accept(ModItems.EVENTIDE_CHESTPLATE.get());
                        output.accept(ModItems.EVENTIDE_LEGGINGS.get());
                        output.accept(ModItems.EVENTIDE_BOOTS.get());

                        // Eventide smithing template.
                        output.accept(ModItems.EVENTIDE_SMITHING_TEMPLATE.get());

                        // Shroud spawn eggs.
                        output.accept(ModItems.LIVING_SCULK_SPAWN_EGG);
                        output.accept(ModItems.UMBRAL_HOWLER_SPAWN_EGG);
                        output.accept(ModItems.BLIGHTED_SHADE_SPAWN_EGG);
                    })
                    .build()
    );

    // Registers the Creative Mode tab deferred register on the mod event bus.
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}