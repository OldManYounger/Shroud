package net.oldmanyounger.shroud.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.BlightedShadeEntity;
import net.oldmanyounger.shroud.entity.custom.LivingSculkEntity;
import net.oldmanyounger.shroud.entity.custom.TwinblightWatcherEntity;
import net.oldmanyounger.shroud.entity.custom.UmbralHowlerEntity;

/**
 * Registers all custom EntityType entries used by Shroud
 *
 * <p>This class defines deferred registrations for each custom hostile mob,
 * including category assignment and hitbox dimensions used for spawning,
 * collision, and rendering interactions
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * entity bootstrap pipeline that binds custom mob definitions into the
 * NeoForge registry lifecycle
 */
public class ModEntities {

    // Deferred register for all Shroud entity types
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Shroud.MOD_ID);

    // Living Sculk entity type registration
    public static final DeferredHolder<EntityType<?>, EntityType<LivingSculkEntity>> LIVING_SCULK =
            ENTITY_TYPES.register(
                    "living_sculk",
                    () -> EntityType.Builder.of(LivingSculkEntity::new, MobCategory.MONSTER)
                            .sized(0.9f, 1.9f) // Sized to match Living Sculk model proportions
                            .build(Shroud.MOD_ID + ":living_sculk")
            );

    // Umbral Howler entity type registration
    public static final DeferredHolder<EntityType<?>, EntityType<UmbralHowlerEntity>> UMBRAL_HOWLER =
            ENTITY_TYPES.register(
                    "umbral_howler",
                    () -> EntityType.Builder.of(UmbralHowlerEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 0.85f) // Wolf-like footprint and height
                            .build(Shroud.MOD_ID + ":umbral_howler")
            );

    // Blighted Shade entity type registration
    public static final DeferredHolder<EntityType<?>, EntityType<BlightedShadeEntity>> BLIGHTED_SHADE =
            ENTITY_TYPES.register(
                    "blighted_shade",
                    () -> EntityType.Builder.of(BlightedShadeEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 2.8f) // Tall narrow silhouette
                            .build(Shroud.MOD_ID + ":blighted_shade")
            );

    // Twinblight Watcher entity type registration
    public static final DeferredHolder<EntityType<?>, EntityType<TwinblightWatcherEntity>> TWINBLIGHT_WATCHER =
            ENTITY_TYPES.register(
                    "twinblight_watcher",
                    () -> EntityType.Builder.of(TwinblightWatcherEntity::new, MobCategory.MONSTER)
                            .sized(1.4f, 2.8f) // Same footprint as Living Sculk
                            .build(Shroud.MOD_ID + ":twinblight_watcher")
            );

    // Registers entity deferred entries on the mod event bus
    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}