package net.oldmanyounger.shroud.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.LivingSculkEntity;

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

    // >>> This is the method your Shroud class should call
    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}