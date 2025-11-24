package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Custom leaves block supplying flammability behavior for leaves */
public class ModLeavesBlock extends LeavesBlock {

    /** Creates a leaves block using standard leaf properties */
    public ModLeavesBlock(Properties properties) {
        super(properties);
    }

    /** Marks leaves as flammable */
    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    /** Defines flammability rate for leaves */
    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }

    /** Defines fire spread speed for leaves */
    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 30;
    }
}
