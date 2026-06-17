package net.oldmanyounger.shroud.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

/**
 * Places a two-block-tall underwater plant.
 *
 * <p>This feature exists because the vanilla simple block feature expects the
 * space above a double plant to be empty, while underwater tall plants need that
 * space to contain source water.
 */
public class ModTallWaterPlantFeature extends Feature<BlockStateConfiguration> {

    // Creates the feature with a block-state configuration codec
    public ModTallWaterPlantFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    // Attempts to place lower and upper plant halves in source water
    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockPos above = origin.above();

        if (!isSourceWater(level, origin) || !isSourceWater(level, above)) {
            return false;
        }

        BlockState lower = context.config().state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = context.config().state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);

        if (!lower.canSurvive(level, origin)) {
            return false;
        }

        level.setBlock(origin, lower, 2);
        level.setBlock(above, upper, 2);
        return true;
    }

    // Checks for a full source-water block
    private boolean isSourceWater(WorldGenLevel level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos).getAmount() == 8;
    }
}