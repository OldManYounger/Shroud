package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ModGrassBlock extends GrassBlock {
    public ModGrassBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // If it can't survive, revert to sculk
        if (!canBeGrass(level, pos)) {
            level.setBlockAndUpdate(pos, Blocks.SCULK.defaultBlockState());
            return;
        }

        // Vanilla spread behavior
        super.randomTick(state, level, pos, random);
    }

    private boolean canBeGrass(LevelReader level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);

        // Dies if submerged under a full water source block
        if (aboveState.getFluidState().isSource()) {
            return false;
        }

        // Dies if covered by something that blocks light (i.e., a "block placed on it")
        // This also catches most solid blocks, slabs depending on shape, etc.
        return aboveState.getLightBlock(level, abovePos) == 0;
    }

}
