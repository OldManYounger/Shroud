package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Defines a simple non-directional block that tracks whether another block of
 * the same type exists directly beneath it.
 *
 * <p>This lets the block expose a {@code stacked} boolean blockstate that can be
 * used by models, blockstates, or rendering logic to visually distinguish a
 * standalone block from one that is part of a vertical stack. The behavior is
 * intentionally lightweight and only concerns the block below, making it useful
 * for decorative piles, columns, or modular blocks that need a different look
 * when layered.
 *
 * <p>In the broader context of the project, this class provides reusable support
 * for stack-aware decorative blocks without requiring a custom block entity or
 * more complex neighbor scanning.
 */
public class ModStackingBlock extends Block {

    // Blockstate property indicating whether this block is sitting on top of another instance of itself
    public static final BooleanProperty STACKED = BooleanProperty.create("stacked");

    // Creates the block and initializes the default stacked state to false
    public ModStackingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(STACKED, false));
    }

    // Sets the initial blockstate when the block is placed into the world
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        // Mark the block as stacked if the block directly below is the same block type
        boolean stacked = level.getBlockState(pos.below()).is(this);
        return this.defaultBlockState().setValue(STACKED, stacked);
    }

    // Updates the stacked property when the supporting block below changes
    @Override
    public BlockState updateShape(BlockState state,
                                  Direction direction,
                                  BlockState neighborState,
                                  LevelAccessor level,
                                  BlockPos pos,
                                  BlockPos neighborPos) {
        // Only the block below affects this custom stacked state
        if (direction == Direction.DOWN) {
            boolean stacked = level.getBlockState(pos.below()).is(this);
            return state.setValue(STACKED, stacked);
        }

        return state;
    }

    // Registers the custom stacked blockstate property
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STACKED);
    }
}