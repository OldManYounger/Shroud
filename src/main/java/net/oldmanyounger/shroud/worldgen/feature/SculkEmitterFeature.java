package net.oldmanyounger.shroud.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Places a single upward-facing sculk emitter at a grounded surface position.
 *
 * <p>This feature scans downward from origin until a solid terrain position is found,
 * skips placement in fluid blocks, and writes a configured emitter block state.
 *
 * <p>In the broader context of the project, this class is part of Shroud's worldgen
 * feature layer that injects ambient reactive structures into corrupted terrain.
 */
public class SculkEmitterFeature extends Feature<NoneFeatureConfiguration> {

    // Creates the feature with the provided no-config codec
    public SculkEmitterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    // Attempts to place one sculk emitter at a valid terrain location
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos pos = ctx.origin();

        // Moves downward through air to find top terrain block
        while (level.isEmptyBlock(pos) && pos.getY() > level.getMinBuildHeight() + 1) {
            pos = pos.below();
        }

        if (pos.getY() <= level.getMinBuildHeight() + 1) {
            return false;
        }

        BlockState existing = level.getBlockState(pos);

        // Skips replacement when target block contains fluid
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