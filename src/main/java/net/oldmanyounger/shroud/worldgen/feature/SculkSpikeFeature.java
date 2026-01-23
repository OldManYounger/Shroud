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

public class SculkSpikeFeature extends Feature<NoneFeatureConfiguration> {

    public SculkSpikeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        BlockPos origin = ctx.origin();
        RandomSource random = ctx.random();
        WorldGenLevel level = ctx.level();

        // Find the surface
        BlockPos basePos = origin;
        while (level.isEmptyBlock(basePos) && basePos.getY() > level.getMinBuildHeight() + 2) {
            basePos = basePos.below();
        }

        // Only generate on a sculk surface
        if (!level.getBlockState(basePos).is(Blocks.SCULK)) {
            return false;
        }

        // Slight vertical offset for variety at the base
        basePos = basePos.above(random.nextInt(3)); // 0–2 blocks above the surface

        // Volcano shape parameters
        int height = 10 + random.nextInt(6);        // 10–15 blocks tall
        int baseRadius = 6 + random.nextInt(4);     // 6–9 block base radius
        int rimRadius = Mth.clamp(baseRadius / 2 + random.nextInt(2), 3, baseRadius - 1); // rim tighter than base

        // Crater starts somewhere in the upper half
        int craterStart = height / 2 + random.nextInt(height / 4 + 1); // between ~H/2 and ~3H/4
        craterStart = Mth.clamp(craterStart, 2, height - 3);

        int craterRadius = Mth.clamp(rimRadius - 1, 1, rimRadius - 1);

        // Build the volcano mound
        for (int y = 0; y < height; y++) {
            float layerT = (float) y / (float) (height - 1); // 0 at base, 1 at top

            // Outer radius tapers from baseRadius at y=0 to rimRadius at y=height-1
            float idealOuterRadius = Mth.lerp(layerT, (float) baseRadius, (float) rimRadius);

            // Slight per-layer noise to roughen the silhouette
            float jitter = (random.nextFloat() - 0.5F) * 0.8F; // -0.4 to +0.4
            float outerRadius = Mth.clamp(idealOuterRadius + jitter, 1.5F, (float) baseRadius + 1.0F);

            int outerIntRadius = Mth.ceil(outerRadius);
            float outerRadiusSq = outerRadius * outerRadius;

            // Crater: only in the upper part, with roughly constant radius
            boolean inCraterZone = (y >= craterStart);
            float craterRadiusF = inCraterZone ? (float) craterRadius : 0.0F;
            float craterRadiusSq = craterRadiusF * craterRadiusF;

            for (int dx = -outerIntRadius; dx <= outerIntRadius; dx++) {
                for (int dz = -outerIntRadius; dz <= outerIntRadius; dz++) {
                    float distSq = (float) (dx * dx + dz * dz);

                    // Must be inside outer radius
                    if (distSq > outerRadiusSq) {
                        continue;
                    }

                    // Inside crater region? Skip to make it hollow
                    if (inCraterZone && distSq < craterRadiusSq) {
                        continue;
                    }

                    // Edge roughness: thin out blocks near outer shell
                    // If this position is close to the outer rim, randomly remove some blocks
                    float dist = Mth.sqrt(distSq);
                    float rimBand = outerRadius - dist;
                    if (rimBand >= 0.0F && rimBand < 1.0F) {
                        // Within 1 block of the outer edge
                        if (random.nextFloat() < 0.20F) { // 20% chance to skip
                            continue;
                        }
                    }

                    BlockPos targetPos = basePos.offset(dx, y, dz);
                    BlockState state = level.getBlockState(targetPos);

                    // Replace air / dirt-like / sculk / sculk_vein
                    if (state.isAir()
                            || isDirt(state)
                            || state.is(Blocks.SCULK)
                            || state.is(Blocks.SCULK_VEIN)) {
                        setBlock(level, targetPos, Blocks.SCULK.defaultBlockState());
                    }
                }
            }
        }

        // Optionally: short sculk "roots" under the volcano to tie it into the ground
        int rootRadius = Mth.clamp(baseRadius / 3, 1, 3);
        for (int dx = -rootRadius; dx <= rootRadius; dx++) {
            for (int dz = -rootRadius; dz <= rootRadius; dz++) {
                float distSq = dx * dx + dz * dz;
                if (distSq > (rootRadius + 0.5F) * (rootRadius + 0.5F)) {
                    continue;
                }

                BlockPos downwardPos = basePos.offset(dx, -1, dz);
                int maxRootDepth = 4 + random.nextInt(4); // roots 4–7 blocks deep

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
