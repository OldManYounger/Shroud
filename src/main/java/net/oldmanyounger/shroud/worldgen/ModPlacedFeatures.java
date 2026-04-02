package net.oldmanyounger.shroud.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

import java.util.List;

public class ModPlacedFeatures {

    public static final ResourceKey<PlacedFeature> VIRELITH_TREE_PLACED = registerKey("virelith_tree_placed");
    public static final ResourceKey<PlacedFeature> SCRAGGLE_TREE_PLACED = registerKey("scraggle_tree_placed");
    public static final ResourceKey<PlacedFeature> UMBER_TREE_PLACED = registerKey("umber_tree_placed");
    public static final ResourceKey<PlacedFeature> BALDACHIN_TREE_PLACED = registerKey("baldachin_tree_placed");

    public static final ResourceKey<PlacedFeature> ORE_NETHERITE_BLOCK_PLACED = registerKey("ore_netherite_block_placed");
    public static final ResourceKey<PlacedFeature> ORE_EVENTIDE_PLACED = registerKey("ore_eventide_placed");

    public static final ResourceKey<PlacedFeature> SCULK_SPIKE_PLACED = registerKey("sculk_spike_placed");
    public static final ResourceKey<PlacedFeature> SCULK_ARCH_PLACED = registerKey("sculk_arch_placed");
    public static final ResourceKey<PlacedFeature> SCULK_EMITTER_PLACED = registerKey("sculk_emitter_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(
                context,
                VIRELITH_TREE_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.VIRELITH_TREE),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(3, 0.1f, 2),
                        ModBlocks.VIRELITH_SAPLING.get()
                )
        );

        // Much sparser than SCULK_TREE
        register(
                context,
                SCRAGGLE_TREE_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SCRAGGLE_TREE),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(1, 0.1f, 0),
                        ModBlocks.VIRELITH_SAPLING.get()
                )
        );

        register(
                context,
                UMBER_TREE_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.UMBER_TREE),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(4, 0.1f, 2),
                        ModBlocks.UMBER_SAPLING.get()
                )
        );

        register(
                context,
                BALDACHIN_TREE_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.BALDACHIN_TREE),
                List.of(
                        CountPlacement.of(1),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                        BiomeFilter.biome()
                )
        );

        register(
                context,
                ORE_NETHERITE_BLOCK_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.ORE_NETHERITE_BLOCK),
                List.of(
                        CountPlacement.of(6),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(16)),
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
                        HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(63)),
                        BiomeFilter.biome()
                )
        );

        register(
                context,
                SCULK_SPIKE_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SCULK_SPIKE),
                List.of(
                        RarityFilter.onAverageOnceEvery(16),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                        BiomeFilter.biome()
                )
        );

        register(
                context,
                SCULK_ARCH_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SCULK_ARCH),
                List.of(
                        RarityFilter.onAverageOnceEvery(24),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                        BiomeFilter.biome()
                )
        );

        register(
                context,
                SCULK_EMITTER_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SCULK_EMITTER),
                List.of(
                        RarityFilter.onAverageOnceEvery(3),
                        InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                        RandomOffsetPlacement.vertical(ConstantInt.of(-1)),
                        BiomeFilter.biome()
                )
        );
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, name));
    }

    private static void register(
            BootstrapContext<PlacedFeature> context,
            ResourceKey<PlacedFeature> key,
            Holder<ConfiguredFeature<?, ?>> configuration,
            List<PlacementModifier> modifiers
    ) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}