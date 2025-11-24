package net.oldmanyounger.shroud.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.AcaciaFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.tags.BlockTags;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

import java.util.List;

/** Holds all configured features registered by the Shroud mod */
public class ModConfiguredFeatures {

    /** Resource key for the SCULK tree feature */
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_TREE =
            registerKey("sculk_tree");

    /** Resource key for emerald block ore veins */
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_EMERALD_BLOCK =
            registerKey("ore_emerald_block");

    /** Registers all configured features to the bootstrap context */
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        register(context, SCULK_TREE, Feature.TREE, buildSculkTree());
        register(context, ORE_EMERALD_BLOCK, Feature.ORE, buildEmeraldBlockOre());
    }

    /** Builds the SCULK tree configuration used for Shroud tree generation */
    private static TreeConfiguration buildSculkTree() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.SCULK_LOG.get()),
                new ForkingTrunkPlacer(5, 2, 3),
                BlockStateProvider.simple(ModBlocks.SCULK_LEAVES.get()),
                new AcaciaFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0)),
                new TwoLayersFeatureSize(1, 0, 2)
        )
                .dirt(BlockStateProvider.simple(Blocks.SCULK))
                .build();
    }

    /** Builds the emerald block ore configuration targeting smooth basalt and deepslate replaceables */
    private static OreConfiguration buildEmeraldBlockOre() {
        BlockState emerald = Blocks.EMERALD_BLOCK.defaultBlockState();

        OreConfiguration.TargetBlockState smoothBasaltTarget =
                OreConfiguration.target(new BlockMatchTest(Blocks.SMOOTH_BASALT), emerald);

        OreConfiguration.TargetBlockState deepslateTarget =
                OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), emerald);

        return new OreConfiguration(
                List.of(smoothBasaltTarget, deepslateTarget),
                9,
                0.0F
        );
    }

    /** Creates a namespaced ResourceKey for a configured feature */
    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(
                Registries.CONFIGURED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, name)
        );
    }

    /** Registers a configured feature and its configuration into the bootstrap context */
    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
            BootstrapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key,
            F feature,
            FC configuration
    ) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}