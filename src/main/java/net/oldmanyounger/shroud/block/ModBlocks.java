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
import net.oldmanyounger.shroud.worldgen.tree.ModTreeGrowers;
import net.oldmanyounger.shroud.portal.ShroudPortalBlock;

import java.util.function.Supplier;

/** Declares and registers all Shroud blocks and associated BlockItems */
public class ModBlocks {

    // Central deferred register for all Shroud block instances
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Shroud.MOD_ID);

    public static final DeferredBlock<Block> SCULK_GRASS = registerBlock("sculk_grass",
            () -> new ModGrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)));
    public static final DeferredBlock<Block> SCULK_BULB = registerBlock("sculk_bulb",
            () -> new ModSculkBulbBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .lightLevel(state -> 6)));

    public static final DeferredBlock<Block> EVENTIDE_BLOCK = registerBlock("eventide_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));

    public static final DeferredBlock<Block> EVENTIDE_ORE = registerBlock("eventide_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 4),
                    BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<Block> EVENTIDE_DEEPSLATE_ORE = registerBlock("eventide_deepslate_ore",
            () -> new DropExperienceBlock(UniformInt.of(3, 6),
                    BlockBehaviour.Properties.of().strength(4f).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));

    // Sculk log, wood, stripped variants, and planks with flammability behavior
    public static final DeferredBlock<Block> SCULK_LOG = registerBlock("sculk_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
    public static final DeferredBlock<Block> SCULK_WOOD = registerBlock("sculk_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)));
    public static final DeferredBlock<Block> STRIPPED_SCULK_LOG = registerBlock("stripped_sculk_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)));
    public static final DeferredBlock<Block> STRIPPED_SCULK_WOOD = registerBlock("stripped_sculk_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD)));

    // Sculk planks with custom flammability and fire spread values
    public static final DeferredBlock<Block> SCULK_PLANKS = registerBlock("sculk_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)) {
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }

                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            });

    // Sculk leaves with overridden flammability profile
    public static final DeferredBlock<Block> SCULK_LEAVES = registerBlock("sculk_leaves",
            () -> new ModLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)) {
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 60;
                }

                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 30;
                }
            });

    // Sculk sapling constrained to survive on Sculk blocks
    public static final DeferredBlock<Block> SCULK_SAPLING = registerBlock(
            "sculk_sapling",
            () -> new ModSaplingBlock(
                    ModTreeGrowers.SCULK,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING),
                    ModBlocks.SCULK_GRASS,
                    () -> Blocks.SCULK));


    // Sculk stairs and slab variants based on Sculk planks
    public static final DeferredBlock<StairBlock> SCULK_STAIRS = registerBlock("sculk_stairs",
            () -> new StairBlock(ModBlocks.SCULK_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<SlabBlock> SCULK_SLAB = registerBlock("sculk_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));

    // Sculk pressure plate and button using iron block set type
    public static final DeferredBlock<PressurePlateBlock> SCULK_PRESSURE_PLATE = registerBlock("sculk_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.IRON, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<ButtonBlock> SCULK_BUTTON = registerBlock("sculk_button",
            () -> new ButtonBlock(BlockSetType.IRON, 20, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .noCollission()));

    // Sculk fence, fence gate, and wall for structural variants
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

    // Sculk door and trapdoor using Acacia block set type with no occlusion
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

    // Umber log, wood, stripped variants, and planks with flammability behavior
    public static final DeferredBlock<Block> UMBER_LOG = registerBlock("umber_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
    public static final DeferredBlock<Block> UMBER_WOOD = registerBlock("umber_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)));
    public static final DeferredBlock<Block> STRIPPED_UMBER_LOG = registerBlock("stripped_umber_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)));
    public static final DeferredBlock<Block> STRIPPED_UMBER_WOOD = registerBlock("stripped_umber_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD)));

    // Umber planks with custom flammability and fire spread values
    public static final DeferredBlock<Block> UMBER_PLANKS = registerBlock("umber_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)) {
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }

                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            });

    // Umber leaves with overridden flammability profile
    public static final DeferredBlock<Block> UMBER_LEAVES = registerBlock("umber_leaves",
            () -> new ModLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)) {
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 60;
                }

                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 30;
                }
            });

    // Umber sapling constrained to survive on Umber blocks
    public static final DeferredBlock<Block> UMBER_SAPLING = registerBlock(
            "umber_sapling",
            () -> new ModSaplingBlock(
                    ModTreeGrowers.SCULK,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING),
                    ModBlocks.SCULK_GRASS,
                    () -> Blocks.SCULK));

    // Umber stairs and slab variants based on Umber planks
    public static final DeferredBlock<StairBlock> UMBER_STAIRS = registerBlock("umber_stairs",
            () -> new StairBlock(ModBlocks.UMBER_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<SlabBlock> UMBER_SLAB = registerBlock("umber_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));

    // Umber pressure plate and button using iron block set type
    public static final DeferredBlock<PressurePlateBlock> UMBER_PRESSURE_PLATE = registerBlock("umber_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.IRON, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<ButtonBlock> UMBER_BUTTON = registerBlock("umber_button",
            () -> new ButtonBlock(BlockSetType.IRON, 20, BlockBehaviour.Properties.of()
                    .strength(2f)
                    .requiresCorrectToolForDrops()
                    .noCollission()));

    // Umber fence, fence gate, and wall for structural variants
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

    // Umber door and trapdoor using Acacia block set type with no occlusion
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

    // Limbo decorative blocks
    public static final DeferredBlock<Block> LIMBO_WALLPAPER_DIAMOND = registerBlock("limbo_wallpaper_diamond",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.DEEPSLATE_BRICKS)
                    .noLootTable()));
    public static final DeferredBlock<Block> LIMBO_WALLPAPER_SEGMENTED = registerBlock("limbo_wallpaper_segmented",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.DEEPSLATE_BRICKS)
                    .noLootTable()));
    public static final DeferredBlock<Block> LIMBO_CARPET = registerBlock("limbo_carpet",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0F)
                    .sound(SoundType.WOOL)
                    .noLootTable()));
    public static final DeferredBlock<Block> LIMBO_CEILING_TILE = registerBlock("limbo_ceiling_tile",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0F)
                    .sound(SoundType.WOOD)
                    .noLootTable()));

    // Visible source block (has texture + item)
    public static final DeferredBlock<Block> LIMBO_FLUORESCENT_LIGHT = registerBlock("limbo_fluorescent_light",
            () -> new ModProjectingLightBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)
                    .noLootTable()));
    // Invisible helper block used for projected lighting (air-like, no item)
    public static final DeferredBlock<Block> PROJECTED_LIGHT = registerBlockWithoutItem("projected_light",
            () -> new ModAirLightBlock(15));

    // Sculk portal block using Nether portal-like behaviour and rendering
    public static final DeferredBlock<Block> SCULK_PORTAL = registerBlockWithoutItem("sculk_portal",
            () -> new ShroudPortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL)));


    // Registers a block under the given name and automatically creates its BlockItem
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    // Registers the BlockItem for a given block in the shared item registry
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    /** Registers a block without creating an associated BlockItem */
    private static <T extends Block> DeferredBlock<T> registerBlockWithoutItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }


    // Attaches the block registry to the mod event bus so all blocks are created at runtime
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
