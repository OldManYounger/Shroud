package net.oldmanyounger.shroud.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public class GhostBloomBlock extends BushBlock {

    public static final MapCodec<GhostBloomBlock> CODEC = simpleCodec(GhostBloomBlock::new);

    public GhostBloomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Must hang from leaves above (underside attachment)
        return level.getBlockState(pos.above()).is(BlockTags.LEAVES);
    }
}