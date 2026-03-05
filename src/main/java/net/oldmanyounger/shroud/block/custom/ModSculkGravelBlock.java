package net.oldmanyounger.shroud.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ModSculkGravelBlock extends FallingBlock {
    public static final MapCodec<ModSculkGravelBlock> CODEC = simpleCodec(ModSculkGravelBlock::new);

    public ModSculkGravelBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    // Dust particle color shown while falling (pick any value you want)
    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return 0x2F3B46; // sculk-ish gray-blue
    }
}