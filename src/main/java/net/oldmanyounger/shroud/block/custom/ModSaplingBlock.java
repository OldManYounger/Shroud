package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/** Custom sapling block enforcing survival only on a specific block type */
public class ModSaplingBlock extends SaplingBlock {

    /** Holds the block that this sapling must be placed on to survive */
    private final Supplier<Block> blockToSurviveOn;

    /** Creates a sapling block with a restricted survival block */
    public ModSaplingBlock(TreeGrower treeGrower, Properties properties, Supplier<Block> block) {
        super(treeGrower, properties);
        this.blockToSurviveOn = block;
    }

    /** Ensures the sapling only survives on the designated block */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return blockToSurviveOn.get() == state.getBlock();
    }
}
