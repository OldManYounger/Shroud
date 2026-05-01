package net.oldmanyounger.shroud.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.*;

/**
 * Registers all custom EntityType entries used by Shroud.
 *
 * <p>This class defines deferred registrations for each custom hostile mob, including category assignment and hitbox dimensions used for spawning, collision, and rendering interactions.
 *
 * <p>In the broader context of the project, this class is part of Shroud's entity bootstrap pipeline that binds custom mob definitions into the NeoForge registry lifecycle.
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
                            .sized(0.9f, 1.9f)
                            .build(Shroud.MOD_ID + ":living_sculk")
            );

    // Umbral Howler entity type registration
    public static final DeferredHolder<EntityType<?>, EntityType<UmbralHowlerEntity>> UMBRAL_HOWLER =
            ENTITY_TYPES.register(
                    "umbral_howler",
                    () -> EntityType.Builder.of(UmbralHowlerEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 0.85f)
                            .build(Shroud.MOD_ID + ":umbral_howler")
            );

    // Blighted Shade entity type registration
    public static final DeferredHolder<EntityType<?>, EntityType<BlightedShadeEntity>> BLIGHTED_SHADE =
            ENTITY_TYPES.register(
                    "blighted_shade",
                    () -> EntityType.Builder.of(BlightedShadeEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 2.8f)
                            .build(Shroud.MOD_ID + ":blighted_shade")
            );

    // Gloam Eyed Amalgam entity type registration
    public static final DeferredHolder<EntityType<?>, EntityType<GloamEyedAmalgamEntity>> GLOAM_EYED_AMALGAM =
            ENTITY_TYPES.register(
                    "gloam_eyed_amalgam",
                    () -> EntityType.Builder.of(GloamEyedAmalgamEntity::new, MobCategory.MONSTER)
                            .sized(0.9f, 2.9f)
                            .build(Shroud.MOD_ID + ":gloam_eyed_amalgam")
            );

    // Gloam Eyed Amalgam shulker-style sculk shot projectile registration
    public static final DeferredHolder<EntityType<?>, EntityType<GloamEyedAmalgamSculkShotEntity>> GLOAM_EYED_AMALGAM_SCULK_SHOT =
            ENTITY_TYPES.register(
                    "gloam_eyed_amalgam_sculk_shot",
                    () -> EntityType.Builder.<GloamEyedAmalgamSculkShotEntity>of(
                                    (entityType, level) ->
                                            new GloamEyedAmalgamSculkShotEntity(entityType, level), MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build(Shroud.MOD_ID + ":gloam_eyed_amalgam_sculk_shot")
            );

    // Registers entity deferred entries on the mod event bus
    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}