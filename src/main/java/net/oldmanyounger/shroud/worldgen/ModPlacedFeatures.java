package net.oldmanyounger.shroud.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

import java.util.List;

/** Holds all placed features registered by the Shroud mod */
public class ModPlacedFeatures {

    /** Resource key for the placed SCULK tree feature */
    public static final ResourceKey<PlacedFeature> SCULK_TREE_PLACED =
            registerKey("sculk_tree_placed");

    /** Resource key for the placed SCULK tree feature */
    public static final ResourceKey<PlacedFeature> UMBER_TREE_PLACED =
            registerKey("umber_tree_placed");

    /** Resource key for the placed netherite block ore feature */
    public static final ResourceKey<PlacedFeature> ORE_NETHERITE_BLOCK_PLACED =
            registerKey("ore_netherite_block_placed");

    /** Resource key for the placed Eventide ore feature */
    public static final ResourceKey<PlacedFeature> ORE_EVENTIDE_PLACED =
            registerKey("ore_eventide_placed");

    /** Resource key for the placed SCULK spike feature */
    public static final ResourceKey<PlacedFeature> SCULK_SPIKE_PLACED =
            registerKey("sculk_spike_placed");

    /** Resource key for the placed SCULK arch feature */
    public static final ResourceKey<PlacedFeature> SCULK_ARCH_PLACED =
            registerKey("sculk_arch_placed");



    /** Registers all placed features to the bootstrap context */
    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(
                context,
                SCULK_TREE_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SCULK_TREE),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(3, 0.1f, 2),
                        ModBlocks.SCULK_SAPLING.get()
                )
        );

        register(
                context,
                UMBER_TREE_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.UMBER_TREE),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(3, 0.1f, 2),
                        ModBlocks.UMBER_SAPLING.get()
                )
        );

        register(
                context,
                ORE_NETHERITE_BLOCK_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.ORE_NETHERITE_BLOCK),
                List.of(
                        CountPlacement.of(6),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(
                                VerticalAnchor.absolute(-32),
                                VerticalAnchor.absolute(16)
                        ),
                        BiomeFilter.biome()
                )
        );

        register(
                context,
                ORE_EVENTIDE_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.ORE_EVENTIDE),
                List.of(
                        CountPlacement.of(10),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.triangle(
                                VerticalAnchor.absolute(-24),
                                VerticalAnchor.absolute(63)
                        ),
                        BiomeFilter.biome()
                )
        );

        // Sculk spike placed feature – tune frequency/height as needed
        register(
                context,
                ModPlacedFeatures.SCULK_SPIKE_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SCULK_SPIKE),
                List.of(
                        RarityFilter.onAverageOnceEvery(16),      // ~1 in 16 chunks
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                        BiomeFilter.biome()
                )
        );

        register(
                context,
                ModPlacedFeatures.SCULK_ARCH_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SCULK_ARCH),
                List.of(
                        RarityFilter.onAverageOnceEvery(24),      // tune as desired
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                        BiomeFilter.biome()
                )
        );


    }

    /** Creates a namespaced ResourceKey for a placed feature */
    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(
                Registries.PLACED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, name)
        );
    }

    /** Registers a placed feature using a configured feature and placement modifiers */
    private static void register(
            BootstrapContext<PlacedFeature> context,
            ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> configuration,
            List<PlacementModifier> modifiers
    ) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
