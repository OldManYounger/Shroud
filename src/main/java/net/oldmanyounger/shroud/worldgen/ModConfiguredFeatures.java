package net.oldmanyounger.shroud.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.AcaciaFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaPineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.tags.BlockTags;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

import java.util.List;

/** Holds all configured features registered by the Shroud mod */
public class ModConfiguredFeatures {

    /** Resource key for the Sculk tree feature */
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_TREE =
            registerKey("sculk_tree");

    /** Resource key for the Umber tree feature */
    public static final ResourceKey<ConfiguredFeature<?, ?>> UMBER_TREE =
            registerKey("umber_tree");

    /** Resource key for netherite block ore veins */
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_NETHERITE_BLOCK =
            registerKey("ore_netherite_block");

    /** Resource key for Eventide ore veins */
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_EVENTIDE =
            registerKey("ore_eventide");

    /** Resource key for the Sculk spike configured feature */
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_SPIKE =
            registerKey("sculk_spike");

    /** Resource key for the Sculk arch configured feature */
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_ARCH =
            registerKey("sculk_arch");


    /** Registers all configured features to the bootstrap context */
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        register(context, SCULK_TREE, Feature.TREE, buildSculkTree());
        register(context, UMBER_TREE, Feature.TREE, buildUmberTree());
        register(context, ORE_NETHERITE_BLOCK, Feature.ORE, buildNetheriteBlockOre());
        register(context, ORE_EVENTIDE, Feature.ORE, buildEventideOre());

        // Sculk spike – uses custom Feature type and NoneFeatureConfiguration
        register(context, SCULK_SPIKE, ModFeatures.SCULK_SPIKE.get(), NoneFeatureConfiguration.INSTANCE);
        register(context, SCULK_ARCH, ModFeatures.SCULK_ARCH.get(), NoneFeatureConfiguration.INSTANCE);

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

    /** Builds the UMBER tree configuration used for Shroud tree generation */
    private static TreeConfiguration buildUmberTree() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.UMBER_LOG.get()),
                new GiantTrunkPlacer(13, 2, 14),
                BlockStateProvider.simple(ModBlocks.UMBER_LEAVES.get()),
                new MegaPineFoliagePlacer(
                        UniformInt.of(0, 2),
                        ConstantInt.of(0),
                        UniformInt.of(13, 17)
                ),
                new TwoLayersFeatureSize(1, 1, 2)
        )
                .dirt(BlockStateProvider.simple(Blocks.SCULK))
                .build();
    }


    /** Builds the netherite block ore configuration targeting smooth basalt and deepslate replaceables */
    private static OreConfiguration buildNetheriteBlockOre() {
        BlockState netherite = Blocks.NETHERITE_BLOCK.defaultBlockState();

        OreConfiguration.TargetBlockState smoothBasaltTarget =
                OreConfiguration.target(new BlockMatchTest(Blocks.SMOOTH_BASALT), netherite);

        OreConfiguration.TargetBlockState deepslateTarget =
                OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), netherite);

        return new OreConfiguration(
                List.of(smoothBasaltTarget, deepslateTarget),
                9,
                0.0F
        );
    }

    /** Builds the Eventide ore configuration targeting stone and deepslate replaceables */
    private static OreConfiguration buildEventideOre() {
        BlockState eventideOre = ModBlocks.EVENTIDE_ORE.get().defaultBlockState();
        BlockState deepslateEventideOre = ModBlocks.EVENTIDE_DEEPSLATE_ORE.get().defaultBlockState();

        OreConfiguration.TargetBlockState stoneTarget =
                OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), eventideOre);

        OreConfiguration.TargetBlockState deepslateTarget =
                OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), deepslateEventideOre);

        return new OreConfiguration(
                List.of(stoneTarget, deepslateTarget),
                8,
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
