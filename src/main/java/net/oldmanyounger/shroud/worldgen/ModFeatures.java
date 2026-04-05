package net.oldmanyounger.shroud.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.worldgen.feature.SculkArchFeature;
import net.oldmanyounger.shroud.worldgen.feature.SculkEmitterFeature;
import net.oldmanyounger.shroud.worldgen.feature.SculkSpikeFeature;

/**
 * Registers custom feature types used by Shroud world generation.
 *
 * <p>This class declares codec-backed feature type registrations for custom
 * no-config features and provides event-bus registration wiring.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * worldgen bootstrap layer that links feature implementations to the feature
 * registry for configured and placed feature usage.
 */
public final class ModFeatures {

    // Deferred register for all custom feature types
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Shroud.MOD_ID);

    // Feature type for sculk spike generation
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> SCULK_SPIKE =
            FEATURES.register("sculk_spike",
                    () -> new SculkSpikeFeature(NoneFeatureConfiguration.CODEC));

    // Feature type for sculk arch generation
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> SCULK_ARCH =
            FEATURES.register("sculk_arch",
                    () -> new SculkArchFeature(NoneFeatureConfiguration.CODEC));

    // Feature type for single sculk emitter placement
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> SCULK_EMITTER =
            FEATURES.register("sculk_emitter",
                    () -> new SculkEmitterFeature(NoneFeatureConfiguration.CODEC));

    // Prevents instantiation of this static registration class
    private ModFeatures() {
    }

    // Registers feature deferred entries on the mod event bus
    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}