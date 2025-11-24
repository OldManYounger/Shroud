package net.oldmanyounger.shroud.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.oldmanyounger.shroud.block.ModBlocks;

import java.util.Set;

/** Generates block loot tables for all Shroud blocks */
public class ModBlockLootTableProvider extends BlockLootSubProvider {

    /** Creates the block loot provider using all available feature flags */
    protected ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    /** Defines drop behavior for each registered Shroud block */
    @Override
    protected void generate() {

        // Registers simple self-dropping behavior for core Sculk wood blocks and sapling
        this.dropSelf(ModBlocks.SCULK_LOG.get());
        this.dropSelf(ModBlocks.SCULK_WOOD.get());
        this.dropSelf(ModBlocks.STRIPPED_SCULK_LOG.get());
        this.dropSelf(ModBlocks.STRIPPED_SCULK_WOOD.get());
        this.dropSelf(ModBlocks.SCULK_PLANKS.get());
        this.dropSelf(ModBlocks.SCULK_SAPLING.get());

        // Configures Sculk leaves to drop saplings and sticks using standard leaf chances
        this.add(ModBlocks.SCULK_LEAVES.get(),
                block -> createLeavesDrops(block, ModBlocks.SCULK_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));

        // Registers self-dropping behavior and special slab handling for Sculk wood variants
        dropSelf(ModBlocks.SCULK_STAIRS.get());
        add(ModBlocks.SCULK_SLAB.get(), block -> createSlabItemTable(ModBlocks.SCULK_SLAB.get()));

        dropSelf(ModBlocks.SCULK_BUTTON.get());
        dropSelf(ModBlocks.SCULK_PRESSURE_PLATE.get());

        dropSelf(ModBlocks.SCULK_FENCE.get());
        dropSelf(ModBlocks.SCULK_FENCE_GATE.get());
        dropSelf(ModBlocks.SCULK_WALL.get());

        // Configures Sculk trapdoor and door to use appropriate loot tables
        dropSelf(ModBlocks.SCULK_TRAPDOOR.get());
        add(ModBlocks.SCULK_DOOR.get(), block -> createDoorTable(ModBlocks.SCULK_DOOR.get()));

        // Configures the Sculk portal to drop nothing
        this.add(ModBlocks.SCULK_PORTAL.get(), block -> noDrop());

    }

    /** Returns all known Shroud blocks to ensure loot tables are generated for each one */
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
