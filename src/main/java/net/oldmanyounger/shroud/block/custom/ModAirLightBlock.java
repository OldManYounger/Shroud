package net.oldmanyounger.shroud.block.custom;

import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public class ModAirLightBlock extends AirBlock {

    private final int lightLevel;

    public ModAirLightBlock(int lightLevel) {
        super(BlockBehaviour.Properties.of()
                .noCollission()
                .noOcclusion()
                .replaceable()
                .strength(0.0F) // irrelevant; it should be “air-like”
        );
        this.lightLevel = Math.max(0, Math.min(15, lightLevel));
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return this.lightLevel;
    }
}