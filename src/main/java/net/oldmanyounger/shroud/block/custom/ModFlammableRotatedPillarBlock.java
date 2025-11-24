package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import net.oldmanyounger.shroud.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

/** Custom rotated pillar block providing flammability and axe-stripping behavior for logs and wood */
public class ModFlammableRotatedPillarBlock extends RotatedPillarBlock {

    /** Creates a flammable rotated pillar block using the provided properties */
    public ModFlammableRotatedPillarBlock(Properties properties) {
        super(properties);
    }

    /** Marks all Sculk pillar variants as flammable */
    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    /** Defines the flammability rate for pillar blocks */
    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    /** Defines the fire spread speed for pillar blocks */
    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    /** Handles axe-stripping interactions for Sculk logs and wood */
    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (context.getItemInHand().getItem() instanceof AxeItem) {

            // Converts Sculk log to its stripped variant
            if (state.is(ModBlocks.SCULK_LOG)) {
                return ModBlocks.STRIPPED_SCULK_LOG.get()
                        .defaultBlockState()
                        .setValue(AXIS, state.getValue(AXIS));
            }

            // Converts Sculk wood to its stripped variant
            if (state.is(ModBlocks.SCULK_WOOD)) {
                return ModBlocks.STRIPPED_SCULK_WOOD.get()
                        .defaultBlockState()
                        .setValue(AXIS, state.getValue(AXIS));
            }
        }

        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }
}
