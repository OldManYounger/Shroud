package net.oldmanyounger.shroud.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Defines the Ghost Bloom, a hanging decorative plant used to support
 * Shroud's darker environmental storytelling and world atmosphere.
 *
 * <p>Unlike a standard flower or bush, this block is meant to feel inverted
 * and unnatural. It does not survive on the ground. Instead, it remains placed
 * only when suspended beneath leaf blocks, allowing it to function like eerie
 * foliage growth hanging from trees or overgrown canopy spaces.
 *
 * <p>In the broader context of the project, this class provides the placement and survival
 * logic needed for that behavior while still inheriting the lightweight plant
 * handling supplied by {@link BushBlock}. It also exposes a codec so Minecraft
 * can serialize and reconstruct the block correctly anywhere codec-backed block
 * definitions are used.
 */
public class ModGhostBloomBlock extends BushBlock {

    // Codec used by the game to serialize and recreate this custom bush block
    public static final MapCodec<ModGhostBloomBlock> CODEC = simpleCodec(ModGhostBloomBlock::new);

    // Creates a new Ghost Bloom block using the configured block properties
    public ModGhostBloomBlock(Properties properties) {
        super(properties);
    }

    // Returns the codec associated with this block type
    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    // Determines whether the Ghost Bloom can remain at its current location
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // The block must be hanging from leaves directly above it
        return level.getBlockState(pos.above()).is(BlockTags.LEAVES);
    }
}