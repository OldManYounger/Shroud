package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

/** Custom sapling block enforcing survival only on specific block types */
public class ModSaplingBlock extends SaplingBlock {

    /** Blocks that this sapling may survive on */
    private final List<Supplier<? extends Block>> validGroundBlocks;

    @SafeVarargs
    public ModSaplingBlock(TreeGrower treeGrower,
                           Properties properties,
                           Supplier<? extends Block>... validGroundBlocks) {
        super(treeGrower, properties);
        this.validGroundBlocks = List.of(validGroundBlocks);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        Block ground = state.getBlock();
        for (Supplier<? extends Block> supplier : validGroundBlocks) {
            if (supplier.get() == ground) {
                return true;
            }
        }
        return false;
    }
}
