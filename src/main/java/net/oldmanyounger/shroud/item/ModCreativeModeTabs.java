package net.oldmanyounger.shroud.item;

import net.minecraft.core.registries.Registries;
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

/** Declares and registers the Shroud mod’s Creative Mode tabs */
public class ModCreativeModeTabs {

    /** Central registry for all Creative Mode tabs owned by the Shroud mod */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(CREATIVE_MODE_TAB, Shroud.MOD_ID);

    /** Main Shroud tab displaying all Sculk wood-set blocks */
    public static final Supplier<CreativeModeTab> SHROUD_BLOCK_TAB = CREATIVE_MODE_TABS.register("shroud_blocks",
                    () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.SCULK_LOG.get()))
                            .title(Component.translatable("creativetab.shroud.shroud_blocks"))
                            .displayItems((params, output) -> {

                                // Sculk grass
                                output.accept(ModBlocks.SCULK_GRASS.get());
                                output.accept(ModBlocks.SCULK_GRAVEL.get());
                                output.accept(ModBlocks.SCULK_STONE.get());
                                output.accept(ModBlocks.SCULK_DEEPSLATE.get());

                                // Sculk bulb
                                output.accept(ModBlocks.SCULK_BULB.get());

                                output.accept(ModBlocks.SCULK_EMITTER.get());

                                // Eventide ore & blocks
                                output.accept(ModBlocks.EVENTIDE_ORE.get());
                                output.accept(ModBlocks.EVENTIDE_DEEPSLATE_ORE.get());
                                output.accept(ModBlocks.SCULK_STONE_EVENTIDE_ORE.get());
                                output.accept(ModBlocks.SCULK_DEEPSLATE_EVENTIDE_ORE.get());
                                output.accept(ModBlocks.EVENTIDE_BLOCK.get());

                                // Sculk logs and wood
                                output.accept(ModBlocks.SCULK_LOG.get());
                                output.accept(ModBlocks.SCULK_WOOD.get());
                                output.accept(ModBlocks.STRIPPED_SCULK_LOG.get());
                                output.accept(ModBlocks.STRIPPED_SCULK_WOOD.get());

                                // Sculk planks, leaves and sapling
                                output.accept(ModBlocks.SCULK_PLANKS.get());
                                output.accept(ModBlocks.SCULK_LEAVES.get());
                                output.accept(ModBlocks.SCULK_SAPLING.get());

                                // Sculk plank variants
                                output.accept(ModBlocks.SCULK_STAIRS.get());
                                output.accept(ModBlocks.SCULK_SLAB.get());

                                // Sculk structural blocks
                                output.accept(ModBlocks.SCULK_FENCE.get());
                                output.accept(ModBlocks.SCULK_FENCE_GATE.get());
                                output.accept(ModBlocks.SCULK_WALL.get());

                                // Sculk door and trapdoor
                                output.accept(ModBlocks.SCULK_DOOR.get());
                                output.accept(ModBlocks.SCULK_TRAPDOOR.get());

                                // Sculk redstone-interaction blocks
                                output.accept(ModBlocks.SCULK_PRESSURE_PLATE.get());
                                output.accept(ModBlocks.SCULK_BUTTON.get());

                                // Umber logs and wood
                                output.accept(ModBlocks.UMBER_LOG.get());
                                output.accept(ModBlocks.UMBER_WOOD.get());
                                output.accept(ModBlocks.STRIPPED_UMBER_LOG.get());
                                output.accept(ModBlocks.STRIPPED_UMBER_WOOD.get());

                                // Umber planks, leaves and sapling
                                output.accept(ModBlocks.UMBER_PLANKS.get());
                                output.accept(ModBlocks.UMBER_LEAVES.get());
                                output.accept(ModBlocks.UMBER_SAPLING.get());

                                // Umber plank variants
                                output.accept(ModBlocks.UMBER_STAIRS.get());
                                output.accept(ModBlocks.UMBER_SLAB.get());

                                // Umber structural blocks
                                output.accept(ModBlocks.UMBER_FENCE.get());
                                output.accept(ModBlocks.UMBER_FENCE_GATE.get());
                                output.accept(ModBlocks.UMBER_WALL.get());

                                // Umber door and trapdoor
                                output.accept(ModBlocks.UMBER_DOOR.get());
                                output.accept(ModBlocks.UMBER_TRAPDOOR.get());

                                // Umber redstone-interaction blocks
                                output.accept(ModBlocks.UMBER_PRESSURE_PLATE.get());
                                output.accept(ModBlocks.UMBER_BUTTON.get());

                                // Limbo blocks
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

    public static final Supplier<CreativeModeTab> SHROUD_ITEMS_TAB = CREATIVE_MODE_TABS.register("shroud_items",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.LIVING_SCULK_SPAWN_EGG.get()))
                            .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "shroud_blocks"))
                            .title(Component.translatable("creativetab.shroud.shroud_items"))
                            .displayItems((params, output) -> {
                                // Sculk pearl
                                output.accept(ModItems.SCULK_PEARL.get());

                                // Eventide ore
                                output.accept(ModItems.RAW_EVENTIDE.get());
                                output.accept(ModItems.EVENTIDE_INGOT.get());

                                // Eventide tools & weapons
                                output.accept(ModItems.EVENTIDE_SWORD.get());
                                output.accept(ModItems.EVENTIDE_PICKAXE.get());
                                output.accept(ModItems.EVENTIDE_SHOVEL.get());
                                output.accept(ModItems.EVENTIDE_AXE.get());
                                output.accept(ModItems.EVENTIDE_HOE.get());
                                output.accept(ModItems.EVENTIDE_BOW.get());

                                // Eventide armor
                                output.accept(ModItems.EVENTIDE_HELMET.get());
                                output.accept(ModItems.EVENTIDE_CHESTPLATE.get());
                                output.accept(ModItems.EVENTIDE_LEGGINGS.get());
                                output.accept(ModItems.EVENTIDE_BOOTS.get());

                                // Eventide trim
                                output.accept(ModItems.EVENTIDE_SMITHING_TEMPLATE.get());

                                // Spawn eggs
                                output.accept(ModItems.LIVING_SCULK_SPAWN_EGG);
                                output.accept(ModItems.UMBRAL_HOWLER_SPAWN_EGG);
                            }).build());

    /** Registers the Creative Mode tabs to the NeoForge mod event bus */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
