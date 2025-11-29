package net.oldmanyounger.shroud.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.item.ModItems;

/** Generates item models for Shroud items that are not handled by the blockstate provider */
public class ModItemModelProvider extends ItemModelProvider {

    /** Creates the item model provider bound to the Shroud mod ID */
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Shroud.MOD_ID, existingFileHelper);
    }

    /** Registers item models for all standalone Shroud items */
    @Override
    protected void registerModels() {

        // Registers the Sculk sapling item texture using item/generated
        saplingItem(ModBlocks.SCULK_SAPLING);

        // Registers inventory models for Sculk button, fence, and wall
        buttonItem(ModBlocks.SCULK_BUTTON, ModBlocks.SCULK_PLANKS);
        fenceItem(ModBlocks.SCULK_FENCE, ModBlocks.SCULK_PLANKS);
        wallItem(ModBlocks.SCULK_WALL, ModBlocks.SCULK_PLANKS);

        // Registers standard generated item model for the Sculk door item
        basicItem(ModBlocks.SCULK_DOOR.asItem());

        // Registers item model for entity spawn egg(s)
        withExistingParent(ModItems.LIVING_SCULK_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
    }

    /** Creates the model for a Sculk sapling using item/generated */
    private ItemModelBuilder saplingItem(DeferredBlock<Block> item) {
        return withExistingParent(
                item.getId().getPath(),
                ResourceLocation.parse("item/generated")
        ).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(
                        Shroud.MOD_ID,
                        "block/" + item.getId().getPath()
                ));
    }

    /** Creates a handheld-style item model for tools or weapons */
    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(
                item.getId().getPath(),
                ResourceLocation.parse("item/handheld")
        ).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(
                        Shroud.MOD_ID,
                        "item/" + item.getId().getPath()
                ));
    }

    /** Registers the inventory model for a Sculk button item */
    public void buttonItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        withExistingParent(block.getId().getPath(), mcLoc("block/button_inventory"))
                .texture("texture",
                        ResourceLocation.fromNamespaceAndPath(
                                Shroud.MOD_ID,
                                "block/" + baseBlock.getId().getPath()
                        ));
    }

    /** Registers the inventory model for a Sculk fence item */
    public void fenceItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        withExistingParent(block.getId().getPath(), mcLoc("block/fence_inventory"))
                .texture("texture",
                        ResourceLocation.fromNamespaceAndPath(
                                Shroud.MOD_ID,
                                "block/" + baseBlock.getId().getPath()
                        ));
    }

    /** Registers the inventory model for a Sculk wall item */
    public void wallItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        withExistingParent(block.getId().getPath(), mcLoc("block/wall_inventory"))
                .texture("wall",
                        ResourceLocation.fromNamespaceAndPath(
                                Shroud.MOD_ID,
                                "block/" + baseBlock.getId().getPath()
                        ));
    }
}
