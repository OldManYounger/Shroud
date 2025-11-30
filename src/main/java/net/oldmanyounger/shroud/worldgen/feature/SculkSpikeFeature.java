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
        BlockPos blockPos = ctx.origin();
        RandomSource random = ctx.random();
        WorldGenLevel level = ctx.level();

        // Walk downward until we hit the terrain surface
        while (level.isEmptyBlock(blockPos) && blockPos.getY() > level.getMinBuildHeight() + 2) {
            blockPos = blockPos.below();
        }

        // Require a sculk surface so this only generates in sculk-covered biomes
        if (!level.getBlockState(blockPos).is(Blocks.SCULK)) {
            return false;
        }

        blockPos = blockPos.above(random.nextInt(4));
        int height = random.nextInt(4) + 7;
        int radius = height / 4 + random.nextInt(2);
        if (radius > 1 && random.nextInt(60) == 0) {
            blockPos = blockPos.above(10 + random.nextInt(30));
        }

        for (int y = 0; y < height; y++) {
            float fr = (1.0F - (float) y / (float) height) * (float) radius;
            int r = Mth.ceil(fr);

            for (int dx = -r; dx <= r; dx++) {
                float fx = (float) Mth.abs(dx) - 0.25F;

                for (int dz = -r; dz <= r; dz++) {
                    float fz = (float) Mth.abs(dz) - 0.25F;
                    if ((dx == 0 && dz == 0 || !(fx * fx + fz * fz > fr * fr))
                            && (dx != -r && dx != r && dz != -r && dz != r || !(random.nextFloat() > 0.75F))) {

                        BlockPos targetPos = blockPos.offset(dx, y, dz);
                        BlockState state = level.getBlockState(targetPos);
                        // Allow replacing air, dirt-like blocks, sculk, and sculk veins
                        if (state.isAir()
                                || isDirt(state)
                                || state.is(Blocks.SCULK)
                                || state.is(Blocks.SCULK_VEIN)) {
                            setBlock(level, targetPos, Blocks.SCULK.defaultBlockState());
                        }

                        if (y != 0 && r > 1) {
                            targetPos = blockPos.offset(dx, -y, dz);
                            state = level.getBlockState(targetPos);
                            if (state.isAir()
                                    || isDirt(state)
                                    || state.is(Blocks.SCULK)
                                    || state.is(Blocks.SCULK_VEIN)) {
                                setBlock(level, targetPos, Blocks.SCULK.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }

        int baseRadius = radius - 1;
        if (baseRadius < 0) {
            baseRadius = 0;
        } else if (baseRadius > 1) {
            baseRadius = 1;
        }

        // Generate the downward "root" of the spike
        for (int dx = -baseRadius; dx <= baseRadius; dx++) {
            for (int dz = -baseRadius; dz <= baseRadius; dz++) {
                BlockPos downwardPos = blockPos.offset(dx, -1, dz);
                int run = 50;
                if (Math.abs(dx) == 1 && Math.abs(dz) == 1) {
                    run = random.nextInt(5);
                }

                while (downwardPos.getY() > 50) {
                    BlockState state = level.getBlockState(downwardPos);
                    // Stop when hitting something solid that is not dirt/sculk/vein
                    if (!state.isAir()
                            && !isDirt(state)
                            && !state.is(Blocks.SCULK)
                            && !state.is(Blocks.SCULK_VEIN)) {
                        break;
                    }

                    setBlock(level, downwardPos, Blocks.SCULK.defaultBlockState());
                    downwardPos = downwardPos.below();
                    if (--run <= 0) {
                        downwardPos = downwardPos.below(random.nextInt(5) + 1);
                        run = random.nextInt(5);
                    }
                }
            }
        }

        return true;
    }
}
