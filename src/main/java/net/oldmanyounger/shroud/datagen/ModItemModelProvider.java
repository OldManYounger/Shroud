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

/**
 * Generates item models for Shroud items that are not fully handled through
 * blockstate generation alone.
 *
 * <p>This provider covers standard generated items, handheld tools and weapons,
 * sapling-style items, block-derived inventory models, spawn eggs, and trimmed
 * armor model overrides. It keeps all non-blockstate item model generation in a
 * single place so the mod's item assets are easier to maintain and expand.
 *
 * <p>In the broader context of the project, this class is the item-side
 * counterpart to the blockstate provider, turning Java registrations into the
 * item model JSON resources required by the client.
 */
public class ModItemModelProvider extends ItemModelProvider {

    // Trim material predicate values used for generating armor trim overrides
    private static final LinkedHashMap<ResourceKey<TrimMaterial>, Float> trimMaterials = new LinkedHashMap<>();

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

    // Creates the item model provider for the Shroud namespace
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Shroud.MOD_ID, existingFileHelper);
    }

    // Registers all standalone item models produced by this provider
    @Override
    protected void registerModels() {
        // Basic generated items
        basicItem(ModItems.SCULK_PEARL.get());
        basicItem(ModItems.GLOOM_PULP.get());
        basicItem(ModItems.GLOAM_SUGAR.get());
        basicItem(ModItems.GLOOMSTONE_DUST.get());
        basicItem(ModItems.TOTEM_OF_LAST_BREATH.get());
        basicItem(ModItems.RAW_EVENTIDE.get());
        basicItem(ModItems.EVENTIDE_INGOT.get());
        basicItem(ModItems.EVENTIDE_SMITHING_TEMPLATE.get());

        wallItem(ModBlocks.SCULK_COBBLESTONE_WALL, ModBlocks.SCULK_COBBLESTONE);
        wallItem(ModBlocks.SCULK_STONE_BRICK_WALL, ModBlocks.SCULK_STONE_BRICKS);
        wallItem(ModBlocks.COBBLED_SCULK_DEEPSLATE_WALL, ModBlocks.COBBLED_SCULK_DEEPSLATE);
        wallItem(ModBlocks.SCULK_DEEPSLATE_BRICK_WALL, ModBlocks.SCULK_DEEPSLATE_BRICKS);
        wallItem(ModBlocks.SCULK_DEEPSLATE_TILE_WALL, ModBlocks.SCULK_DEEPSLATE_TILES);

        // Eventide tools and weapons
        handheldItem(ModItems.EVENTIDE_SWORD.get());
        handheldItem(ModItems.EVENTIDE_PICKAXE.get());
        handheldItem(ModItems.EVENTIDE_SHOVEL.get());
        handheldItem(ModItems.EVENTIDE_AXE.get());
        handheldItem(ModItems.EVENTIDE_HOE.get());

        // Eventide armor with trim support
        trimmedArmorItem(ModItems.EVENTIDE_HELMET);
        trimmedArmorItem(ModItems.EVENTIDE_CHESTPLATE);
        trimmedArmorItem(ModItems.EVENTIDE_LEGGINGS);
        trimmedArmorItem(ModItems.EVENTIDE_BOOTS);

        // Plant and block-derived items
        saplingItem(ModBlocks.SCULK_BULB);
        saplingItem(ModBlocks.GHOST_BLOOM);
        basicItem(ModBlocks.GLOAMCANE.asItem());
        saplingItem(ModBlocks.VIRELITH_SAPLING);
        buttonItem(ModBlocks.VIRELITH_BUTTON, ModBlocks.VIRELITH_PLANKS);
        fenceItem(ModBlocks.VIRELITH_FENCE, ModBlocks.VIRELITH_PLANKS);
        wallItem(ModBlocks.VIRELITH_WALL, ModBlocks.VIRELITH_PLANKS);
        basicItem(ModBlocks.VIRELITH_DOOR.asItem());

        saplingItem(ModBlocks.UMBER_SAPLING);
        buttonItem(ModBlocks.UMBER_BUTTON, ModBlocks.UMBER_PLANKS);
        fenceItem(ModBlocks.UMBER_FENCE, ModBlocks.UMBER_PLANKS);
        wallItem(ModBlocks.UMBER_WALL, ModBlocks.UMBER_PLANKS);
        basicItem(ModBlocks.UMBER_DOOR.asItem());

        // Spawn eggs
        registerSpawnEggModels();
    }

    // Registers template_spawn_egg models for all custom spawn eggs
    private void registerSpawnEggModels() {
        withExistingParent(ModItems.LIVING_SCULK_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.UMBRAL_HOWLER_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.BLIGHTED_SHADE_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.TWINBLIGHT_WATCHER_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
    }

    // Creates an item/generated model for a plant-like block item using the block texture
    private ItemModelBuilder saplingItem(DeferredBlock<Block> item) {
        return withExistingParent(item.getId().getPath(), ResourceLocation.parse("item/generated"))
                .texture("layer0",
                        ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "block/" + item.getId().getPath()));
    }

    // Creates a handheld item model for tools and weapons
    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(), ResourceLocation.parse("item/handheld"))
                .texture("layer0",
                        ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "item/" + item.getId().getPath()));
    }

    // Creates the inventory model for a button item using the supplied base block texture
    public void buttonItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        withExistingParent(block.getId().getPath(), mcLoc("block/button_inventory"))
                .texture("texture",
                        ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "block/" + baseBlock.getId().getPath()));
    }

    // Creates the inventory model for a fence item using the supplied base block texture
    public void fenceItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        withExistingParent(block.getId().getPath(), mcLoc("block/fence_inventory"))
                .texture("texture",
                        ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "block/" + baseBlock.getId().getPath()));
    }

    // Creates the inventory model for a wall item using the supplied base block texture
    public void wallItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        withExistingParent(block.getId().getPath(), mcLoc("block/wall_inventory"))
                .texture("wall",
                        ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "block/" + baseBlock.getId().getPath()));
    }

    // Generates trim override item models for a custom armor piece
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
                ResourceLocation trimResLoc = ResourceLocation.parse(trimPath);
                ResourceLocation trimNameResLoc = ResourceLocation.parse(currentTrimName);

                existingFileHelper.trackGenerated(trimResLoc, PackType.CLIENT_RESOURCES, ".png", "textures");

                getBuilder(currentTrimName)
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", armorItemResLoc.getNamespace() + ":item/" + armorItemResLoc.getPath())
                        .texture("layer1", trimResLoc);

                this.withExistingParent(itemDeferredItem.getId().getPath(), mcLoc("item/generated"))
                        .override()
                        .model(new ModelFile.UncheckedModelFile(
                                trimNameResLoc.getNamespace() + ":item/" + trimNameResLoc.getPath()))
                        .predicate(mcLoc("trim_type"), trimValue)
                        .end()
                        .texture("layer0",
                                ResourceLocation.fromNamespaceAndPath(MOD_ID, "item/" + itemDeferredItem.getId().getPath()));
            });
        }
    }
}