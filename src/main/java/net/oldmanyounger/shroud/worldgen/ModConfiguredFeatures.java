package net.oldmanyounger.shroud.worldgen;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.levelgen.feature.foliageplacers.CherryFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaPineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AttachedToLeavesDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.CherryTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

import java.util.List;

/**
 * Declares and bootstraps all configured features used by Shroud world generation.
 *
 * <p>This class defines configured trees, ores, and custom no-config features, then
 * registers them into the configured feature registry through a single bootstrap path.
 *
 * <p>In the broader context of the project, this class is part of Shroud's worldgen
 * configuration layer that converts block and feature design intent into reusable
 * registry-backed feature definitions consumed by placed features and biomes.
 */
public class ModConfiguredFeatures {

    // ==================================
    //  RESOURCE KEYS
    // ==================================

    // Tree configured feature keys
    public static final ResourceKey<ConfiguredFeature<?, ?>> VIRELITH_TREE = registerKey("virelith_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCRAGGLE_TREE = registerKey("scraggle_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> UMBER_TREE = registerKey("umber_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BALDACHIN_TREE = registerKey("baldachin_tree");

    // Ore configured feature keys
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_NETHERITE_BLOCK = registerKey("ore_netherite_block");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_EVENTIDE = registerKey("ore_eventide");

    // Custom feature keys
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_SPIKE = registerKey("sculk_spike");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_ARCH = registerKey("sculk_arch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_EMITTER = registerKey("sculk_emitter");

    // ==================================
    //  BOOTSTRAP
    // ==================================

    // Registers all configured feature entries
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        register(context, VIRELITH_TREE, Feature.TREE, buildVirelithTree());
        register(context, SCRAGGLE_TREE, Feature.TREE, buildScraggleTree());
        register(context, UMBER_TREE, Feature.TREE, buildUmberTree());
        register(context, BALDACHIN_TREE, Feature.TREE, buildBaldachinTree());

        register(context, ORE_NETHERITE_BLOCK, Feature.ORE, buildNetheriteBlockOre());
        register(context, ORE_EVENTIDE, Feature.ORE, buildEventideOre());

        register(context, SCULK_SPIKE, ModFeatures.SCULK_SPIKE.get(), NoneFeatureConfiguration.INSTANCE);
        register(context, SCULK_ARCH, ModFeatures.SCULK_ARCH.get(), NoneFeatureConfiguration.INSTANCE);
        register(context, SCULK_EMITTER, ModFeatures.SCULK_EMITTER.get(), NoneFeatureConfiguration.INSTANCE);
    }

    // ==================================
    //  TREE CONFIGURATIONS
    // ==================================

    // Builds Virelith tree configuration
    private static TreeConfiguration buildVirelithTree() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.VIRELITH_LOG.get()),
                new ForkingTrunkPlacer(5, 2, 3),
                BlockStateProvider.simple(ModBlocks.VIRELITH_LEAVES.get()),
                new AcaciaFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0)),
                new TwoLayersFeatureSize(1, 0, 2)
        )
                .dirt(BlockStateProvider.simple(Blocks.SCULK))
                .build();
    }

    // Builds Scraggle tree configuration
    private static TreeConfiguration buildScraggleTree() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.VIRELITH_LOG.get()),
                new ForkingTrunkPlacer(3, 1, 2),
                BlockStateProvider.simple(ModBlocks.VIRELITH_LEAVES.get()),
                new AcaciaFoliagePlacer(ConstantInt.of(1), ConstantInt.of(0)),
                new TwoLayersFeatureSize(0, 0, 1)
        )
                .dirt(BlockStateProvider.simple(Blocks.SCULK))
                .ignoreVines()
                .build();
    }

    // Builds Baldachin tree configuration
    private static TreeConfiguration buildBaldachinTree() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.VIRELITH_LOG.get()),
                new CherryTrunkPlacer(
                        9,
                        3,
                        0,
                        UniformInt.of(1, 3),
                        UniformInt.of(2, 4),
                        UniformInt.of(-4, -3),
                        ConstantInt.of(-1)
                ),
                BlockStateProvider.simple(ModBlocks.VIRELITH_LEAVES.get()),
                new CherryFoliagePlacer(
                        ConstantInt.of(4),
                        ConstantInt.of(0),
                        ConstantInt.of(5),
                        0.25F,
                        0.5F,
                        0.16666667F,
                        0.33333334F
                ),
                new TwoLayersFeatureSize(1, 0, 2)
        )
                .dirt(BlockStateProvider.simple(Blocks.SCULK))
                .decorators(List.of(
                        new AttachedToLeavesDecorator(
                                0.02F,
                                1,
                                1,
                                BlockStateProvider.simple(ModBlocks.GHOST_BLOOM.get()),
                                2,
                                List.of(Direction.DOWN)
                        )
                ))
                .ignoreVines()
                .build();
    }

    // Builds Umber tree configuration
    private static TreeConfiguration buildUmberTree() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.UMBER_LOG.get()),
                new GiantTrunkPlacer(13, 2, 14),
                BlockStateProvider.simple(ModBlocks.UMBER_LEAVES.get()),
                new MegaPineFoliagePlacer(
                        ConstantInt.of(0),
                        ConstantInt.of(0),
                        UniformInt.of(13, 17)
                ),
                new TwoLayersFeatureSize(1, 1, 2)
        )
                .dirt(BlockStateProvider.simple(Blocks.SCULK))
                .ignoreVines()
                .decorators(List.of(new LeaveVineDecorator(0.75F)))
                .build();
    }

    // ==================================
    //  ORE CONFIGURATIONS
    // ==================================

    // Builds multi-target netherite block ore configuration
    private static OreConfiguration buildNetheriteBlockOre() {
        BlockState netherite = Blocks.NETHERITE_BLOCK.defaultBlockState();

        OreConfiguration.TargetBlockState smoothBasaltTarget =
                OreConfiguration.target(new BlockMatchTest(Blocks.SMOOTH_BASALT), netherite);

        OreConfiguration.TargetBlockState deepslateTarget =
                OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), netherite);

        return new OreConfiguration(List.of(smoothBasaltTarget, deepslateTarget), 9, 0.0F);
    }

    // Builds eventide ore configuration across vanilla and sculk hosts
    private static OreConfiguration buildEventideOre() {
        BlockState eventideOre = ModBlocks.EVENTIDE_ORE.get().defaultBlockState();
        BlockState deepslateEventideOre = ModBlocks.EVENTIDE_DEEPSLATE_ORE.get().defaultBlockState();

        BlockState sculkStoneEventideOre = ModBlocks.SCULK_STONE_EVENTIDE_ORE.get().defaultBlockState();
        BlockState sculkDeepslateEventideOre = ModBlocks.SCULK_DEEPSLATE_EVENTIDE_ORE.get().defaultBlockState();

        OreConfiguration.TargetBlockState stoneTarget =
                OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), eventideOre);

        OreConfiguration.TargetBlockState deepslateTarget =
                OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), deepslateEventideOre);

        OreConfiguration.TargetBlockState sculkStoneTarget =
                OreConfiguration.target(new BlockMatchTest(ModBlocks.SCULK_STONE.get()), sculkStoneEventideOre);

        OreConfiguration.TargetBlockState sculkDeepslateTarget =
                OreConfiguration.target(new BlockMatchTest(ModBlocks.SCULK_DEEPSLATE.get()), sculkDeepslateEventideOre);

        return new OreConfiguration(List.of(sculkStoneTarget, sculkDeepslateTarget, stoneTarget, deepslateTarget), 8, 0.0F);
    }

    // ==================================
    //  REGISTRY HELPERS
    // ==================================

    // Creates configured feature resource key from local name
    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, name));
    }

    // Registers configured feature entry into bootstrap context
    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
            BootstrapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key,
            F feature,
            FC configuration
    ) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}