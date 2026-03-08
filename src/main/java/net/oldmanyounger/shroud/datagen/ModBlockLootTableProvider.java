package net.oldmanyounger.shroud.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.item.ModItems;

import java.util.Set;
import java.util.function.Supplier;

/** Generates block loot tables for all Shroud blocks */
public class ModBlockLootTableProvider extends BlockLootSubProvider {

    /** Creates the block loot provider using all available feature flags */
    protected ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    // Future staging for gravel-style rare drop logic:
    // - Set chance > 0 to enable the rare drop path.
    // - Swap this supplier to your custom item later.
    private static final Supplier<ItemLike> SCULK_GRAVEL_RARE_DROP_ITEM = () -> Blocks.AIR.asItem(); // placeholder; replace later
    private static final float SCULK_GRAVEL_RARE_DROP_CHANCE = 0.0F; // currently always drops itself

    /** Defines drop behavior for each registered Shroud block */
    @Override
    protected void generate() {

        add(ModBlocks.SCULK_GRASS.get(),
                block -> createSingleItemTableWithSilkTouch(block, Blocks.SCULK));

        add(ModBlocks.SCULK_GRAVEL.get(),
                this::createSculkGravelDropTable);

        dropSelf(ModBlocks.SCULK_STONE.get());
        dropSelf(ModBlocks.SCULK_DEEPSLATE.get());
        add(ModBlocks.GLOOMSTONE.get(), block -> createSingleItemTableWithSilkTouch(block, ModItems.GLOOMSTONE_DUST.get()));

        add(ModBlocks.SCULK_BULB.get(), block -> createShearsOnlyDrop(ModBlocks.SCULK_BULB.get()));
        add(ModBlocks.GHOST_BLOOM.get(), block -> createShearsOnlyDrop(ModBlocks.GHOST_BLOOM.get()));
        add(ModBlocks.SCULK_VINES.get(), block -> noDrop());
        add(ModBlocks.SCULK_VINES_PLANT.get(), block -> noDrop());

        dropSelf(ModBlocks.SCULK_EMITTER.get());

        // Eventide block drops itself
        dropSelf(ModBlocks.EVENTIDE_BLOCK.get());

        // Eventide ore drops raw_eventide (with silk touch / fortune behavior via createOreDrop)
        add(ModBlocks.EVENTIDE_ORE.get(),
                block -> createOreDrop(ModBlocks.EVENTIDE_ORE.get(), ModItems.RAW_EVENTIDE.get()));
        add(ModBlocks.EVENTIDE_DEEPSLATE_ORE.get(),
                block -> createOreDrop(ModBlocks.EVENTIDE_DEEPSLATE_ORE.get(), ModItems.RAW_EVENTIDE.get()));
        add(ModBlocks.SCULK_STONE_EVENTIDE_ORE.get(),
                block -> createOreDrop(ModBlocks.SCULK_STONE_EVENTIDE_ORE.get(), ModItems.RAW_EVENTIDE.get()));
        add(ModBlocks.SCULK_DEEPSLATE_EVENTIDE_ORE.get(),
                block -> createOreDrop(ModBlocks.SCULK_DEEPSLATE_EVENTIDE_ORE.get(), ModItems.RAW_EVENTIDE.get()));

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

        // Registers simple self-dropping behavior for core Umber wood blocks and sapling
        this.dropSelf(ModBlocks.UMBER_LOG.get());
        this.dropSelf(ModBlocks.UMBER_WOOD.get());
        this.dropSelf(ModBlocks.STRIPPED_UMBER_LOG.get());
        this.dropSelf(ModBlocks.STRIPPED_UMBER_WOOD.get());
        this.dropSelf(ModBlocks.UMBER_PLANKS.get());
        this.dropSelf(ModBlocks.UMBER_SAPLING.get());

        // Configures Umber leaves to drop saplings and sticks using standard leaf chances
        this.add(ModBlocks.UMBER_LEAVES.get(),
                block -> createLeavesDrops(block, ModBlocks.UMBER_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));

        // Registers self-dropping behavior and special slab handling for Umber wood variants
        dropSelf(ModBlocks.UMBER_STAIRS.get());
        add(ModBlocks.UMBER_SLAB.get(), block -> createSlabItemTable(ModBlocks.UMBER_SLAB.get()));

        dropSelf(ModBlocks.UMBER_BUTTON.get());
        dropSelf(ModBlocks.UMBER_PRESSURE_PLATE.get());

        dropSelf(ModBlocks.UMBER_FENCE.get());
        dropSelf(ModBlocks.UMBER_FENCE_GATE.get());
        dropSelf(ModBlocks.UMBER_WALL.get());

        // Configures Umber trapdoor and door to use appropriate loot tables
        dropSelf(ModBlocks.UMBER_TRAPDOOR.get());
        add(ModBlocks.UMBER_DOOR.get(), block -> createDoorTable(ModBlocks.UMBER_DOOR.get()));

        // Configures the Sculk portal to drop nothing
        this.add(ModBlocks.SCULK_PORTAL.get(), block -> noDrop());

        // Configures the projected light source to drop nothing
        this.add(ModBlocks.PROJECTED_LIGHT.get(), noDrop());
    }

    private LootTable.Builder createSculkGravelDropTable(Block block) {
        // Current behavior: always drop itself.
        if (SCULK_GRAVEL_RARE_DROP_CHANCE <= 0.0F) {
            return createSingleItemTable(block);
        }

        // Future behavior: rare item drop at configured chance, otherwise drop self.
        return LootTable.lootTable().withPool(
                LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(applyExplosionCondition(
                                block,
                                AlternativesEntry.alternatives(
                                        LootItem.lootTableItem(SCULK_GRAVEL_RARE_DROP_ITEM.get())
                                                .when(LootItemRandomChanceCondition.randomChance(SCULK_GRAVEL_RARE_DROP_CHANCE)),
                                        LootItem.lootTableItem(block)
                                )
                        ))
        );
    }

    /** Returns all known Shroud blocks to ensure loot tables are generated for each one */
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}