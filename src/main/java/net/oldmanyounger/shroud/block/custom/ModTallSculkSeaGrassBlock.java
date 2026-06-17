package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Tall sculk sea grass block with vanilla tall seagrass behavior.
 *
 * <p>This block keeps the two-block underwater placement and survival behavior
 * from vanilla tall seagrass while returning Shroud's own tall sea grass item
 * when cloned in creative mode.
 */
public class ModTallSculkSeaGrassBlock extends TallSeagrassBlock {

    // Creates tall sculk sea grass with vanilla tall seagrass properties
    public ModTallSculkSeaGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // Returns the custom tall sculk sea grass item when picked in creative mode
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModBlocks.TALL_SCULK_SEA_GRASS.get());
    }
}