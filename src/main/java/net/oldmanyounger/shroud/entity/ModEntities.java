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
import net.oldmanyounger.shroud.entity.custom.ResonantHulkEntity;
import net.oldmanyounger.shroud.entity.custom.UmbralHowlerEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Shroud.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<LivingSculkEntity>> LIVING_SCULK =
            ENTITY_TYPES.register(
                    "living_sculk",
                    () -> EntityType.Builder.of(LivingSculkEntity::new, MobCategory.MONSTER)
                            .sized(0.9f, 1.9f) // tweak to match your model
                            .build(Shroud.MOD_ID + ":living_sculk")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<UmbralHowlerEntity>> UMBRAL_HOWLER =
            ENTITY_TYPES.register(
                    "umbral_howler",
                    () -> EntityType.Builder.of(UmbralHowlerEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 0.85f) // matches vanilla wolf
                            .build(Shroud.MOD_ID + ":umbral_howler")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<ResonantHulkEntity>> RESONANT_HULK =
            ENTITY_TYPES.register(
                    "resonant_hulk",
                    () -> EntityType.Builder.of(ResonantHulkEntity::new, MobCategory.MONSTER)
                            .sized(0.9f, 2.9f) // Warden-like dimensions
                            .build(Shroud.MOD_ID + ":resonant_hulk")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<BlightedShadeEntity>> BLIGHTED_SHADE =
            ENTITY_TYPES.register(
                    "blighted_shade",
                    () -> EntityType.Builder.of(BlightedShadeEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 2.8f)
                            .build(Shroud.MOD_ID + ":blighted_shade")
            );




    // >>> This is the method your Shroud class should call
    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
