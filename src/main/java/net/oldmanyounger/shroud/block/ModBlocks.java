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

    // Central deferred register that owns all Shroud block registrations
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Shroud.MOD_ID);

    // -------------------------------------------------------------------------
    // Core terrain and environment blocks
    // -------------------------------------------------------------------------

    // Custom spreading sculk surface block
    public static final DeferredBlock<Block> SCULK_GRASS = registerBlock("sculk_grass",
            () -> new ModGrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)));

    // Base sculk stone terrain block
    public static final DeferredBlock<Block> SCULK_STONE = registerBlock("sculk_stone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));

    // Deep sculk stone terrain variant
    public static final DeferredBlock<Block> SCULK_DEEPSLATE = registerBlock("sculk_deepslate",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)));

    // Falling sculk gravel terrain block
    public static final DeferredBlock<Block> SCULK_GRAVEL = registerBlock("sculk_gravel",
            () -> new ModSculkGravelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL)));

    // Glowing stone-like block
    public static final DeferredBlock<Block> GLOOMSTONE = registerBlock("gloomstone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GLOWSTONE)));

    // -------------------------------------------------------------------------
    // Flora and plant blocks
    // -------------------------------------------------------------------------

    // Hanging decorative flower-like plant
    public static final DeferredBlock<Block> GHOST_BLOOM = registerBlock("ghost_bloom",
            () -> new ModGhostBloomBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .lightLevel(state -> 10)));

    // Ground plant restricted to sculk surfaces
    public static final DeferredBlock<Block> SCULK_BULB = registerBlock("sculk_bulb",
            () -> new ModSculkBulbBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .lightLevel(state -> 14)));

    // Harvestable cave-vine-like head block for sculk vines
    public static final DeferredBlock<ModSculkVinesBlock> SCULK_VINES = registerBlockNoItem("sculk_vines",
            () -> new ModSculkVinesBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CAVE_VINES),
                    () -> ModBlocks.SCULK_VINES_PLANT.get()
            ));

    // Matching body block for the sculk vine growth chain
    public static final DeferredBlock<ModSculkVinesPlantBlock> SCULK_VINES_PLANT = registerBlockNoItem("sculk_vines_plant",
            () -> new ModSculkVinesPlantBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CAVE_VINES_PLANT),
                    () -> ModBlocks.SCULK_VINES.get()
            ));

    // -------------------------------------------------------------------------
    // Resource and ore blocks
    // -------------------------------------------------------------------------

    // Solid refined eventide storage block
    public static final DeferredBlock<Block> EVENTIDE_BLOCK = registerBlock("eventide_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST)));

    // Overworld eventide ore
    public static final DeferredBlock<Block> EVENTIDE_ORE = registerBlock("eventide_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 4),
                    BlockBehaviour.Properties.of()
                            .strength(3f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // Deepslate eventide ore
    public static final DeferredBlock<Block> EVENTIDE_DEEPSLATE_ORE = registerBlock("eventide_deepslate_ore",
            () -> new DropExperienceBlock(UniformInt.of(3, 6),
                    BlockBehaviour.Properties.of()
                            .strength(4f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)));

    // Eventide ore embedded in sculk stone
    public static final DeferredBlock<Block> SCULK_STONE_EVENTIDE_ORE = registerBlock("sculk_stone_eventide_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 4),
                    BlockBehaviour.Properties.of()
                            .strength(3f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    // Eventide ore embedded in sculk deepslate
    public static final DeferredBlock<Block> SCULK_DEEPSLATE_EVENTIDE_ORE = registerBlock("sculk_deepslate_eventide_ore",
            () -> new DropExperienceBlock(UniformInt.of(3, 6),
                    BlockBehaviour.Properties.of()
                            .strength(4f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)));

    // -------------------------------------------------------------------------
    // Sculk wood set
    // -------------------------------------------------------------------------

    // Sculk log and wood variants
    public static final DeferredBlock<Block> SCULK_LOG = registerBlock("sculk_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
    public static final DeferredBlock<Block> SCULK_WOOD = registerBlock("sculk_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)));
    public static final DeferredBlock<Block> STRIPPED_SCULK_LOG = registerBlock("stripped_sculk_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)));
    public static final DeferredBlock<Block> STRIPPED_SCULK_WOOD = registerBlock("stripped_sculk_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD)));

    // Sculk plank block
    public static final DeferredBlock<Block> SCULK_PLANKS = registerBlock("sculk_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)) {
                // Marks sculk planks as flammable
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                // Sets the flammability value for sculk planks
                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }

                // Sets the fire spread speed for sculk planks
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            });

    // Sculk leaves block
    public static final DeferredBlock<Block> SCULK_LEAVES = registerBlock("sculk_leaves",
            () -> new ModLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)) {
                // Marks sculk leaves as flammable
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                // Sets the flammability value for sculk leaves
                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 60;
                }

                // Sets the fire spread speed for sculk leaves
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 30;
                }
            });

    // Sculk sapling tied to sculk terrain
    public static final DeferredBlock<Block> SCULK_SAPLING = registerBlock(
            "sculk_sapling",
            () -> new ModSaplingBlock(
                    ModTreeGrowers.SCULK,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING),
                    ModBlocks.SCULK_GRASS,
                    () -> Blocks.SCULK));

    // Sculk building set variants
    public static final DeferredBlock<StairBlock> SCULK_STAIRS = registerBlock("sculk_stairs",
            () -> new StairBlock(ModBlocks.SCULK_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<SlabBlock> SCULK_SLAB = registerBlock("sculk_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<PressurePlateBlock> SCULK_PRESSURE_PLATE = registerBlock("sculk_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.IRON, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<ButtonBlock> SCULK_BUTTON = registerBlock("sculk_button",
            () -> new ButtonBlock(BlockSetType.IRON, 20, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .noCollission()));
    public static final DeferredBlock<FenceBlock> SCULK_FENCE = registerBlock("sculk_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<FenceGateBlock> SCULK_FENCE_GATE = registerBlock("sculk_fence_gate",
            () -> new FenceGateBlock(WoodType.ACACIA, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<WallBlock> SCULK_WALL = registerBlock("sculk_wall",
            () -> new WallBlock(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<DoorBlock> SCULK_DOOR = registerBlock("sculk_door",
            () -> new DoorBlock(BlockSetType.ACACIA, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));
    public static final DeferredBlock<TrapDoorBlock> SCULK_TRAPDOOR = registerBlock("sculk_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.ACACIA, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    // -------------------------------------------------------------------------
    // Umber wood set
    // -------------------------------------------------------------------------

    // Umber log and wood variants
    public static final DeferredBlock<Block> UMBER_LOG = registerBlock("umber_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
    public static final DeferredBlock<Block> UMBER_WOOD = registerBlock("umber_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)));
    public static final DeferredBlock<Block> STRIPPED_UMBER_LOG = registerBlock("stripped_umber_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)));
    public static final DeferredBlock<Block> STRIPPED_UMBER_WOOD = registerBlock("stripped_umber_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD)));

    // Umber plank block
    public static final DeferredBlock<Block> UMBER_PLANKS = registerBlock("umber_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)) {
                // Marks umber planks as flammable
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                // Sets the flammability value for umber planks
                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }

                // Sets the fire spread speed for umber planks
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            });

    // Umber leaves block
    public static final DeferredBlock<Block> UMBER_LEAVES = registerBlock("umber_leaves",
            () -> new ModLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)) {
                // Marks umber leaves as flammable
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                // Sets the flammability value for umber leaves
                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 60;
                }

                // Sets the fire spread speed for umber leaves
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 30;
                }
            });

    // Umber sapling registration
    public static final DeferredBlock<Block> UMBER_SAPLING = registerBlock(
            "umber_sapling",
            () -> new ModSaplingBlock(
                    ModTreeGrowers.SCULK,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING),
                    ModBlocks.SCULK_GRASS,
                    () -> Blocks.SCULK));

    // Umber building set variants
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

    // -------------------------------------------------------------------------
    // Limbo and technical blocks
    // -------------------------------------------------------------------------

    // Decorative stacked wallpaper variants
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

    // Limbo carpet and trim variants
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

    // Limbo structural ceiling block
    public static final DeferredBlock<Block> LIMBO_CEILING_TILE = registerBlock("limbo_ceiling_tile",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0F)
                    .sound(SoundType.WOOD)
                    .noLootTable()));

    // Visible light source block that owns the projecting-light block entity
    public static final DeferredBlock<Block> LIMBO_FLUORESCENT_LIGHT = registerBlock("limbo_fluorescent_light",
            () -> new ModProjectingLightBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)
                    .noLootTable()));

    // Invisible helper light block used only for projected illumination
    public static final DeferredBlock<Block> PROJECTED_LIGHT = registerBlockWithoutItem("projected_light",
            () -> new ModAirLightBlock(15));

    // Sculk emitter block with directional particle behavior
    public static final DeferredBlock<Block> SCULK_EMITTER = registerBlock("sculk_emitter",
            () -> new ModParticlePillarBlock(BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .sound(SoundType.SCULK)
                    .lightLevel(state -> 7)));

    // Nether-portal-like custom portal block used by Shroud
    public static final DeferredBlock<Block> SCULK_PORTAL = registerBlockWithoutItem("sculk_portal",
            () -> new ShroudPortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL)));

    // -------------------------------------------------------------------------
    // Registration helpers
    // -------------------------------------------------------------------------

    // Registers a block and automatically creates its inventory BlockItem
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    // Registers a block without creating a matching BlockItem
    private static <T extends Block> DeferredBlock<T> registerBlockNoItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    // Registers the BlockItem for a previously registered block
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // Registers a block without an item using the alternate helper name already present in this class
    private static <T extends Block> DeferredBlock<T> registerBlockWithoutItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    // Attaches the block registry to the mod event bus
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}