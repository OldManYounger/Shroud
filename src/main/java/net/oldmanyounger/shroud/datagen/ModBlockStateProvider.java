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

/** Generates all blockstates and block models for Shroud blocks */
public class ModBlockStateProvider extends BlockStateProvider {

    /** Constructs the blockstate provider using the mod ID and file helper */
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Shroud.MOD_ID, exFileHelper);
    }

    /** Registers blockstates and models for every Shroud block */
    @Override
    protected void registerStatesAndModels() {

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
