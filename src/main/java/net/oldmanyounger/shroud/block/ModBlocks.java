package net.oldmanyounger.shroud.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.custom.*;
import net.oldmanyounger.shroud.item.ModItems;
import net.oldmanyounger.shroud.portal.ShroudPortalBlock;
import net.oldmanyounger.shroud.worldgen.tree.ModTreeGrowers;

import java.util.function.Supplier;

/**
 * Central registry class for all Shroud blocks and their associated block items.
 *
 * <p>This file declares every block the mod adds, from world-generation terrain
 * and decorative flora to wood sets, technical helper blocks, and special portal
 * functionality. Where appropriate, registrations also automatically create the
 * corresponding {@link BlockItem} so the block can exist both in-world and in
 * inventories.
 *
 * <p>In the broader context of the project, this class serves as the foundation
 * of Shroud's block content pipeline. It keeps the mod's block catalog in one
 * place, groups related registrations together for readability, and provides the
 * shared registration helpers that connect block definitions to the NeoForge
 * deferred registry system.
 */
public class ModBlocks {

    // Central deferred register that owns all Shroud block registrations.
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Shroud.MOD_ID);

    // Core terrain and environment blocks.
    public static final DeferredBlock<Block> SCULK_GRASS = registerBlock("sculk_grass",
            () -> new ModGrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)));

    public static final DeferredBlock<Block> SCULK_STONE = registerBlock("sculk_stone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));

    public static final DeferredBlock<Block> SCULK_COBBLESTONE = registerBlock("sculk_cobblestone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)));

    public static final DeferredBlock<Block> SCULK_STONE_BRICKS = registerBlock("sculk_stone_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)));

    public static final DeferredBlock<Block> CRACKED_SCULK_STONE_BRICKS = registerBlock("cracked_sculk_stone_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CRACKED_STONE_BRICKS)));

    public static final DeferredBlock<Block> CHISELED_SCULK_STONE_BRICKS = registerBlock("chiseled_sculk_stone_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_STONE_BRICKS)));

    // Sculk cobblestone structural variants.
    public static final DeferredBlock<StairBlock> SCULK_COBBLESTONE_STAIRS = registerBlock("sculk_cobblestone_stairs",
            () -> new StairBlock(ModBlocks.SCULK_COBBLESTONE.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE_STAIRS)));

    public static final DeferredBlock<SlabBlock> SCULK_COBBLESTONE_SLAB = registerBlock("sculk_cobblestone_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE_SLAB)));

    public static final DeferredBlock<WallBlock> SCULK_COBBLESTONE_WALL = registerBlock("sculk_cobblestone_wall",
            () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE_WALL)));

    // Sculk stone brick structural variants.
    public static final DeferredBlock<StairBlock> SCULK_STONE_BRICK_STAIRS = registerBlock("sculk_stone_brick_stairs",
            () -> new StairBlock(ModBlocks.SCULK_STONE_BRICKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_STAIRS)));

    public static final DeferredBlock<SlabBlock> SCULK_STONE_BRICK_SLAB = registerBlock("sculk_stone_brick_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_SLAB)));

    public static final DeferredBlock<WallBlock> SCULK_STONE_BRICK_WALL = registerBlock("sculk_stone_brick_wall",
            () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_WALL)));

    // Sculk deepslate and derived sets.
    public static final DeferredBlock<Block> SCULK_DEEPSLATE = registerBlock("sculk_deepslate",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)));

    public static final DeferredBlock<Block> COBBLED_SCULK_DEEPSLATE = registerBlock("cobbled_sculk_deepslate",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLED_DEEPSLATE)));

    public static final DeferredBlock<Block> SCULK_DEEPSLATE_BRICKS = registerBlock("sculk_deepslate_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_BRICKS)));

    public static final DeferredBlock<Block> CRACKED_SCULK_DEEPSLATE_BRICKS = registerBlock("cracked_sculk_deepslate_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CRACKED_DEEPSLATE_BRICKS)));

    public static final DeferredBlock<Block> SCULK_DEEPSLATE_TILES = registerBlock("sculk_deepslate_tiles",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_TILES)));

    public static final DeferredBlock<Block> CRACKED_SCULK_DEEPSLATE_TILES = registerBlock("cracked_sculk_deepslate_tiles",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CRACKED_DEEPSLATE_TILES)));

    // Cobbled sculk deepslate structural variants.
    public static final DeferredBlock<StairBlock> COBBLED_SCULK_DEEPSLATE_STAIRS = registerBlock("cobbled_sculk_deepslate_stairs",
            () -> new StairBlock(ModBlocks.COBBLED_SCULK_DEEPSLATE.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLED_DEEPSLATE_STAIRS)));

    public static final DeferredBlock<SlabBlock> COBBLED_SCULK_DEEPSLATE_SLAB = registerBlock("cobbled_sculk_deepslate_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLED_DEEPSLATE_SLAB)));

    public static final DeferredBlock<WallBlock> COBBLED_SCULK_DEEPSLATE_WALL = registerBlock("cobbled_sculk_deepslate_wall",
            () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLED_DEEPSLATE_WALL)));

    // Sculk deepslate brick structural variants.
    public static final DeferredBlock<StairBlock> SCULK_DEEPSLATE_BRICK_STAIRS = registerBlock("sculk_deepslate_brick_stairs",
            () -> new StairBlock(ModBlocks.SCULK_DEEPSLATE_BRICKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_BRICK_STAIRS)));

    public static final DeferredBlock<SlabBlock> SCULK_DEEPSLATE_BRICK_SLAB = registerBlock("sculk_deepslate_brick_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_BRICK_SLAB)));

    public static final DeferredBlock<WallBlock> SCULK_DEEPSLATE_BRICK_WALL = registerBlock("sculk_deepslate_brick_wall",
            () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_BRICK_WALL)));

    // Sculk deepslate tile structural variants.
    public static final DeferredBlock<StairBlock> SCULK_DEEPSLATE_TILE_STAIRS = registerBlock("sculk_deepslate_tile_stairs",
            () -> new StairBlock(ModBlocks.SCULK_DEEPSLATE_TILES.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_TILE_STAIRS)));

    public static final DeferredBlock<SlabBlock> SCULK_DEEPSLATE_TILE_SLAB = registerBlock("sculk_deepslate_tile_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_TILE_SLAB)));

    public static final DeferredBlock<WallBlock> SCULK_DEEPSLATE_TILE_WALL = registerBlock("sculk_deepslate_tile_wall",
            () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_TILE_WALL)));

    public static final DeferredBlock<Block> SCULK_GRAVEL = registerBlock("sculk_gravel",
            () -> new ModSculkGravelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL)));

    public static final DeferredBlock<Block> GLOOMSTONE = registerBlock("gloomstone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GLOWSTONE)));

    // Flora and plant blocks.
    public static final DeferredBlock<Block> GHOST_BLOOM = registerBlock("ghost_bloom",
            () -> new ModGhostBloomBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .lightLevel(state -> 10)));

    public static final DeferredBlock<Block> SCULK_BULB = registerBlock("sculk_bulb",
            () -> new ModSculkBulbBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .lightLevel(state -> 14)));

    public static final DeferredBlock<Block> GLOAMCANE = registerBlock("gloamcane",
            () -> new ModGloamcaneBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SUGAR_CANE)));

    public static final DeferredBlock<ModSculkVinesBlock> SCULK_VINES = registerBlockNoItem("sculk_vines",
            () -> new ModSculkVinesBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CAVE_VINES),
                    () -> ModBlocks.SCULK_VINES_PLANT.get()
            ));

    public static final DeferredBlock<ModSculkVinesPlantBlock> SCULK_VINES_PLANT = registerBlockNoItem("sculk_vines_plant",
            () -> new ModSculkVinesPlantBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CAVE_VINES_PLANT),
                    () -> ModBlocks.SCULK_VINES.get()
            ));

    // Resource and ore blocks.
    public static final DeferredBlock<Block> EVENTIDE_BLOCK = registerBlock("eventide_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)));

    public static final DeferredBlock<Block> EVENTIDE_ORE = registerBlock("eventide_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 4),
                    BlockBehaviour.Properties.of()
                            .strength(3f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    public static final DeferredBlock<Block> EVENTIDE_DEEPSLATE_ORE = registerBlock("eventide_deepslate_ore",
            () -> new DropExperienceBlock(UniformInt.of(3, 6),
                    BlockBehaviour.Properties.of()
                            .strength(4f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)));

    public static final DeferredBlock<Block> SCULK_STONE_EVENTIDE_ORE = registerBlock("sculk_stone_eventide_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 4),
                    BlockBehaviour.Properties.of()
                            .strength(3f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    public static final DeferredBlock<Block> SCULK_DEEPSLATE_EVENTIDE_ORE = registerBlock("sculk_deepslate_eventide_ore",
            () -> new DropExperienceBlock(UniformInt.of(3, 6),
                    BlockBehaviour.Properties.of()
                            .strength(4f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)));

    // Virelith wood set.
    public static final DeferredBlock<Block> VIRELITH_LOG = registerBlock("virelith_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
    public static final DeferredBlock<Block> VIRELITH_WOOD = registerBlock("virelith_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)));
    public static final DeferredBlock<Block> STRIPPED_VIRELITH_LOG = registerBlock("stripped_virelith_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)));
    public static final DeferredBlock<Block> STRIPPED_VIRELITH_WOOD = registerBlock("stripped_virelith_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD)));

    public static final DeferredBlock<Block> VIRELITH_PLANKS = registerBlock("virelith_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)) {
                // Marks virelith planks as flammable.
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                // Sets the flammability value for virelith planks.
                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }

                // Sets the fire spread speed for virelith planks.
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            });

    public static final DeferredBlock<Block> VIRELITH_LEAVES = registerBlock("virelith_leaves",
            () -> new ModLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)) {
                // Marks virelith leaves as flammable.
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                // Sets the flammability value for virelith leaves.
                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 60;
                }

                // Sets the fire spread speed for virelith leaves.
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 30;
                }
            });

    public static final DeferredBlock<Block> VIRELITH_SAPLING = registerBlock(
            "virelith_sapling",
            () -> new ModSaplingBlock(
                    ModTreeGrowers.VIRELITH,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING),
                    ModBlocks.SCULK_GRASS,
                    () -> Blocks.SCULK));

    public static final DeferredBlock<StairBlock> VIRELITH_STAIRS = registerBlock("virelith_stairs",
            () -> new StairBlock(ModBlocks.VIRELITH_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<SlabBlock> VIRELITH_SLAB = registerBlock("virelith_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<PressurePlateBlock> VIRELITH_PRESSURE_PLATE = registerBlock("virelith_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.IRON, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<ButtonBlock> VIRELITH_BUTTON = registerBlock("virelith_button",
            () -> new ButtonBlock(BlockSetType.IRON, 20, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .noCollission()));
    public static final DeferredBlock<FenceBlock> VIRELITH_FENCE = registerBlock("virelith_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<FenceGateBlock> VIRELITH_FENCE_GATE = registerBlock("virelith_fence_gate",
            () -> new FenceGateBlock(WoodType.ACACIA, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<WallBlock> VIRELITH_WALL = registerBlock("virelith_wall",
            () -> new WallBlock(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<DoorBlock> VIRELITH_DOOR = registerBlock("virelith_door",
            () -> new DoorBlock(BlockSetType.ACACIA, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));
    public static final DeferredBlock<TrapDoorBlock> VIRELITH_TRAPDOOR = registerBlock("virelith_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.ACACIA, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    // Umber wood set.
    public static final DeferredBlock<Block> UMBER_LOG = registerBlock("umber_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
    public static final DeferredBlock<Block> UMBER_WOOD = registerBlock("umber_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)));
    public static final DeferredBlock<Block> STRIPPED_UMBER_LOG = registerBlock("stripped_umber_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)));
    public static final DeferredBlock<Block> STRIPPED_UMBER_WOOD = registerBlock("stripped_umber_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD)));

    public static final DeferredBlock<Block> UMBER_PLANKS = registerBlock("umber_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)) {
                // Marks umber planks as flammable.
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                // Sets the flammability value for umber planks.
                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }

                // Sets the fire spread speed for umber planks.
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            });

    public static final DeferredBlock<Block> UMBER_LEAVES = registerBlock("umber_leaves",
            () -> new ModLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)) {
                // Marks umber leaves as flammable.
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                // Sets the flammability value for umber leaves.
                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 60;
                }

                // Sets the fire spread speed for umber leaves.
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 30;
                }
            });

    public static final DeferredBlock<Block> UMBER_SAPLING = registerBlock(
            "umber_sapling",
            () -> new ModSaplingBlock(
                    ModTreeGrowers.UMBER,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING),
                    ModBlocks.SCULK_GRASS,
                    () -> Blocks.SCULK));

    public static final DeferredBlock<StairBlock> UMBER_STAIRS = registerBlock("umber_stairs",
            () -> new StairBlock(ModBlocks.UMBER_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<SlabBlock> UMBER_SLAB = registerBlock("umber_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<PressurePlateBlock> UMBER_PRESSURE_PLATE = registerBlock("umber_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.IRON, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<ButtonBlock> UMBER_BUTTON = registerBlock("umber_button",
            () -> new ButtonBlock(BlockSetType.IRON, 20, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .noCollission()));
    public static final DeferredBlock<FenceBlock> UMBER_FENCE = registerBlock("umber_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<FenceGateBlock> UMBER_FENCE_GATE = registerBlock("umber_fence_gate",
            () -> new FenceGateBlock(WoodType.ACACIA, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<WallBlock> UMBER_WALL = registerBlock("umber_wall",
            () -> new WallBlock(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<DoorBlock> UMBER_DOOR = registerBlock("umber_door",
            () -> new DoorBlock(BlockSetType.ACACIA, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));
    public static final DeferredBlock<TrapDoorBlock> UMBER_TRAPDOOR = registerBlock("umber_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.ACACIA, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    // Limbo and technical blocks.
    public static final DeferredBlock<ModStackingBlock> LIMBO_WALLPAPER_DIAMOND = registerBlock("limbo_wallpaper_diamond",
            () -> new ModStackingBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.DEEPSLATE_BRICKS)
                    .noLootTable()));
    public static final DeferredBlock<ModStackingBlock> LIMBO_WALLPAPER_SEGMENTED = registerBlock("limbo_wallpaper_segmented",
            () -> new ModStackingBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.DEEPSLATE_BRICKS)
                    .noLootTable()));

    public static final DeferredBlock<Block> LIMBO_CARPET = registerBlock("limbo_carpet",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0F)
                    .sound(SoundType.WOOL)
                    .noLootTable()));
    public static final DeferredBlock<StairBlock> LIMBO_CARPET_STAIRS = registerBlock("limbo_carpet_stairs",
            () -> new StairBlock(ModBlocks.LIMBO_CARPET.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0F)
                    .sound(SoundType.WOOL)
                    .noLootTable()));
    public static final DeferredBlock<SlabBlock> LIMBO_CARPET_SLAB = registerBlock("limbo_carpet_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0F)
                    .sound(SoundType.WOOL)
                    .noLootTable()));

    public static final DeferredBlock<Block> LIMBO_CEILING_TILE = registerBlock("limbo_ceiling_tile",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0F)
                    .sound(SoundType.WOOD)
                    .noLootTable()));

    public static final DeferredBlock<Block> LIMBO_FLUORESCENT_LIGHT = registerBlock("limbo_fluorescent_light",
            () -> new ModProjectingLightBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)
                    .noLootTable()));

    public static final DeferredBlock<Block> PROJECTED_LIGHT = registerBlockWithoutItem("projected_light",
            () -> new ModAirLightBlock(15));

    public static final DeferredBlock<Block> SCULK_EMITTER = registerBlock("sculk_emitter",
            () -> new ModParticlePillarBlock(BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .sound(SoundType.SCULK)
                    .lightLevel(state -> 7)));

    public static final DeferredBlock<Block> SCULK_PORTAL = registerBlockWithoutItem("sculk_portal",
            () -> new ShroudPortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL)));

    // Ritual crafting input block that stores up to 64 single-item entries
    public static final DeferredBlock<Block> CORRUPTED_RELIQUARY = registerBlock("corrupted_reliquary",
            () -> new ModCorruptedReliquaryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                    .strength(4.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)
                    .noOcclusion()));

    // Mob-binding pedestal block that captures and holds one mob on top
    public static final DeferredBlock<Block> BINDING_PEDESTAL = registerBlock("binding_pedestal",
            () -> new ModBindingPedestalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                    .strength(4.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)
                    .noOcclusion()));

    // Registration helpers.
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlockNoItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> DeferredBlock<T> registerBlockWithoutItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    // Registers block entries to the mod event bus.
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}