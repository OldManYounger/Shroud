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

/** Holds all configured features registered by the Shroud mod */
public class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_TREE = registerKey("sculk_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCRAGGLE_TREE = registerKey("scraggle_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> UMBER_TREE = registerKey("umber_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BALDACHIN_TREE = registerKey("baldachin_tree");

    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_NETHERITE_BLOCK = registerKey("ore_netherite_block");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_EVENTIDE = registerKey("ore_eventide");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_SCULK_GRAVEL_UPPER = registerKey("ore_sculk_gravel_upper");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_SCULK_GRAVEL_LOWER = registerKey("ore_sculk_gravel_lower");

    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_SPIKE = registerKey("sculk_spike");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_ARCH = registerKey("sculk_arch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SCULK_EMITTER = registerKey("sculk_emitter");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        register(context, SCULK_TREE, Feature.TREE, buildSculkTree());
        register(context, SCRAGGLE_TREE, Feature.TREE, buildScraggleTree());
        register(context, UMBER_TREE, Feature.TREE, buildUmberTree());
        register(context, BALDACHIN_TREE, Feature.TREE, buildBaldachinTree());

        register(context, ORE_NETHERITE_BLOCK, Feature.ORE, buildNetheriteBlockOre());
        register(context, ORE_EVENTIDE, Feature.ORE, buildEventideOre());

        register(context, SCULK_SPIKE, ModFeatures.SCULK_SPIKE.get(), NoneFeatureConfiguration.INSTANCE);
        register(context, SCULK_ARCH, ModFeatures.SCULK_ARCH.get(), NoneFeatureConfiguration.INSTANCE);
        register(context, SCULK_EMITTER, ModFeatures.SCULK_EMITTER.get(), NoneFeatureConfiguration.INSTANCE);
    }

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

    private static TreeConfiguration buildScraggleTree() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.SCULK_LOG.get()),
                // Short trunk, occasional fork-ish behavior
                new ForkingTrunkPlacer(3, 1, 2),
                BlockStateProvider.simple(ModBlocks.SCULK_LEAVES.get()),
                // Small, sparse canopy similar in spirit to acacia
                new AcaciaFoliagePlacer(ConstantInt.of(1), ConstantInt.of(0)),
                // Keeps canopy compact and low
                new TwoLayersFeatureSize(0, 0, 1)
        )
                .dirt(BlockStateProvider.simple(Blocks.SCULK))
                .ignoreVines()
                .build();
    }

    private static TreeConfiguration buildBaldachinTree() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.SCULK_LOG.get()), // Trunk block provider (all trunk/branch logs use SCULK_LOG)
                new CherryTrunkPlacer(
                        9,                 // baseHeight: minimum trunk height before random additions
                        3,                 // heightRandA: first random height add (0..1)
                        0,                 // heightRandB: second random height add (0..0, so no extra from this term)
                        UniformInt.of(1, 3), // branchCount: number of side branches generated (always 1 here)
                        UniformInt.of(2, 4), // branchHorizontalLength: horizontal branch reach (random 2..4)
                        UniformInt.of(-4, -3), // branchStartOffsetFromTop: where branch starts relative to top of trunk (higher negative = starts lower)
                        ConstantInt.of(-1) // branchEndOffsetFromTop: target branch end height offset from trunk top (always -1)
                ),
                BlockStateProvider.simple(ModBlocks.SCULK_LEAVES.get()), // Foliage block provider (all generated leaves use SCULK_LEAVES)
                new CherryFoliagePlacer(
                        ConstantInt.of(4), // radius: base canopy radius
                        ConstantInt.of(0), // offset: vertical shift of foliage attachment center
                        ConstantInt.of(5), // height: foliage stack height (number of foliage layers)
                        0.25F,             // wideBottomLayerHoleChance: chance to carve holes on wide bottom edge layer
                        0.5F,              // cornerHoleChance: chance to remove corner leaves (more ragged canopy outline)
                        0.16666667F,       // hangingLeavesChance: chance to place hanging leaves beneath canopy edge
                        0.33333334F        // hangingLeavesExtensionChance: chance hanging leaves extend one more block downward
                ),
                new TwoLayersFeatureSize(
                        1, // limit: upper size limit where trunk taper logic changes
                        0, // lowerSize: lower trunk thickness size parameter
                        2  // upperSize: upper trunk thickness size parameter
                )
        )
                .dirt(BlockStateProvider.simple(Blocks.SCULK)) // Replaces/uses this block as valid dirt base under generated trunk
                .decorators(List.of(
                        new AttachedToLeavesDecorator(
                                0.02F, // probability: chance per foliage position to attempt placing attached block
                                1,     // exclusionRadiusXZ: avoids clustering too close in X/Z
                                1,     // exclusionRadiusY: avoids clustering too close vertically
                                BlockStateProvider.simple(ModBlocks.GHOST_BLOOM.get()), // block provider for attached decorative block
                                2,     // requiredEmptyBlocks: air-space requirement from leaf before placement succeeds
                                List.of(Direction.DOWN) // allowed directions from leaves (DOWN = hangs under leaves)
                        )
                ))
                .ignoreVines() // Allows tree generation to ignore vine obstruction checks
                .build();
    }

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

    private static OreConfiguration buildNetheriteBlockOre() {
        BlockState netherite = Blocks.NETHERITE_BLOCK.defaultBlockState();

        OreConfiguration.TargetBlockState smoothBasaltTarget =
                OreConfiguration.target(new BlockMatchTest(Blocks.SMOOTH_BASALT), netherite);

        OreConfiguration.TargetBlockState deepslateTarget =
                OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), netherite);

        return new OreConfiguration(List.of(smoothBasaltTarget, deepslateTarget), 9, 0.0F);
    }

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

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
            BootstrapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key,
            F feature,
            FC configuration
    ) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}