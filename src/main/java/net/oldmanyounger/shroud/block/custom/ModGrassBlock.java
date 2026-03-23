package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

/**
 * Defines Shroud's custom grass-like spreading block behavior for sculk terrain.
 *
 * <p>This class exists to make a custom surface block behave similarly to vanilla
 * grass while fitting the mod's biome and terrain identity. Instead of interacting
 * with dirt and grass like vanilla logic does, it converts between {@code SCULK}
 * and this custom grass state, allowing sculk-covered terrain to decay or spread
 * naturally based on lighting and environmental conditions.
 *
 * <p>In the broader context of the project, this helps Shroud's world generation
 * feel more alive by giving corrupted or altered terrain a self-maintaining
 * surface layer. The implementation mirrors vanilla's spreading snowy dirt logic
 * closely so that behavior remains familiar and stable, while swapping in the
 * mod's intended block palette.
 */
public class ModGrassBlock extends GrassBlock {

    // Creates a new sculk-grass-style block using the supplied properties
    public ModGrassBlock(Properties properties) {
        super(properties);
    }

    // Runs the random tick logic that lets the block either decay back into sculk or spread to nearby sculk blocks
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Mirror vanilla SpreadingSnowyDirtBlock behavior, but use SCULK and this custom grass block instead
        if (!canBeGrass(state, level, pos)) {
            // Avoid forcing chunk loads while checking or updating nearby terrain
            if (!level.isAreaLoaded(pos, 1)) return;

            // Revert this block back into plain sculk when it can no longer survive as grass
            level.setBlockAndUpdate(pos, Blocks.SCULK.defaultBlockState());
        } else {
            // Avoid spreading into positions that would require unloaded chunks
            if (!level.isAreaLoaded(pos, 3)) return;

            // Only attempt spreading when there is enough local brightness above the block
            if (level.getMaxLocalRawBrightness(pos.above()) >= 8) {
                BlockState blockstate = this.defaultBlockState();

                // Try several random nearby positions for possible spread
                for (int i = 0; i < 4; i++) {
                    BlockPos blockpos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);

                    // Spread onto neighboring sculk blocks that satisfy the same survival conditions
                    if (level.getBlockState(blockpos).is(Blocks.SCULK) && canPropagate(blockstate, level, blockpos)) {
                        level.setBlockAndUpdate(
                                blockpos,
                                blockstate.setValue(SNOWY, level.getBlockState(blockpos.above()).is(Blocks.SNOW))
                        );
                    }
                }
            }
        }
    }

    // Checks whether this block is allowed to remain in its grass-like form at the current position
    private static boolean canBeGrass(BlockState state, LevelReader levelReader, BlockPos pos) {
        BlockPos blockpos = pos.above();
        BlockState blockstate = levelReader.getBlockState(blockpos);

        // A single snow layer still allows the block to survive, matching vanilla grass behavior
        if (blockstate.is(Blocks.SNOW) && blockstate.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        } else if (blockstate.getFluidState().getAmount() == 8) {
            // Full fluid coverage prevents the block from behaving like grass
            return false;
        } else {
            // Use vanilla-style light blocking checks to determine if the surface can stay alive
            int i = LightEngine.getLightBlockInto(
                    levelReader, state, pos, blockstate, blockpos, Direction.UP, blockstate.getLightBlock(levelReader, blockpos)
            );

            return i < levelReader.getMaxLightLevel();
        }
    }

    // Checks whether the block can spread into a nearby position
    private static boolean canPropagate(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos blockpos = pos.above();

        // The target must satisfy the same grass survival rules and not be waterlogged above
        return canBeGrass(state, level, pos) && !level.getFluidState(blockpos).is(FluidTags.WATER);
    }
}