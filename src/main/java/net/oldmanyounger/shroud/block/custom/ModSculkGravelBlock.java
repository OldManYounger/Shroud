package net.oldmanyounger.shroud.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Defines a custom falling gravel-like block themed around sculk terrain.
 *
 * <p>By extending {@link FallingBlock}, this block inherits vanilla sand/gravel
 * behavior so it naturally drops when unsupported. That makes it suitable for
 * cave ceilings, unstable terrain pockets, or decorative deposits that should
 * feel physically loose in the world.
 *
 * <p>In the broader context of the project, this class helps to expand the
 * sculk material set beyond static stone-like blocks by introducing a granular
 * terrain variant that fits the same biome and environmental palette while still
 * using familiar Minecraft block behavior.
 */
public class ModSculkGravelBlock extends FallingBlock {

    // Codec used by the game to serialize and recreate this custom falling block
    public static final MapCodec<ModSculkGravelBlock> CODEC = simpleCodec(ModSculkGravelBlock::new);

    // Creates a new sculk gravel block with the supplied block properties
    public ModSculkGravelBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // Returns the codec associated with this custom falling block type
    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    // Supplies the dust particle color shown while the block is falling
    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        // Use a muted sculk-like gray-blue particle tint
        return 0x2F3B46;
    }
}