package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Custom sugar cane variant used for gloamcane placement rules.
 *
 * <p>This block preserves vanilla sugar cane behavior while extending valid
 * base blocks to include Shroud terrain blocks such as sculk gravel and sculk grass.
 *
 * <p>In the broader context of the project, this class allows biome-themed flora
 * to integrate naturally with Shroud-specific surface blocks without changing
 * vanilla sugar cane growth and survival expectations.
 */
public class ModGloamcaneBlock extends SugarCaneBlock {

    // Creates the custom gloamcane block with sugar cane-like properties
    public ModGloamcaneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // Extends sugar cane survival so gloamcane can root on Shroud sculk surfaces
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (super.canSurvive(state, level, pos)) {
            return true;
        }

        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        if (belowState.is(this)) {
            return true;
        }

        boolean validCustomGround =
                belowState.is(Blocks.SCULK)
                        || belowState.is(ModBlocks.SCULK_GRASS.get())
                        || belowState.is(ModBlocks.SCULK_GRAVEL.get());

        if (!validCustomGround) {
            return false;
        }

        // Requires adjacent water at base level like vanilla sugar cane
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = belowPos.relative(direction);
            if (level.getFluidState(adjacentPos).is(FluidTags.WATER)) {
                return true;
            }
        }

        return false;
    }
}