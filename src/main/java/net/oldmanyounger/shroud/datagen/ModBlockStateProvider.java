package net.oldmanyounger.shroud.datagen;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.block.custom.ModStackingBlock;

/**
 * Generates blockstate definitions and block models for Shroud blocks.
 *
 * <p>This provider is responsible for describing how Shroud blocks should be
 * rendered in the world and, where needed, how their corresponding block items
 * should be modeled. It covers simple cube blocks, plants, logs, multi-part
 * grass overlays, directional emitters, vine variants, and stack-aware wallpaper
 * blocks.
 *
 * <p>In the broader context of the project, this class is one of the central
 * asset-generation providers that converts Java-side block registrations into
 * the blockstate and model JSON files used by the client renderer.
 */
public class ModBlockStateProvider extends BlockStateProvider {

    // Creates the blockstate provider for the Shroud namespace
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Shroud.MOD_ID, exFileHelper);
    }

    // Registers blockstate and model output for all supported Shroud blocks
    @Override
    protected void registerStatesAndModels() {
        // Terrain and plant blocks
        grassBlockWithItem(ModBlocks.SCULK_GRASS);
        blockWithItem(ModBlocks.SCULK_GRAVEL);
        blockWithItem(ModBlocks.GLOOMSTONE);

        blockWithItem(ModBlocks.SCULK_STONE);
        blockWithItem(ModBlocks.SCULK_COBBLESTONE);
        blockWithItem(ModBlocks.SCULK_STONE_BRICKS);
        blockWithItem(ModBlocks.CRACKED_SCULK_STONE_BRICKS);
        blockWithItem(ModBlocks.CHISELED_SCULK_STONE_BRICKS);

        blockWithItem(ModBlocks.SCULK_DEEPSLATE);
        blockWithItem(ModBlocks.COBBLED_SCULK_DEEPSLATE);
        blockWithItem(ModBlocks.SCULK_DEEPSLATE_BRICKS);
        blockWithItem(ModBlocks.CRACKED_SCULK_DEEPSLATE_BRICKS);
        blockWithItem(ModBlocks.SCULK_DEEPSLATE_TILES);
        blockWithItem(ModBlocks.CRACKED_SCULK_DEEPSLATE_TILES);

        // Sculk stone/deepslate structural variants
        stairsBlock(ModBlocks.SCULK_COBBLESTONE_STAIRS.get(), blockTexture(ModBlocks.SCULK_COBBLESTONE.get()));
        slabBlock(ModBlocks.SCULK_COBBLESTONE_SLAB.get(), blockTexture(ModBlocks.SCULK_COBBLESTONE.get()), blockTexture(ModBlocks.SCULK_COBBLESTONE.get()));
        wallBlock(ModBlocks.SCULK_COBBLESTONE_WALL.get(), blockTexture(ModBlocks.SCULK_COBBLESTONE.get()));

        stairsBlock(ModBlocks.SCULK_STONE_BRICK_STAIRS.get(), blockTexture(ModBlocks.SCULK_STONE_BRICKS.get()));
        slabBlock(ModBlocks.SCULK_STONE_BRICK_SLAB.get(), blockTexture(ModBlocks.SCULK_STONE_BRICKS.get()), blockTexture(ModBlocks.SCULK_STONE_BRICKS.get()));
        wallBlock(ModBlocks.SCULK_STONE_BRICK_WALL.get(), blockTexture(ModBlocks.SCULK_STONE_BRICKS.get()));

        stairsBlock(ModBlocks.COBBLED_SCULK_DEEPSLATE_STAIRS.get(), blockTexture(ModBlocks.COBBLED_SCULK_DEEPSLATE.get()));
        slabBlock(ModBlocks.COBBLED_SCULK_DEEPSLATE_SLAB.get(), blockTexture(ModBlocks.COBBLED_SCULK_DEEPSLATE.get()), blockTexture(ModBlocks.COBBLED_SCULK_DEEPSLATE.get()));
        wallBlock(ModBlocks.COBBLED_SCULK_DEEPSLATE_WALL.get(), blockTexture(ModBlocks.COBBLED_SCULK_DEEPSLATE.get()));

        stairsBlock(ModBlocks.SCULK_DEEPSLATE_BRICK_STAIRS.get(), blockTexture(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()));
        slabBlock(ModBlocks.SCULK_DEEPSLATE_BRICK_SLAB.get(), blockTexture(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()), blockTexture(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()));
        wallBlock(ModBlocks.SCULK_DEEPSLATE_BRICK_WALL.get(), blockTexture(ModBlocks.SCULK_DEEPSLATE_BRICKS.get()));

        stairsBlock(ModBlocks.SCULK_DEEPSLATE_TILE_STAIRS.get(), blockTexture(ModBlocks.SCULK_DEEPSLATE_TILES.get()));
        slabBlock(ModBlocks.SCULK_DEEPSLATE_TILE_SLAB.get(), blockTexture(ModBlocks.SCULK_DEEPSLATE_TILES.get()), blockTexture(ModBlocks.SCULK_DEEPSLATE_TILES.get()));
        wallBlock(ModBlocks.SCULK_DEEPSLATE_TILE_WALL.get(), blockTexture(ModBlocks.SCULK_DEEPSLATE_TILES.get()));

        blockItem(ModBlocks.SCULK_COBBLESTONE_STAIRS);
        blockItem(ModBlocks.SCULK_COBBLESTONE_SLAB);
        blockItem(ModBlocks.SCULK_STONE_BRICK_STAIRS);
        blockItem(ModBlocks.SCULK_STONE_BRICK_SLAB);
        blockItem(ModBlocks.COBBLED_SCULK_DEEPSLATE_STAIRS);
        blockItem(ModBlocks.COBBLED_SCULK_DEEPSLATE_SLAB);
        blockItem(ModBlocks.SCULK_DEEPSLATE_BRICK_STAIRS);
        blockItem(ModBlocks.SCULK_DEEPSLATE_BRICK_SLAB);
        blockItem(ModBlocks.SCULK_DEEPSLATE_TILE_STAIRS);
        blockItem(ModBlocks.SCULK_DEEPSLATE_TILE_SLAB);

        saplingBlock(ModBlocks.SCULK_BULB);
        saplingBlock(ModBlocks.GHOST_BLOOM);
        sculkVinesBlock(ModBlocks.SCULK_VINES, ModBlocks.SCULK_VINES_PLANT);

        // Technical and resource blocks
        emitterBlock(ModBlocks.SCULK_EMITTER.get());
        blockItem(ModBlocks.SCULK_EMITTER);
        blockWithItem(ModBlocks.EVENTIDE_BLOCK);
        blockWithItem(ModBlocks.EVENTIDE_ORE);
        blockWithItem(ModBlocks.EVENTIDE_DEEPSLATE_ORE);
        blockWithItem(ModBlocks.SCULK_STONE_EVENTIDE_ORE);
        blockWithItem(ModBlocks.SCULK_DEEPSLATE_EVENTIDE_ORE);

        // Virelith wood set
        logBlock((RotatedPillarBlock) ModBlocks.VIRELITH_LOG.get());
        axisBlock((RotatedPillarBlock) ModBlocks.VIRELITH_WOOD.get(),
                blockTexture(ModBlocks.VIRELITH_LOG.get()),
                blockTexture(ModBlocks.VIRELITH_LOG.get()));
        logBlock((RotatedPillarBlock) ModBlocks.STRIPPED_VIRELITH_LOG.get());
        axisBlock((RotatedPillarBlock) ModBlocks.STRIPPED_VIRELITH_WOOD.get(),
                blockTexture(ModBlocks.STRIPPED_VIRELITH_LOG.get()),
                blockTexture(ModBlocks.STRIPPED_VIRELITH_LOG.get()));

        blockItem(ModBlocks.VIRELITH_LOG);
        blockItem(ModBlocks.VIRELITH_WOOD);
        blockItem(ModBlocks.STRIPPED_VIRELITH_LOG);
        blockItem(ModBlocks.STRIPPED_VIRELITH_WOOD);

        blockWithItem(ModBlocks.VIRELITH_PLANKS);
        leavesBlock(ModBlocks.VIRELITH_LEAVES);
        saplingBlock(ModBlocks.VIRELITH_SAPLING);

        stairsBlock(ModBlocks.VIRELITH_STAIRS.get(), blockTexture(ModBlocks.VIRELITH_PLANKS.get()));
        slabBlock(ModBlocks.VIRELITH_SLAB.get(), blockTexture(ModBlocks.VIRELITH_PLANKS.get()), blockTexture(ModBlocks.VIRELITH_PLANKS.get()));
        buttonBlock(ModBlocks.VIRELITH_BUTTON.get(), blockTexture(ModBlocks.VIRELITH_PLANKS.get()));
        pressurePlateBlock(ModBlocks.VIRELITH_PRESSURE_PLATE.get(), blockTexture(ModBlocks.VIRELITH_PLANKS.get()));
        fenceBlock(ModBlocks.VIRELITH_FENCE.get(), blockTexture(ModBlocks.VIRELITH_PLANKS.get()));
        fenceGateBlock(ModBlocks.VIRELITH_FENCE_GATE.get(), blockTexture(ModBlocks.VIRELITH_PLANKS.get()));
        wallBlock(ModBlocks.VIRELITH_WALL.get(), blockTexture(ModBlocks.VIRELITH_PLANKS.get()));
        doorBlockWithRenderType(ModBlocks.VIRELITH_DOOR.get(), modLoc("block/virelith_door_bottom"), modLoc("block/virelith_door_top"), "cutout");
        trapdoorBlockWithRenderType(ModBlocks.VIRELITH_TRAPDOOR.get(), modLoc("block/virelith_trapdoor"), true, "cutout");

        blockItem(ModBlocks.VIRELITH_STAIRS);
        blockItem(ModBlocks.VIRELITH_SLAB);
        blockItem(ModBlocks.VIRELITH_PRESSURE_PLATE);
        blockItem(ModBlocks.VIRELITH_FENCE_GATE);
        blockItem(ModBlocks.VIRELITH_TRAPDOOR, "_bottom");

        // Umber wood set
        logBlock((RotatedPillarBlock) ModBlocks.UMBER_LOG.get());
        axisBlock((RotatedPillarBlock) ModBlocks.UMBER_WOOD.get(),
                blockTexture(ModBlocks.UMBER_LOG.get()),
                blockTexture(ModBlocks.UMBER_LOG.get()));
        logBlock((RotatedPillarBlock) ModBlocks.STRIPPED_UMBER_LOG.get());
        axisBlock((RotatedPillarBlock) ModBlocks.STRIPPED_UMBER_WOOD.get(),
                blockTexture(ModBlocks.STRIPPED_UMBER_LOG.get()),
                blockTexture(ModBlocks.STRIPPED_UMBER_LOG.get()));

        blockItem(ModBlocks.UMBER_LOG);
        blockItem(ModBlocks.UMBER_WOOD);
        blockItem(ModBlocks.STRIPPED_UMBER_LOG);
        blockItem(ModBlocks.STRIPPED_UMBER_WOOD);

        blockWithItem(ModBlocks.UMBER_PLANKS);
        leavesBlock(ModBlocks.UMBER_LEAVES);
        saplingBlock(ModBlocks.UMBER_SAPLING);

        stairsBlock(ModBlocks.UMBER_STAIRS.get(), blockTexture(ModBlocks.UMBER_PLANKS.get()));
        slabBlock(ModBlocks.UMBER_SLAB.get(), blockTexture(ModBlocks.UMBER_PLANKS.get()), blockTexture(ModBlocks.UMBER_PLANKS.get()));
        buttonBlock(ModBlocks.UMBER_BUTTON.get(), blockTexture(ModBlocks.UMBER_PLANKS.get()));
        pressurePlateBlock(ModBlocks.UMBER_PRESSURE_PLATE.get(), blockTexture(ModBlocks.UMBER_PLANKS.get()));
        fenceBlock(ModBlocks.UMBER_FENCE.get(), blockTexture(ModBlocks.UMBER_PLANKS.get()));
        fenceGateBlock(ModBlocks.UMBER_FENCE_GATE.get(), blockTexture(ModBlocks.UMBER_PLANKS.get()));
        wallBlock(ModBlocks.UMBER_WALL.get(), blockTexture(ModBlocks.UMBER_PLANKS.get()));
        doorBlockWithRenderType(ModBlocks.UMBER_DOOR.get(), modLoc("block/umber_door_bottom"), modLoc("block/umber_door_top"), "cutout");
        trapdoorBlockWithRenderType(ModBlocks.UMBER_TRAPDOOR.get(), modLoc("block/umber_trapdoor"), true, "cutout");

        blockItem(ModBlocks.UMBER_STAIRS);
        blockItem(ModBlocks.UMBER_SLAB);
        blockItem(ModBlocks.UMBER_PRESSURE_PLATE);
        blockItem(ModBlocks.UMBER_FENCE_GATE);
        blockItem(ModBlocks.UMBER_TRAPDOOR, "_bottom");

        // Reliquary and pedestal blocks
        simpleBlockWithItem(ModBlocks.CORRUPTED_RELIQUARY.get(), cubeAll(ModBlocks.CORRUPTED_RELIQUARY.get()));

        // Limbo blocks
        stackingWallpaperBlock(
                ModBlocks.LIMBO_WALLPAPER_DIAMOND.get(),
                modLoc("block/limbo_wallpaper_diamond"),
                modLoc("block/limbo_wallpaper_diamond_stacked"),
                modLoc("block/limbo_carpet"),
                modLoc("block/limbo_ceiling_tile")
        );

        stackingWallpaperBlock(
                ModBlocks.LIMBO_WALLPAPER_SEGMENTED.get(),
                modLoc("block/limbo_wallpaper_segmented"),
                modLoc("block/limbo_wallpaper_segmented_stacked"),
                modLoc("block/limbo_carpet"),
                modLoc("block/limbo_ceiling_tile")
        );

        blockItem(ModBlocks.LIMBO_WALLPAPER_DIAMOND);
        blockItem(ModBlocks.LIMBO_WALLPAPER_SEGMENTED);
        simpleBlockWithItem(ModBlocks.LIMBO_CARPET.get(), cubeAll(ModBlocks.LIMBO_CARPET.get()));
        stairsBlock(ModBlocks.LIMBO_CARPET_STAIRS.get(), blockTexture(ModBlocks.LIMBO_CARPET.get()));
        slabBlock(ModBlocks.LIMBO_CARPET_SLAB.get(), blockTexture(ModBlocks.LIMBO_CARPET.get()), blockTexture(ModBlocks.LIMBO_CARPET.get()));
        blockItem(ModBlocks.LIMBO_CARPET_STAIRS);
        blockItem(ModBlocks.LIMBO_CARPET_SLAB);
        simpleBlockWithItem(ModBlocks.LIMBO_CEILING_TILE.get(), cubeAll(ModBlocks.LIMBO_CEILING_TILE.get()));
        simpleBlockWithItem(ModBlocks.LIMBO_FLUORESCENT_LIGHT.get(), cubeAll(ModBlocks.LIMBO_FLUORESCENT_LIGHT.get()));
    }

    // Creates a cutout cross model for plant-like blocks
    private void saplingBlock(DeferredBlock<Block> blockRegistryObject) {
        simpleBlock(
                blockRegistryObject.get(),
                models().cross(
                        BuiltInRegistries.BLOCK.getKey(blockRegistryObject.get()).getPath(),
                        blockTexture(blockRegistryObject.get())
                ).renderType("cutout")
        );
    }

    // Creates a cutout leaves model and matching item model
    private void leavesBlock(DeferredBlock<Block> blockRegistryObject) {
        simpleBlockWithItem(
                blockRegistryObject.get(),
                models().singleTexture(
                        BuiltInRegistries.BLOCK.getKey(blockRegistryObject.get()).getPath(),
                        ResourceLocation.parse("minecraft:block/leaves"),
                        "all",
                        blockTexture(blockRegistryObject.get())
                ).renderType("cutout")
        );
    }

    // Creates lit and unlit variants for the custom sculk vine head and plant blocks
    private void sculkVinesBlock(DeferredBlock<? extends Block> head, DeferredBlock<? extends Block> plant) {
        String headPath = head.getId().getPath();
        String plantPath = plant.getId().getPath();

        ModelFile headUnlit = models().cross(headPath, modLoc("block/" + headPath)).renderType("cutout");
        ModelFile headLit = models().cross(headPath + "_lit", modLoc("block/" + headPath + "_lit")).renderType("cutout");

        ModelFile plantUnlit = models().cross(plantPath, modLoc("block/" + plantPath)).renderType("cutout");
        ModelFile plantLit = models().cross(plantPath + "_lit", modLoc("block/" + plantPath + "_lit")).renderType("cutout");

        getVariantBuilder(head.get()).forAllStates(state -> {
            boolean berries = state.getValue(net.minecraft.world.level.block.CaveVines.BERRIES);
            return ConfiguredModel.builder().modelFile(berries ? headLit : headUnlit).build();
        });

        getVariantBuilder(plant.get()).forAllStates(state -> {
            boolean berries = state.getValue(net.minecraft.world.level.block.CaveVines.BERRIES);
            return ConfiguredModel.builder().modelFile(berries ? plantLit : plantUnlit).build();
        });
    }

    // Builds a multipart grass block model with a tinted overlay, similar to vanilla grass
    private void grassBlockWithItem(DeferredBlock<Block> blockRegistryObject) {
        String path = blockRegistryObject.getId().getPath();

        var baseModel = models().withExistingParent(path, mcLoc("block/block"))
                .texture("particle", mcLoc("block/sculk"))
                .texture("bottom", mcLoc("block/sculk"))
                .texture("top", modLoc("block/" + path + "_top"))
                .texture("side", modLoc("block/" + path + "_side"));

        baseModel.element()
                .from(0, 0, 0).to(16, 16, 16)
                .face(net.minecraft.core.Direction.DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(net.minecraft.core.Direction.DOWN).end()
                .face(net.minecraft.core.Direction.UP).uvs(0, 0, 16, 16).texture("#top").cullface(net.minecraft.core.Direction.UP).tintindex(0).end()
                .face(net.minecraft.core.Direction.NORTH).uvs(0, 0, 16, 16).texture("#side").cullface(net.minecraft.core.Direction.NORTH).end()
                .face(net.minecraft.core.Direction.SOUTH).uvs(0, 0, 16, 16).texture("#side").cullface(net.minecraft.core.Direction.SOUTH).end()
                .face(net.minecraft.core.Direction.WEST).uvs(0, 0, 16, 16).texture("#side").cullface(net.minecraft.core.Direction.WEST).end()
                .face(net.minecraft.core.Direction.EAST).uvs(0, 0, 16, 16).texture("#side").cullface(net.minecraft.core.Direction.EAST).end()
                .end();

        var overlayModel = models().withExistingParent(path + "_overlay", mcLoc("block/block"))
                .texture("particle", modLoc("block/" + path + "_side_overlay"))
                .texture("overlay", modLoc("block/" + path + "_side_overlay"));

        overlayModel.element()
                .from(0, 0, 0).to(16, 16, 16)
                .face(net.minecraft.core.Direction.NORTH).uvs(0, 0, 16, 16).texture("#overlay").cullface(net.minecraft.core.Direction.NORTH).tintindex(0).end()
                .face(net.minecraft.core.Direction.SOUTH).uvs(0, 0, 16, 16).texture("#overlay").cullface(net.minecraft.core.Direction.SOUTH).tintindex(0).end()
                .face(net.minecraft.core.Direction.WEST).uvs(0, 0, 16, 16).texture("#overlay").cullface(net.minecraft.core.Direction.WEST).tintindex(0).end()
                .face(net.minecraft.core.Direction.EAST).uvs(0, 0, 16, 16).texture("#overlay").cullface(net.minecraft.core.Direction.EAST).tintindex(0).end()
                .end();

        getMultipartBuilder(blockRegistryObject.get())
                .part().modelFile(baseModel).addModel().end()
                .part().modelFile(overlayModel).addModel().end();

        simpleBlockItem(blockRegistryObject.get(), baseModel);
    }

    // Creates the directional emitter block model and rotates it based on the block's facing state
    private void emitterBlock(Block block) {
        ModelFile model = models().cubeBottomTop(
                "sculk_emitter",
                modLoc("block/sculk_emitter"),      // side
                modLoc("block/sculk_emitter"),      // bottom
                modLoc("block/sculk_emitter_top")   // top
        );

        getVariantBuilder(block).forAllStates(state -> {
            Direction facing = state.getValue(DirectionalBlock.FACING);

            int xRot = switch (facing) {
                case DOWN -> 180;
                case UP -> 0;
                case NORTH -> 90;
                case SOUTH -> 270;
                case WEST, EAST -> 90;
            };

            int yRot = switch (facing) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };

            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .build();
        });
    }

    // Creates the normal and stacked variants for a wallpaper block that changes appearance when vertically stacked
    private void stackingWallpaperBlock(ModStackingBlock block,
                                        ResourceLocation normalSide,
                                        ResourceLocation stackedSide,
                                        ResourceLocation topTexture,
                                        ResourceLocation bottomTexture) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();

        ModelFile normalModel = models().cubeBottomTop(name, normalSide, bottomTexture, topTexture);
        ModelFile stackedModel = models().cubeBottomTop(name + "_stacked", stackedSide, bottomTexture, topTexture);

        getVariantBuilder(block).forAllStates(state -> {
            boolean stacked = state.getValue(ModStackingBlock.STACKED);
            return ConfiguredModel.builder()
                    .modelFile(stacked ? stackedModel : normalModel)
                    .build();
        });
    }

    // Registers a block with a standard cube-all block model and matching block item model
    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    // Registers a block item model that points directly at the generated block model
    private void blockItem(DeferredBlock<?> deferredBlock) {
        simpleBlockItem(deferredBlock.get(),
                new ModelFile.UncheckedModelFile("shroud:block/" + deferredBlock.getId().getPath()));
    }

    // Registers a block item model that points to a suffixed block model variant
    private void blockItem(DeferredBlock<?> deferredBlock, String appendix) {
        simpleBlockItem(deferredBlock.get(),
                new ModelFile.UncheckedModelFile("shroud:block/" + deferredBlock.getId().getPath() + appendix));
    }
}