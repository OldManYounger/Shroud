package net.oldmanyounger.shroud.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.entity.ModEntities;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.item.custom.ModArmorItem;
import net.oldmanyounger.shroud.item.custom.TotemOfLastBreathItem;

import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Shroud.MOD_ID);

    public static final DeferredItem<Item> SCULK_PEARL = ITEMS.register("sculk_pearl",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TOTEM_OF_LAST_BREATH = ITEMS.register("totem_of_last_breath",
            () -> new TotemOfLastBreathItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
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
    public static final DeferredItem<Item> GLOOMSTONE_DUST = ITEMS.register("gloomstone_dust",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> EVENTIDE_INGOT = ITEMS.register("eventide_ingot",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAW_EVENTIDE = ITEMS.register("raw_eventide",
            () -> new Item(new Item.Properties()));

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

    // Eventide helmet
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

    // Armor trim
    public static final DeferredItem<Item> EVENTIDE_SMITHING_TEMPLATE = ITEMS.register(
            "eventide_armor_trim_smithing_template",
            () -> SmithingTemplateItem.createArmorTrimTemplate(
                    ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "eventide")
            )
    );

    // Spawn eggs
    public static final DeferredItem<Item> LIVING_SCULK_SPAWN_EGG = ITEMS.register("living_sculk_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.LIVING_SCULK, 0x31afaf, 0xffac00,
                    new Item.Properties()));
    public static final DeferredItem<Item> UMBRAL_HOWLER_SPAWN_EGG = ITEMS.register("umbral_howler_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.UMBRAL_HOWLER, 0x0A0A0A, 0x6A0DAD,
                    new Item.Properties()));
    public static final DeferredItem<Item> RESONANT_HULK_SPAWN_EGG = ITEMS.register("resonant_hulk_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.RESONANT_HULK, 0x1F2E2E, 0x8A6BFF,
                    new Item.Properties()));
    public static final DeferredItem<Item> BLIGHTED_SHADE_SPAWN_EGG = ITEMS.register("blighted_shade_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BLIGHTED_SHADE, 0x1B1B2E, 0x7B2CBF,
                    new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}