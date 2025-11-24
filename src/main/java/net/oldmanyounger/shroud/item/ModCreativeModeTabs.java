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

/** Declares and registers the Shroud mod’s Creative Mode tabs */
public class ModCreativeModeTabs {

    /** Central registry for all Creative Mode tabs owned by the Shroud mod */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Shroud.MOD_ID);

    /** Main Shroud tab displaying all Sculk wood-set blocks */
    public static final Supplier<CreativeModeTab> SHROUD_BLOCK_TAB =
            CREATIVE_MODE_TABS.register("shroud_blocks",
                    () -> CreativeModeTab.builder()
                            .icon(() -> new ItemStack(ModBlocks.SCULK_LOG.get()))
                            .title(Component.translatable("creativetab.shroud.shroud_blocks"))
                            .displayItems((params, output) -> {

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
                            })
                            .build()
            );

    /** Registers the Creative Mode tabs to the NeoForge mod event bus */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
