package net.oldmanyounger.shroud.datagen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.oldmanyounger.shroud.block.custom.ModStackingBlock;

/** Generates all blockstates and block models for Shroud blocks */
public class ModBlockStateProvider extends BlockStateProvider {

    /** Constructs the blockstate provider using the mod ID and file helper */
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Shroud.MOD_ID, exFileHelper);
    }

    /** Registers blockstates and models for every Shroud block */
    @Override
    protected void registerStatesAndModels() {

        // Sculk grass and plants
        grassBlockWithItem(ModBlocks.SCULK_GRASS);
        saplingBlock(ModBlocks.SCULK_BULB);

        logBlock((RotatedPillarBlock) ModBlocks.SCULK_EMITTER.get());
        blockItem(ModBlocks.SCULK_EMITTER);

        // Eventide storage block (simple cube + matching item model)
        blockWithItem(ModBlocks.EVENTIDE_BLOCK);

        // Eventide ore blockstate/model (simple cube + matching item model)
        blockWithItem(ModBlocks.EVENTIDE_ORE);
        blockWithItem(ModBlocks.EVENTIDE_DEEPSLATE_ORE);

        // Registers Sculk log and wood (normal and stripped) with appropriate axis models
        logBlock((RotatedPillarBlock) ModBlocks.SCULK_LOG.get());
        axisBlock((RotatedPillarBlock) ModBlocks.SCULK_WOOD.get(),
                blockTexture(ModBlocks.SCULK_LOG.get()),
                blockTexture(ModBlocks.SCULK_LOG.get()));
        logBlock((RotatedPillarBlock) ModBlocks.STRIPPED_SCULK_LOG.get());
        axisBlock((RotatedPillarBlock) ModBlocks.STRIPPED_SCULK_WOOD.get(),
                blockTexture(ModBlocks.STRIPPED_SCULK_LOG.get()),
                blockTexture(ModBlocks.STRIPPED_SCULK_LOG.get()));

        // Registers block items for log and wood variants
        blockItem(ModBlocks.SCULK_LOG);
        blockItem(ModBlocks.SCULK_WOOD);
        blockItem(ModBlocks.STRIPPED_SCULK_LOG);
        blockItem(ModBlocks.STRIPPED_SCULK_WOOD);

        // Registers Sculk planks with a standard cube-all block + item model
        blockWithItem(ModBlocks.SCULK_PLANKS);

        // Registers Sculk leaves and sapling models using cutout rendering
        leavesBlock(ModBlocks.SCULK_LEAVES);
        saplingBlock(ModBlocks.SCULK_SAPLING);

        // Registers Sculk stairs and slab variants
        stairsBlock(ModBlocks.SCULK_STAIRS.get(), blockTexture(ModBlocks.SCULK_PLANKS.get()));
        slabBlock(ModBlocks.SCULK_SLAB.get(),
                blockTexture(ModBlocks.SCULK_PLANKS.get()),
                blockTexture(ModBlocks.SCULK_PLANKS.get()));

        // Registers Sculk redstone-interactable blocks
        buttonBlock(ModBlocks.SCULK_BUTTON.get(), blockTexture(ModBlocks.SCULK_PLANKS.get()));
        pressurePlateBlock(ModBlocks.SCULK_PRESSURE_PLATE.get(), blockTexture(ModBlocks.SCULK_PLANKS.get()));

        // Registers Sculk fence, gate, and wall models
        fenceBlock(ModBlocks.SCULK_FENCE.get(), blockTexture(ModBlocks.SCULK_PLANKS.get()));
        fenceGateBlock(ModBlocks.SCULK_FENCE_GATE.get(), blockTexture(ModBlocks.SCULK_PLANKS.get()));
        wallBlock(ModBlocks.SCULK_WALL.get(), blockTexture(ModBlocks.SCULK_PLANKS.get()));

        // Registers Sculk door and trapdoor models using cutout rendering
        doorBlockWithRenderType(ModBlocks.SCULK_DOOR.get(),
                modLoc("block/sculk_door_bottom"),
                modLoc("block/sculk_door_top"),
                "cutout");
        trapdoorBlockWithRenderType(ModBlocks.SCULK_TRAPDOOR.get(),
                modLoc("block/sculk_trapdoor"),
                true,
                "cutout");

        // Registers item models for stairs, slab, pressure plate, fence gate, and trapdoor
        blockItem(ModBlocks.SCULK_STAIRS);
        blockItem(ModBlocks.SCULK_SLAB);
        blockItem(ModBlocks.SCULK_PRESSURE_PLATE);
        blockItem(ModBlocks.SCULK_FENCE_GATE);
        blockItem(ModBlocks.SCULK_TRAPDOOR, "_bottom");

        // Registers Umber log and wood (normal and stripped) with appropriate axis models
        logBlock((RotatedPillarBlock) ModBlocks.UMBER_LOG.get());
        axisBlock((RotatedPillarBlock) ModBlocks.UMBER_WOOD.get(),
                blockTexture(ModBlocks.UMBER_LOG.get()),
                blockTexture(ModBlocks.UMBER_LOG.get()));
        logBlock((RotatedPillarBlock) ModBlocks.STRIPPED_UMBER_LOG.get());
        axisBlock((RotatedPillarBlock) ModBlocks.STRIPPED_UMBER_WOOD.get(),
                blockTexture(ModBlocks.STRIPPED_UMBER_LOG.get()),
                blockTexture(ModBlocks.STRIPPED_UMBER_LOG.get()));

        // Registers block items for log and wood variants
        blockItem(ModBlocks.UMBER_LOG);
        blockItem(ModBlocks.UMBER_WOOD);
        blockItem(ModBlocks.STRIPPED_UMBER_LOG);
        blockItem(ModBlocks.STRIPPED_UMBER_WOOD);

        // Registers Umber planks with a standard cube-all block + item model
        blockWithItem(ModBlocks.UMBER_PLANKS);

        // Registers Umber leaves and sapling models using cutout rendering
        leavesBlock(ModBlocks.UMBER_LEAVES);
        saplingBlock(ModBlocks.UMBER_SAPLING);

        // Registers Umber stairs and slab variants
        stairsBlock(ModBlocks.UMBER_STAIRS.get(), blockTexture(ModBlocks.UMBER_PLANKS.get()));
        slabBlock(ModBlocks.UMBER_SLAB.get(),
                blockTexture(ModBlocks.UMBER_PLANKS.get()),
                blockTexture(ModBlocks.UMBER_PLANKS.get()));

        // Registers Umber redstone-interactable blocks
        buttonBlock(ModBlocks.UMBER_BUTTON.get(), blockTexture(ModBlocks.UMBER_PLANKS.get()));
        pressurePlateBlock(ModBlocks.UMBER_PRESSURE_PLATE.get(), blockTexture(ModBlocks.UMBER_PLANKS.get()));

        // Registers Umber fence, gate, and wall models
        fenceBlock(ModBlocks.UMBER_FENCE.get(), blockTexture(ModBlocks.UMBER_PLANKS.get()));
        fenceGateBlock(ModBlocks.UMBER_FENCE_GATE.get(), blockTexture(ModBlocks.UMBER_PLANKS.get()));
        wallBlock(ModBlocks.UMBER_WALL.get(), blockTexture(ModBlocks.UMBER_PLANKS.get()));

        // Registers Umber door and trapdoor models using cutout rendering
        doorBlockWithRenderType(ModBlocks.UMBER_DOOR.get(),
                modLoc("block/umber_door_bottom"),
                modLoc("block/umber_door_top"),
                "cutout");
        trapdoorBlockWithRenderType(ModBlocks.UMBER_TRAPDOOR.get(),
                modLoc("block/umber_trapdoor"),
                true,
                "cutout");

        // Registers item models for stairs, slab, pressure plate, fence gate, and trapdoor
        blockItem(ModBlocks.UMBER_STAIRS);
        blockItem(ModBlocks.UMBER_SLAB);
        blockItem(ModBlocks.UMBER_PRESSURE_PLATE);
        blockItem(ModBlocks.UMBER_FENCE_GATE);
        blockItem(ModBlocks.UMBER_TRAPDOOR, "_bottom");

        // Limbo blocks and entities
        stackingWallpaperBlock(ModBlocks.LIMBO_WALLPAPER_DIAMOND.get(),
                modLoc("block/limbo_wallpaper_diamond"),
                modLoc("block/limbo_wallpaper_diamond_stacked"),
                modLoc("block/limbo_carpet"),
                modLoc("block/limbo_ceiling_tile"));

        stackingWallpaperBlock(ModBlocks.LIMBO_WALLPAPER_SEGMENTED.get(),
                modLoc("block/limbo_wallpaper_segmented"),
                modLoc("block/limbo_wallpaper_segmented_stacked"),
                modLoc("block/limbo_carpet"),
                modLoc("block/limbo_ceiling_tile"));

        blockItem(ModBlocks.LIMBO_WALLPAPER_DIAMOND);
        blockItem(ModBlocks.LIMBO_WALLPAPER_SEGMENTED);
        simpleBlockWithItem(ModBlocks.LIMBO_CARPET.get(), cubeAll(ModBlocks.LIMBO_CARPET.get()));

        stairsBlock(ModBlocks.LIMBO_CARPET_STAIRS.get(), blockTexture(ModBlocks.LIMBO_CARPET.get()));
        slabBlock(ModBlocks.LIMBO_CARPET_SLAB.get(),
                blockTexture(ModBlocks.LIMBO_CARPET.get()),
                blockTexture(ModBlocks.LIMBO_CARPET.get()));

        blockItem(ModBlocks.LIMBO_CARPET_STAIRS);
        blockItem(ModBlocks.LIMBO_CARPET_SLAB);

        simpleBlockWithItem(ModBlocks.LIMBO_CEILING_TILE.get(), cubeAll(ModBlocks.LIMBO_CEILING_TILE.get()));

        simpleBlockWithItem(
                ModBlocks.LIMBO_FLUORESCENT_LIGHT.get(),
                cubeAll(ModBlocks.LIMBO_FLUORESCENT_LIGHT.get())
        );

    }

    /** Creates a sapling model using a cross texture with cutout rendering */
    private void saplingBlock(DeferredBlock<Block> blockRegistryObject) {
        simpleBlock(
                blockRegistryObject.get(),
                models().cross(
                        BuiltInRegistries.BLOCK.getKey(blockRegistryObject.get()).getPath(),
                        blockTexture(blockRegistryObject.get())
                ).renderType("cutout")
        );
    }

    /** Creates a leaves block + item model using the vanilla leaves template */
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

    private void grassBlockWithItem(DeferredBlock<Block> blockRegistryObject) {
        String path = blockRegistryObject.getId().getPath();

        // Base model: bottom/top/side + particle, with TOP tinted (tintindex 0)
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

        // Overlay model: side overlay only, tinted (tintindex 0), like vanilla grass_block_side_overlay
        // Texture file: assets/shroud/textures/block/<path>_side_overlay.png
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

        // Multipart: render base + overlay together (identical concept to vanilla)
        getMultipartBuilder(blockRegistryObject.get())
                .part().modelFile(baseModel).addModel().end()
                .part().modelFile(overlayModel).addModel().end();

        // Item should use ONLY the base model (vanilla behavior)
        simpleBlockItem(blockRegistryObject.get(), baseModel);
    }

    private void stackingWallpaperBlock(ModStackingBlock block,
                                        ResourceLocation normalSide,
                                        ResourceLocation stackedSide,
                                        ResourceLocation topTexture,
                                        ResourceLocation bottomTexture) {

        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();

        // Not stacked
        ModelFile normalModel = models().cubeBottomTop(name,
                normalSide,
                bottomTexture,
                topTexture
        );

        // Stacked
        ModelFile stackedModel = models().cubeBottomTop(name + "_stacked",
                stackedSide,
                bottomTexture,
                topTexture
        );

        getVariantBuilder(block).forAllStates(state -> {
            boolean stacked = state.getValue(ModStackingBlock.STACKED);
            return ConfiguredModel.builder()
                    .modelFile(stacked ? stackedModel : normalModel)
                    .build();
        });
    }


    /** Registers a block and its item model using a generated cube-all template */
    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    /** Registers a block item model using a direct model reference */
    private void blockItem(DeferredBlock<?> deferredBlock) {
        simpleBlockItem(deferredBlock.get(),
                new ModelFile.UncheckedModelFile("shroud:block/" + deferredBlock.getId().getPath()));
    }

    /** Registers a block item model with a suffix (used for trapdoor bottom model) */
    private void blockItem(DeferredBlock<?> deferredBlock, String appendix) {
        simpleBlockItem(deferredBlock.get(),
                new ModelFile.UncheckedModelFile("shroud:block/" + deferredBlock.getId().getPath() + appendix));
    }
}
