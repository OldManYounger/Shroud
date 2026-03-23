package net.oldmanyounger.shroud.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Central registry class for all Shroud block entity types.
 *
 * <p>This class is responsible for declaring each block entity type and linking
 * it to the blocks that should create it at runtime. It acts as the bridge
 * between registered blocks and their server/client-side logic containers,
 * ensuring that blocks with persistent state or ticking behavior can function
 * correctly once placed in the world.
 *
 * <p>In the broader context of the project, this file serves as the single
 * registration point for Shroud's block entities, keeping special block logic
 * organized and discoverable in the same way that {@code ModBlocks} does for
 * blocks themselves.
 */
public class ModBlockEntities {

    // Deferred register that owns all Shroud block entity type registrations
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Shroud.MOD_ID);

    // Block entity for the visible Limbo fluorescent light block that manages projected helper lights
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModProjectingLightBlockEntity>> LIMBO_FLUORESCENT_LIGHT =
            BLOCK_ENTITIES.register("limbo_fluorescent_light",
                    () -> BlockEntityType.Builder.of(
                            ModProjectingLightBlockEntity::new,
                            ModBlocks.LIMBO_FLUORESCENT_LIGHT.get()
                    ).build(null));

    // Block entity for the sculk emitter block that periodically emits particles and sound
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModSculkEmitterBlockEntity>> SCULK_EMITTER =
            BLOCK_ENTITIES.register("sculk_emitter",
                    () -> BlockEntityType.Builder.of(
                            ModSculkEmitterBlockEntity::new,
                            ModBlocks.SCULK_EMITTER.get()
                    ).build(null));

    // Registers all declared block entity types onto the mod event bus
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}