package net.oldmanyounger.shroud.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.oldmanyounger.shroud.block.ModBlocks;

public class SculkEmitterFeature extends Feature<NoneFeatureConfiguration> {

    public SculkEmitterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos pos = ctx.origin();

        // Move downward through air until we find the top terrain block.
        while (level.isEmptyBlock(pos) && pos.getY() > level.getMinBuildHeight() + 1) {
            pos = pos.below();
        }

        if (pos.getY() <= level.getMinBuildHeight() + 1) {
            return false;
        }

        BlockState existing = level.getBlockState(pos);

        // NEW: do not replace liquids (water/lava/etc).
        if (!existing.getFluidState().isEmpty()) {
            return false;
        }

        BlockState emitterState = ModBlocks.SCULK_EMITTER.get()
                .defaultBlockState()
                .setValue(net.minecraft.world.level.block.DirectionalBlock.FACING, Direction.UP);

        setBlock(level, pos, emitterState);
        return true;
    }
}
