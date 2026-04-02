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

/**
 * Defines a flammable rotated pillar block for Shroud wood-family blocks such
 * as logs and wood variants.
 *
 * <p>In the broader context of the project, this class provides shared behavior for pillar-like
 * natural materials that should act like vanilla logs: they can burn, spread
 * fire, and be transformed into stripped variants when used with an axe. This
 * keeps the block registration layer simpler by centralizing that reusable
 * behavior in one place rather than re-implementing it for each custom log type.
 *
 * <p>The class extends {@link RotatedPillarBlock} so it retains vanilla axis
 * rotation behavior, then adds two important pieces of project-specific logic:
 * flammability values for fire interaction, and axe-stripping rules that map
 * Shroud logs and woods to their corresponding stripped blocks while preserving
 * the pillar axis.
 */
public class ModFlammableRotatedPillarBlock extends RotatedPillarBlock {

    // Creates a new flammable pillar block using the supplied block properties
    public ModFlammableRotatedPillarBlock(Properties properties) {
        super(properties);
    }

    // Marks this custom pillar block as flammable from any side
    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    // Returns the block's flammability value used when fire checks ignition chance
    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    // Returns the speed at which fire can spread across this block
    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    // Handles axe interactions that convert custom logs and wood blocks into stripped variants
    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        // Only apply custom stripping behavior when the held tool is an axe
        if (context.getItemInHand().getItem() instanceof AxeItem) {

            // Convert the Sculk log into its stripped version while preserving axis rotation
            if (state.is(ModBlocks.VIRELITH_LOG)) {
                return ModBlocks.STRIPPED_VIRELITH_LOG.get()
                        .defaultBlockState()
                        .setValue(AXIS, state.getValue(AXIS));
            }

            if (state.is(ModBlocks.VIRELITH_WOOD)) {
                return ModBlocks.STRIPPED_VIRELITH_WOOD.get()
                        .defaultBlockState()
                        .setValue(AXIS, state.getValue(AXIS));
            }

            // Convert the Umber log into its stripped version while preserving axis rotation
            if (state.is(ModBlocks.UMBER_LOG)) {
                return ModBlocks.STRIPPED_UMBER_LOG.get()
                        .defaultBlockState()
                        .setValue(AXIS, state.getValue(AXIS));
            }

            // Convert the Umber wood into its stripped version while preserving axis rotation
            if (state.is(ModBlocks.UMBER_WOOD)) {
                return ModBlocks.STRIPPED_UMBER_WOOD.get()
                        .defaultBlockState()
                        .setValue(AXIS, state.getValue(AXIS));
            }
        }

        // Fall back to vanilla or inherited tool modification handling when no custom match is found
        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }
}