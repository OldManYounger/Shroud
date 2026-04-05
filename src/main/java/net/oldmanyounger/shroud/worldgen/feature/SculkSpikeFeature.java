package net.oldmanyounger.shroud.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Generates a tapered sculk spike mound with a hollow crater and root supports.
 *
 * <p>This feature finds a valid sculk surface anchor, builds a layered volcanic
 * silhouette with noisy edge shaping, hollows the upper crater zone, and optionally
 * extends short downward root structures for grounded integration.
 *
 * <p>In the broader context of the project, this class is part of Shroud's terrain
 * feature generation layer that adds large corrupted landmarks to reinforce biome
 * identity and environmental variation.
 */
public class SculkSpikeFeature extends Feature<NoneFeatureConfiguration> {

    // Creates the feature with the provided no-config codec
    public SculkSpikeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    // Attempts to place one sculk spike structure at the configured origin
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        BlockPos origin = ctx.origin();
        RandomSource random = ctx.random();
        WorldGenLevel level = ctx.level();

        // Scans downward to find nearby non-air surface
        BlockPos basePos = origin;
        while (level.isEmptyBlock(basePos) && basePos.getY() > level.getMinBuildHeight() + 2) {
            basePos = basePos.below();
        }

        // Requires sculk surface at the anchor position
        if (!level.getBlockState(basePos).is(Blocks.SCULK)) {
            return false;
        }

        // Lifts base slightly for shape variation
        basePos = basePos.above(random.nextInt(3));

        // Chooses overall spike profile parameters
        int height = 10 + random.nextInt(6);
        int baseRadius = 6 + random.nextInt(4);
        int rimRadius = Mth.clamp(baseRadius / 2 + random.nextInt(2), 3, baseRadius - 1);

        // Chooses crater start in upper half and clamps for stability
        int craterStart = height / 2 + random.nextInt(height / 4 + 1);
        craterStart = Mth.clamp(craterStart, 2, height - 3);

        int craterRadius = Mth.clamp(rimRadius - 1, 1, rimRadius - 1);

        // Builds layered tapered body from bottom to top
        for (int y = 0; y < height; y++) {
            float layerT = (float) y / (float) (height - 1);

            float idealOuterRadius = Mth.lerp(layerT, (float) baseRadius, (float) rimRadius);

            // Applies slight per-layer jitter to reduce perfect symmetry
            float jitter = (random.nextFloat() - 0.5F) * 0.8F;
            float outerRadius = Mth.clamp(idealOuterRadius + jitter, 1.5F, (float) baseRadius + 1.0F);

            int outerIntRadius = Mth.ceil(outerRadius);
            float outerRadiusSq = outerRadius * outerRadius;

            boolean inCraterZone = (y >= craterStart);
            float craterRadiusF = inCraterZone ? (float) craterRadius : 0.0F;
            float craterRadiusSq = craterRadiusF * craterRadiusF;

            for (int dx = -outerIntRadius; dx <= outerIntRadius; dx++) {
                for (int dz = -outerIntRadius; dz <= outerIntRadius; dz++) {
                    float distSq = (float) (dx * dx + dz * dz);

                    // Keeps only blocks inside current outer radius
                    if (distSq > outerRadiusSq) {
                        continue;
                    }

                    // Skips inner crater region for upper layers
                    if (inCraterZone && distSq < craterRadiusSq) {
                        continue;
                    }

                    // Randomly thins very outer rim for rough silhouette
                    float dist = Mth.sqrt(distSq);
                    float rimBand = outerRadius - dist;
                    if (rimBand >= 0.0F && rimBand < 1.0F) {
                        if (random.nextFloat() < 0.20F) {
                            continue;
                        }
                    }

                    BlockPos targetPos = basePos.offset(dx, y, dz);
                    BlockState state = level.getBlockState(targetPos);

                    // Replaces allowed local materials with sculk
                    if (state.isAir()
                            || isDirt(state)
                            || state.is(Blocks.SCULK)
                            || state.is(Blocks.SCULK_VEIN)) {
                        setBlock(level, targetPos, Blocks.SCULK.defaultBlockState());
                    }
                }
            }
        }

        // Adds optional short downward root supports near the base
        int rootRadius = Mth.clamp(baseRadius / 3, 1, 3);
        for (int dx = -rootRadius; dx <= rootRadius; dx++) {
            for (int dz = -rootRadius; dz <= rootRadius; dz++) {
                float distSq = dx * dx + dz * dz;
                if (distSq > (rootRadius + 0.5F) * (rootRadius + 0.5F)) {
                    continue;
                }

                BlockPos downwardPos = basePos.offset(dx, -1, dz);
                int maxRootDepth = 4 + random.nextInt(4);

                for (int depth = 0; depth < maxRootDepth && downwardPos.getY() > level.getMinBuildHeight() + 4; depth++) {
                    BlockState state = level.getBlockState(downwardPos);
                    if (!state.isAir()
                            && !isDirt(state)
                            && !state.is(Blocks.SCULK)
                            && !state.is(Blocks.SCULK_VEIN)) {
                        break;
                    }

                    if (random.nextFloat() < 0.85F) {
                        setBlock(level, downwardPos, Blocks.SCULK.defaultBlockState());
                    }

                    downwardPos = downwardPos.below();
                }
            }
        }

        return true;
    }
}