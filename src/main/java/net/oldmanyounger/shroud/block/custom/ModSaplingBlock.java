package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

/**
 * Defines a custom sapling block that restricts survival and placement to a
 * configurable set of valid ground blocks.
 *
 * <p>This gives Shroud's tree saplings tighter environmental control than a
 * generic vanilla sapling by allowing each sapling type to specify exactly what
 * terrain or substrate it can grow on. That makes it easier to support custom
 * biomes, themed vegetation, and non-standard world rules without duplicating
 * sapling logic across multiple block classes.
 *
 * <p>In the broader context of the project, this class serves as reusable
 * infrastructure for custom tree ecosystems whose growth should be tied to the
 * mod's own terrain blocks rather than default overworld dirt and grass.
 */
public class ModSaplingBlock extends SaplingBlock {

    // Set of ground blocks this sapling is allowed to survive on
    private final List<Supplier<? extends Block>> validGroundBlocks;

    // Creates a sapling with a tree grower and a custom list of valid ground blocks
    @SafeVarargs
    public ModSaplingBlock(TreeGrower treeGrower,
                           Properties properties,
                           Supplier<? extends Block>... validGroundBlocks) {
        super(treeGrower, properties);

        // Store the allowed placement/survival substrates for later checks
        this.validGroundBlocks = List.of(validGroundBlocks);
    }

    // Restricts placement and survival to the configured valid ground blocks
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        Block ground = state.getBlock();

        // Check whether the supporting block matches any allowed substrate
        for (Supplier<? extends Block> supplier : validGroundBlocks) {
            if (supplier.get() == ground) {
                return true;
            }
        }

        // Reject placement on unsupported ground types
        return false;
    }
}