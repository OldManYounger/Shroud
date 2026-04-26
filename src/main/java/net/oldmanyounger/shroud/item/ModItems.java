package net.oldmanyounger.shroud.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.entity.ModEntities;
import net.oldmanyounger.shroud.item.custom.ModArmorItem;
import net.oldmanyounger.shroud.item.custom.ModTotemOfLastBreathItem;

/**
 * Declares and registers all item entries used by Shroud.
 *
 * <p>This class contains registration for materials, utility items, consumables,
 * tools, weapons, armor, smithing templates, and spawn eggs, and exposes a
 * single event-bus registration hook.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * content bootstrap layer that binds item definitions into NeoForge registries
 * so they can participate in crafting, loot, combat, and UI systems.
 */
public class ModItems {

    // ==================================
    //  FIELDS
    // ==================================

    // Deferred item register for the mod namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Shroud.MOD_ID);

    // ==================================
    //  CORE MATERIALS / UTILITY ITEMS
    // ==================================

    // Core sculk material item
    public static final DeferredItem<Item> SCULK_PEARL = ITEMS.register("sculk_pearl",
            () -> new Item(new Item.Properties()));

    // Custom totem item with rarity and single-stack behavior
    public static final DeferredItem<Item> TOTEM_OF_LAST_BREATH = ITEMS.register("totem_of_last_breath",
            () -> new ModTotemOfLastBreathItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    // Consumable vine-based pulp block item
    public static final DeferredItem<Item> GLOOM_PULP = ITEMS.register("gloom_pulp",
            () -> new ItemNameBlockItem(
                    ModBlocks.SCULK_VINES.get(),
                    new Item.Properties().food(
                            new FoodProperties.Builder()
                                    .nutrition(3)
                                    .saturationModifier(0.3F)
                                    .build()
                    )
            ));

    // Alternative sugar from gloamcane
    public static final DeferredItem<Item> GLOAM_SUGAR = ITEMS.register("gloam_sugar",
            () -> new Item(new Item.Properties()));

    // Crafting dust item from gloomstone
    public static final DeferredItem<Item> GLOOMSTONE_DUST = ITEMS.register("gloomstone_dust",
            () -> new Item(new Item.Properties()));

    // ==================================
    //  EVENTIDE MATERIALS
    // ==================================

    // Refined Eventide ingot
    public static final DeferredItem<Item> EVENTIDE_INGOT = ITEMS.register("eventide_ingot",
            () -> new Item(new Item.Properties()));

    // Raw Eventide drop item
    public static final DeferredItem<Item> RAW_EVENTIDE = ITEMS.register("raw_eventide",
            () -> new Item(new Item.Properties()));

    // ==================================
    //  EVENTIDE TOOLS / WEAPONS
    // ==================================

    // Eventide sword
    public static final DeferredItem<SwordItem> EVENTIDE_SWORD = ITEMS.register("eventide_sword",
            () -> new SwordItem(ModToolTiers.EVENTIDE, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.EVENTIDE, 5, -2.4f))));

    // Eventide pickaxe
    public static final DeferredItem<PickaxeItem> EVENTIDE_PICKAXE = ITEMS.register("eventide_pickaxe",
            () -> new PickaxeItem(ModToolTiers.EVENTIDE, new Item.Properties()
                    .attributes(PickaxeItem.createAttributes(ModToolTiers.EVENTIDE, 1.0F, -2.8f))));

    // Eventide shovel
    public static final DeferredItem<ShovelItem> EVENTIDE_SHOVEL = ITEMS.register("eventide_shovel",
            () -> new ShovelItem(ModToolTiers.EVENTIDE, new Item.Properties()
                    .attributes(ShovelItem.createAttributes(ModToolTiers.EVENTIDE, 1.5F, -3.0f))));

    // Eventide axe
    public static final DeferredItem<AxeItem> EVENTIDE_AXE = ITEMS.register("eventide_axe",
            () -> new AxeItem(ModToolTiers.EVENTIDE, new Item.Properties()
                    .attributes(AxeItem.createAttributes(ModToolTiers.EVENTIDE, 6.0F, -3.0f))));

    // Eventide hoe
    public static final DeferredItem<HoeItem> EVENTIDE_HOE = ITEMS.register("eventide_hoe",
            () -> new HoeItem(ModToolTiers.EVENTIDE, new Item.Properties()
                    .attributes(HoeItem.createAttributes(ModToolTiers.EVENTIDE, 0F, -3.0f))));

    // Eventide bow
    public static final DeferredItem<Item> EVENTIDE_BOW = ITEMS.register("eventide_bow",
            () -> new BowItem(new Item.Properties().durability(500)));

    // ==================================
    //  EVENTIDE ARMOR
    // ==================================

    // Eventide helmet with custom armor item behavior
    public static final DeferredItem<ArmorItem> EVENTIDE_HELMET = ITEMS.register("eventide_helmet",
            () -> new ModArmorItem(ModArmorMaterials.EVENTIDE_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(19))));

    // Eventide chestplate
    public static final DeferredItem<ArmorItem> EVENTIDE_CHESTPLATE = ITEMS.register("eventide_chestplate",
            () -> new ArmorItem(ModArmorMaterials.EVENTIDE_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(19))));

    // Eventide leggings
    public static final DeferredItem<ArmorItem> EVENTIDE_LEGGINGS = ITEMS.register("eventide_leggings",
            () -> new ArmorItem(ModArmorMaterials.EVENTIDE_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(19))));

    // Eventide boots
    public static final DeferredItem<ArmorItem> EVENTIDE_BOOTS = ITEMS.register("eventide_boots",
            () -> new ArmorItem(ModArmorMaterials.EVENTIDE_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(19))));

    // ==================================
    //  EVENTIDE SMITHING
    // ==================================

    // Armor trim smithing template item
    public static final DeferredItem<Item> EVENTIDE_SMITHING_TEMPLATE = ITEMS.register(
            "eventide_armor_trim_smithing_template",
            () -> SmithingTemplateItem.createArmorTrimTemplate(
                    ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "eventide")
            )
    );

    // ==================================
    //  SPAWN EGGS
    // ==================================

    // Living Sculk spawn egg
    public static final DeferredItem<Item> LIVING_SCULK_SPAWN_EGG = ITEMS.register("living_sculk_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.LIVING_SCULK, 593685, 1517611,
                    new Item.Properties()));

    // Umbral Howler spawn egg
    public static final DeferredItem<Item> UMBRAL_HOWLER_SPAWN_EGG = ITEMS.register("umbral_howler_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.UMBRAL_HOWLER, 213328, 12305307,
                    new Item.Properties()));

    // Blighted Shade spawn egg
    public static final DeferredItem<Item> BLIGHTED_SHADE_SPAWN_EGG = ITEMS.register("blighted_shade_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BLIGHTED_SHADE, 1517611, 2748379,
                    new Item.Properties()));

    // Twinblight Watcher spawn egg
    public static final DeferredItem<Item> TWINBLIGHT_WATCHER_SPAWN_EGG = ITEMS.register("twinblight_watcher_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.TWINBLIGHT_WATCHER, 593685, 2748379,
                    new Item.Properties()));

    // ==================================
    //  REGISTRATION
    // ==================================

    // Registers item deferred entries on the mod event bus
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}