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
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

import java.util.List;

/** Holds all placed features registered by the Shroud mod */
public class ModPlacedFeatures {

    /** Resource key for the placed SCULK tree feature */
    public static final ResourceKey<PlacedFeature> SCULK_TREE_PLACED =
            registerKey("sculk_tree_placed");

    /** Resource key for the placed emerald block ore feature */
    public static final ResourceKey<PlacedFeature> ORE_EMERALD_BLOCK_PLACED =
            registerKey("ore_emerald_block_placed");

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
                ORE_EMERALD_BLOCK_PLACED,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.ORE_EMERALD_BLOCK),
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
