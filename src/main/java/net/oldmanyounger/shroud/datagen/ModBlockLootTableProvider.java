package net.oldmanyounger.shroud.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.item.ModItems;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Generates block loot tables for all Shroud blocks.
 *
 * <p>This provider defines how each registered block should drop when broken,
 * including self-drops, silk-touch behavior, ore drops, leaf drops, and blocks
 * that intentionally drop nothing. It keeps Shroud's block drop behavior in one
 * place so the generated loot tables remain aligned with the mod's content and
 * design expectations.
 *
 * <p>In the broader context of the project, this class is part of the data
 * generation layer that converts Java-side block intent into the JSON loot table
 * resources Minecraft actually consumes at runtime.
 */
public class ModBlockLootTableProvider extends BlockLootSubProvider {

    // Placeholder supplier for a future rare sculk gravel drop
    private static final Supplier<ItemLike> SCULK_GRAVEL_RARE_DROP_ITEM = () -> Blocks.AIR.asItem();

    // Chance for the future rare sculk gravel drop; zero means the block always drops itself
    private static final float SCULK_GRAVEL_RARE_DROP_CHANCE = 0.0F;

    // Creates the block loot table provider with all feature flags enabled
    protected ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    // Declares drop behavior for every Shroud block that needs a loot table
    @Override
    protected void generate() {
        // Terrain and utility blocks
        add(ModBlocks.SCULK_GRASS.get(), block -> createSingleItemTableWithSilkTouch(block, Blocks.SCULK));
        add(ModBlocks.SCULK_GRAVEL.get(), this::createSculkGravelDropTable);
        add(ModBlocks.GLOOMSTONE.get(), block -> createGloomstoneLikeDrops(block, ModItems.GLOOMSTONE_DUST.get()));

        add(ModBlocks.SCULK_STONE.get(), block -> createSingleItemTable(ModBlocks.SCULK_COBBLESTONE.get()));
        dropSelf(ModBlocks.SCULK_COBBLESTONE.get());
        dropSelf(ModBlocks.SCULK_STONE_BRICKS.get());
        dropSelf(ModBlocks.CRACKED_SCULK_STONE_BRICKS.get());
        dropSelf(ModBlocks.CHISELED_SCULK_STONE_BRICKS.get());

        add(ModBlocks.SCULK_DEEPSLATE.get(), block -> createSingleItemTable(ModBlocks.COBBLED_SCULK_DEEPSLATE.get()));
        dropSelf(ModBlocks.COBBLED_SCULK_DEEPSLATE.get());
        dropSelf(ModBlocks.SCULK_DEEPSLATE_BRICKS.get());
        dropSelf(ModBlocks.CRACKED_SCULK_DEEPSLATE_BRICKS.get());
        dropSelf(ModBlocks.SCULK_DEEPSLATE_TILES.get());
        dropSelf(ModBlocks.CRACKED_SCULK_DEEPSLATE_TILES.get());

        dropSelf(ModBlocks.SCULK_COBBLESTONE_STAIRS.get());
        add(ModBlocks.SCULK_COBBLESTONE_SLAB.get(), block -> createSlabItemTable(ModBlocks.SCULK_COBBLESTONE_SLAB.get()));
        dropSelf(ModBlocks.SCULK_COBBLESTONE_WALL.get());

        dropSelf(ModBlocks.SCULK_STONE_BRICK_STAIRS.get());
        add(ModBlocks.SCULK_STONE_BRICK_SLAB.get(), block -> createSlabItemTable(ModBlocks.SCULK_STONE_BRICK_SLAB.get()));
        dropSelf(ModBlocks.SCULK_STONE_BRICK_WALL.get());

        dropSelf(ModBlocks.COBBLED_SCULK_DEEPSLATE_STAIRS.get());
        add(ModBlocks.COBBLED_SCULK_DEEPSLATE_SLAB.get(), block -> createSlabItemTable(ModBlocks.COBBLED_SCULK_DEEPSLATE_SLAB.get()));
        dropSelf(ModBlocks.COBBLED_SCULK_DEEPSLATE_WALL.get());

        dropSelf(ModBlocks.SCULK_DEEPSLATE_BRICK_STAIRS.get());
        add(ModBlocks.SCULK_DEEPSLATE_BRICK_SLAB.get(), block -> createSlabItemTable(ModBlocks.SCULK_DEEPSLATE_BRICK_SLAB.get()));
        dropSelf(ModBlocks.SCULK_DEEPSLATE_BRICK_WALL.get());

        dropSelf(ModBlocks.SCULK_DEEPSLATE_TILE_STAIRS.get());
        add(ModBlocks.SCULK_DEEPSLATE_TILE_SLAB.get(), block -> createSlabItemTable(ModBlocks.SCULK_DEEPSLATE_TILE_SLAB.get()));
        dropSelf(ModBlocks.SCULK_DEEPSLATE_TILE_WALL.get());

        // Plants and flora
        add(ModBlocks.SCULK_BULB.get(), block -> createShearsOnlyDrop(ModBlocks.SCULK_BULB.get()));
        add(ModBlocks.GHOST_BLOOM.get(), block -> createShearsOnlyDrop(ModBlocks.GHOST_BLOOM.get()));
        add(ModBlocks.SCULK_VINES.get(), block -> noDrop());
        add(ModBlocks.SCULK_VINES_PLANT.get(), block -> noDrop());

        // Technical blocks
        dropSelf(ModBlocks.SCULK_EMITTER.get());
        add(ModBlocks.SCULK_PORTAL.get(), block -> noDrop());
        add(ModBlocks.PROJECTED_LIGHT.get(), noDrop());

        // Eventide blocks and ores
        dropSelf(ModBlocks.EVENTIDE_BLOCK.get());
        add(ModBlocks.EVENTIDE_ORE.get(), block -> createOreDrop(ModBlocks.EVENTIDE_ORE.get(), ModItems.RAW_EVENTIDE.get()));
        add(ModBlocks.EVENTIDE_DEEPSLATE_ORE.get(), block -> createOreDrop(ModBlocks.EVENTIDE_DEEPSLATE_ORE.get(), ModItems.RAW_EVENTIDE.get()));
        add(ModBlocks.SCULK_STONE_EVENTIDE_ORE.get(), block -> createOreDrop(ModBlocks.SCULK_STONE_EVENTIDE_ORE.get(), ModItems.RAW_EVENTIDE.get()));
        add(ModBlocks.SCULK_DEEPSLATE_EVENTIDE_ORE.get(), block -> createOreDrop(ModBlocks.SCULK_DEEPSLATE_EVENTIDE_ORE.get(), ModItems.RAW_EVENTIDE.get()));

        // Virelith wood set
        dropSelf(ModBlocks.VIRELITH_LOG.get());
        dropSelf(ModBlocks.VIRELITH_WOOD.get());
        dropSelf(ModBlocks.STRIPPED_VIRELITH_LOG.get());
        dropSelf(ModBlocks.STRIPPED_VIRELITH_WOOD.get());
        dropSelf(ModBlocks.VIRELITH_PLANKS.get());
        dropSelf(ModBlocks.VIRELITH_SAPLING.get());
        add(ModBlocks.VIRELITH_LEAVES.get(),
                block -> createLeavesDrops(block, ModBlocks.VIRELITH_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        dropSelf(ModBlocks.VIRELITH_STAIRS.get());
        add(ModBlocks.VIRELITH_SLAB.get(), block -> createSlabItemTable(ModBlocks.VIRELITH_SLAB.get()));
        dropSelf(ModBlocks.VIRELITH_BUTTON.get());
        dropSelf(ModBlocks.VIRELITH_PRESSURE_PLATE.get());
        dropSelf(ModBlocks.VIRELITH_FENCE.get());
        dropSelf(ModBlocks.VIRELITH_FENCE_GATE.get());
        dropSelf(ModBlocks.VIRELITH_WALL.get());
        dropSelf(ModBlocks.VIRELITH_TRAPDOOR.get());
        add(ModBlocks.VIRELITH_DOOR.get(), block -> createDoorTable(ModBlocks.VIRELITH_DOOR.get()));

        // Umber wood set
        dropSelf(ModBlocks.UMBER_LOG.get());
        dropSelf(ModBlocks.UMBER_WOOD.get());
        dropSelf(ModBlocks.STRIPPED_UMBER_LOG.get());
        dropSelf(ModBlocks.STRIPPED_UMBER_WOOD.get());
        dropSelf(ModBlocks.UMBER_PLANKS.get());
        dropSelf(ModBlocks.UMBER_SAPLING.get());
        add(ModBlocks.UMBER_LEAVES.get(),
                block -> createLeavesDrops(block, ModBlocks.UMBER_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        dropSelf(ModBlocks.UMBER_STAIRS.get());
        add(ModBlocks.UMBER_SLAB.get(), block -> createSlabItemTable(ModBlocks.UMBER_SLAB.get()));
        dropSelf(ModBlocks.UMBER_BUTTON.get());
        dropSelf(ModBlocks.UMBER_PRESSURE_PLATE.get());
        dropSelf(ModBlocks.UMBER_FENCE.get());
        dropSelf(ModBlocks.UMBER_FENCE_GATE.get());
        dropSelf(ModBlocks.UMBER_WALL.get());
        dropSelf(ModBlocks.UMBER_TRAPDOOR.get());
        add(ModBlocks.UMBER_DOOR.get(), block -> createDoorTable(ModBlocks.UMBER_DOOR.get()));

        // Reliquary and pedestal for ritual crafting
        dropSelf(ModBlocks.CORRUPTED_RELIQUARY.get());
        dropSelf(ModBlocks.BINDING_PEDESTAL.get());
    }

    // Creates the special drop table for sculk gravel, with support for a future rare-drop path
    private LootTable.Builder createSculkGravelDropTable(Block block) {
        // Current behavior always returns the block itself
        if (SCULK_GRAVEL_RARE_DROP_CHANCE <= 0.0F) {
            return createSingleItemTable(block);
        }

        // Future behavior can return a rare item instead of the block at a configurable chance
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

    private LootTable.Builder createGloomstoneLikeDrops(Block block, ItemLike dustItem) {
        return createSilkTouchDispatchTable(
                block,
                applyExplosionDecay(
                        block,
                        LootItem.lootTableItem(dustItem)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F)))
                                .apply(ApplyBonusCount.addUniformBonusCount(
                                        registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE)
                                ))
                )
        );
    }

    // Returns every registered Shroud block so a loot table can be generated where needed
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}