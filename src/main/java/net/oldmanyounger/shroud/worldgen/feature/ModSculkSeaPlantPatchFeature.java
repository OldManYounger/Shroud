package net.oldmanyounger.shroud.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Places Shroud underwater plants in loose seagrass-like patches.
 *
 * <p>This feature follows vanilla seagrass' random offset pattern around each
 * placement origin, but places multiple nearby custom plants so Shroud aquatic
 * vegetation appears in natural clusters instead of uniform single points.
 */
public class ModSculkSeaPlantPatchFeature extends Feature<BlockStateConfiguration> {

    // Number of placement attempts inside each patch center.
    private static final int PATCH_ATTEMPTS = 5;
    private static final float PLACEMENT_CHANCE = 0.45F;

    // Matches vanilla seagrass' random offset range.
    private static final int PATCH_RADIUS = 8;

    // Creates the patch feature with a block-state configuration codec.
    public ModSculkSeaPlantPatchFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    // Attempts to place a loose underwater plant patch around the sampled origin.
    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockState state = context.config().state;
        boolean placedAny = false;

        for (int i = 0; i < PATCH_ATTEMPTS; i++) {
            int xOffset = random.nextInt(PATCH_RADIUS) - random.nextInt(PATCH_RADIUS);
            int zOffset = random.nextInt(PATCH_RADIUS) - random.nextInt(PATCH_RADIUS);
            int x = origin.getX() + xOffset;
            int z = origin.getZ() + zOffset;
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);

            // Skips some valid positions so patches remain broken and natural.
            if (random.nextFloat() > PLACEMENT_CHANCE) {
                continue;
            }

            if (placePlant(level, new BlockPos(x, y, z), state)) {
                placedAny = true;
            }
        }

        return placedAny;
    }

    // Places either a single-block plant or a two-block-tall underwater plant.
    private boolean placePlant(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (!isSourceWater(level, pos) || !isValidSculkSubstrate(level.getBlockState(pos.below()))) {
            return false;
        }

        if (state.hasProperty(DoublePlantBlock.HALF)) {
            return placeTallPlant(level, pos, state);
        }

        if (!state.canSurvive(level, pos)) {
            return false;
        }

        level.setBlock(pos, state, 2);
        return true;
    }

    // Places lower and upper halves for tall underwater plants.
    private boolean placeTallPlant(WorldGenLevel level, BlockPos pos, BlockState state) {
        BlockPos above = pos.above();

        if (!isSourceWater(level, above)) {
            return false;
        }

        BlockState lower = state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);

        if (!lower.canSurvive(level, pos)) {
            return false;
        }

        level.setBlock(pos, lower, 2);
        level.setBlock(above, upper, 2);
        return true;
    }

    // Checks for full source water.
    private boolean isSourceWater(WorldGenLevel level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos).getAmount() == 8;
    }

    // Restricts patches to Shroud-compatible underwater surfaces.
    private boolean isValidSculkSubstrate(BlockState state) {
        return state.is(Blocks.SCULK)
                || state.is(ModBlocks.SCULK_GRAVEL.get())
                || state.is(ModBlocks.SCULK_GRASS.get());
    }
}