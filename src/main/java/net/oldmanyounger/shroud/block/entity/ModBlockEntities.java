package net.oldmanyounger.shroud.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Shroud.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModProjectingLightBlockEntity>> LIMBO_FLUORESCENT_LIGHT =
            BLOCK_ENTITIES.register("limbo_fluorescent_light",
                    () -> BlockEntityType.Builder.of(
                            ModProjectingLightBlockEntity::new,
                            ModBlocks.LIMBO_FLUORESCENT_LIGHT.get()
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}