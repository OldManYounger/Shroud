package net.oldmanyounger.shroud.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.worldgen.feature.SculkSpikeFeature;

/**
 * Registers all custom Feature types for the Shroud mod
 */
public final class ModFeatures {

    private ModFeatures() {
    }

    /** Deferred register for all Feature types owned by Shroud */
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Shroud.MOD_ID);

    /** Feature type for the Sculk spike (IceSpike-like) feature */
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> SCULK_SPIKE =
            FEATURES.register("sculk_spike",
                    () -> new SculkSpikeFeature(NoneFeatureConfiguration.CODEC));

    /** Registers this deferred register with the mod event bus */
    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}
