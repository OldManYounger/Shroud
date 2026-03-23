package net.oldmanyounger.shroud.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Defines a small plant-style sculk flora block that can only exist on sculk
 * terrain.
 *
 * <p>This block behaves like a simple decorative bush or ground plant, but its
 * placement rules are intentionally limited so it only appears on thematically
 * appropriate substrates. Rather than allowing normal farmland or dirt-style
 * placement, it survives only on vanilla {@code SCULK} and the mod's custom
 * {@code SCULK_GRASS}, helping it read as part of a corrupted biome palette.
 *
 * <p>In the broader context of the project, this class supports atmospheric
 * world-building by making surface flora respond to Shroud's custom terrain
 * systems instead of vanilla biome assumptions.
 */
public class ModSculkBulbBlock extends BushBlock {

    // Codec used by Minecraft to serialize and recreate this custom bush block
    public static final MapCodec<ModSculkBulbBlock> CODEC =
            simpleCodec(ModSculkBulbBlock::new);

    // Creates a new sculk bulb block with the supplied properties
    public ModSculkBulbBlock(Properties properties) {
        super(properties);
    }

    // Returns the codec associated with this custom bush block type
    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    // Restricts placement and survival to sculk-based ground blocks
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // Allow this plant to remain only on vanilla sculk or the mod's sculk grass
        return state.is(Blocks.SCULK) || state.is(ModBlocks.SCULK_GRASS.get());
    }
}