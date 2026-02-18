package net.oldmanyounger.shroud.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Plant-style block similar to vanilla short grass, but restricted to
 * placement/survival on Sculk and the mod's Sculk Grass.
 *
 * Light level is configured via block Properties in registration (ModBlocks).
 */
public class ModSculkBulbBlock extends BushBlock {

    public static final MapCodec<ModSculkBulbBlock> CODEC =
            simpleCodec(ModSculkBulbBlock::new);

    public ModSculkBulbBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SCULK) || state.is(ModBlocks.SCULK_GRASS.get());
    }
}
