package net.oldmanyounger.shroud.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.item.ModItems;

import java.util.LinkedHashMap;

/** Generates item models for Shroud items that are not handled by the blockstate provider */
public class ModItemModelProvider extends ItemModelProvider {

    /** Creates the item model provider bound to the Shroud mod ID */
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Shroud.MOD_ID, existingFileHelper);
    }

    /** Registers item models for all standalone Shroud items */
    @Override
    protected void registerModels() {
        // Registers the Sculk pearl
        basicItem(ModItems.SCULK_PEARL.get());
        basicItem(ModItems.GLOOM_PULP.get());

        // Registers the Eventide ingot and raw ore texture using item/generated
        basicItem(ModItems.RAW_EVENTIDE.get());
        basicItem(ModItems.EVENTIDE_INGOT.get());

        handheldItem(ModItems.EVENTIDE_SWORD.get());
        handheldItem(ModItems.EVENTIDE_PICKAXE.get());
        handheldItem(ModItems.EVENTIDE_SHOVEL.get());
        handheldItem(ModItems.EVENTIDE_AXE.get());
        handheldItem(ModItems.EVENTIDE_HOE.get());

        trimmedArmorItem(ModItems.EVENTIDE_HELMET);
        trimmedArmorItem(ModItems.EVENTIDE_CHESTPLATE);
        trimmedArmorItem(ModItems.EVENTIDE_LEGGINGS);
        trimmedArmorItem(ModItems.EVENTIDE_BOOTS);

        basicItem(ModItems.EVENTIDE_SMITHING_TEMPLATE.get());

        // Registers the Sculk sapling item texture using item/generated
        saplingItem(ModBlocks.SCULK_BULB);
        saplingItem(ModBlocks.SCULK_SAPLING);

        // Registers inventory models for Sculk button, fence, and wall
        buttonItem(ModBlocks.SCULK_BUTTON, ModBlocks.SCULK_PLANKS);
        fenceItem(ModBlocks.SCULK_FENCE, ModBlocks.SCULK_PLANKS);
        wallItem(ModBlocks.SCULK_WALL, ModBlocks.SCULK_PLANKS);

        // Registers standard generated item model for the Sculk door item
        basicItem(ModBlocks.SCULK_DOOR.asItem());

        // Registers the Umber sapling item texture using item/generated
        saplingItem(ModBlocks.UMBER_SAPLING);

        // Registers inventory models for Umber button, fence, and wall
        buttonItem(ModBlocks.UMBER_BUTTON, ModBlocks.UMBER_PLANKS);
        fenceItem(ModBlocks.UMBER_FENCE, ModBlocks.UMBER_PLANKS);
        wallItem(ModBlocks.UMBER_WALL, ModBlocks.UMBER_PLANKS);

        // Registers standard generated item model for the Umber door item
        basicItem(ModBlocks.UMBER_DOOR.asItem());

        registerSpawnEggModels();
    }

    /** Registers all custom spawn egg item models */
    private void registerSpawnEggModels() {
        withExistingParent(ModItems.LIVING_SCULK_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.UMBRAL_HOWLER_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.RESONANT_HULK_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.BLIGHTED_SHADE_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
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

    /** Registers trim materials **/
    private static LinkedHashMap<ResourceKey<TrimMaterial>, Float> trimMaterials = new LinkedHashMap<>();
    static {
        trimMaterials.put(TrimMaterials.QUARTZ, 0.1F);
        trimMaterials.put(TrimMaterials.IRON, 0.2F);
        trimMaterials.put(TrimMaterials.NETHERITE, 0.3F);
        trimMaterials.put(TrimMaterials.REDSTONE, 0.4F);
        trimMaterials.put(TrimMaterials.COPPER, 0.5F);
        trimMaterials.put(TrimMaterials.GOLD, 0.6F);
        trimMaterials.put(TrimMaterials.EMERALD, 0.7F);
        trimMaterials.put(TrimMaterials.DIAMOND, 0.8F);
        trimMaterials.put(TrimMaterials.LAPIS, 0.9F);
        trimMaterials.put(TrimMaterials.AMETHYST, 1.0F);
    }

    // Shoutout to El_Redstoniano for making this; used for trimming custom armors
    private void trimmedArmorItem(DeferredItem<ArmorItem> itemDeferredItem) {
        final String MOD_ID = Shroud.MOD_ID;

        if (itemDeferredItem.get() instanceof ArmorItem armorItem) {
            trimMaterials.forEach((trimMaterial, value) -> {
                float trimValue = value;

                String armorType = switch (armorItem.getEquipmentSlot()) {
                    case HEAD -> "helmet";
                    case CHEST -> "chestplate";
                    case LEGS -> "leggings";
                    case FEET -> "boots";
                    default -> "";
                };

                String armorItemPath = armorItem.toString();
                String trimPath = "trims/items/" + armorType + "_trim_" + trimMaterial.location().getPath();
                String currentTrimName = armorItemPath + "_" + trimMaterial.location().getPath() + "_trim";
                ResourceLocation armorItemResLoc = ResourceLocation.parse(armorItemPath);
                ResourceLocation trimResLoc = ResourceLocation.parse(trimPath); // minecraft namespace
                ResourceLocation trimNameResLoc = ResourceLocation.parse(currentTrimName);

                // Ensure the trim texture is acknowledged by the ExistingFileHelper
                existingFileHelper.trackGenerated(trimResLoc, PackType.CLIENT_RESOURCES, ".png", "textures");

                // Trimmed armor item model
                getBuilder(currentTrimName)
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", armorItemResLoc.getNamespace() + ":item/" + armorItemResLoc.getPath())
                        .texture("layer1", trimResLoc);

                // Base (non-trimmed) armor item model with trim overrides
                this.withExistingParent(itemDeferredItem.getId().getPath(), mcLoc("item/generated"))
                        .override()
                        .model(new ModelFile.UncheckedModelFile(
                                trimNameResLoc.getNamespace() + ":item/" + trimNameResLoc.getPath()))
                        .predicate(mcLoc("trim_type"), trimValue)
                        .end()
                        .texture("layer0",
                                ResourceLocation.fromNamespaceAndPath(
                                        MOD_ID,
                                        "item/" + itemDeferredItem.getId().getPath()));
            });
        }
    }
}
