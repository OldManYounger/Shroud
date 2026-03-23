package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Defines a custom leaves block for Shroud with vanilla-like fire behavior.
 *
 * <p>In the broader context of the project, this class acts as the shared implementation
 * for custom tree canopies and leaf-based foliage that should behave naturally
 * in the world. Rather than leaving flammability unspecified, it explicitly
 * provides fire interaction values so Shroud leaves burn and spread fire in a
 * way consistent with player expectations for organic leaf blocks.
 *
 * <p>By extending {@link LeavesBlock}, the class keeps standard decay and leaf
 * handling while only customizing the fire-related hooks needed by the mod's
 * custom tree sets.
 */
public class ModLeavesBlock extends LeavesBlock {

    // Creates a new custom leaves block using standard leaf-style properties
    public ModLeavesBlock(Properties properties) {
        super(properties);
    }

    // Marks these leaves as flammable from any side
    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    // Returns the ignition chance used when fire checks this block
    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }

    // Returns the rate at which fire spreads across these leaves
    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 30;
    }
}