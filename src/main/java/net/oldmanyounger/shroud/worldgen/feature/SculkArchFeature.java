package net.oldmanyounger.shroud.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Generates a large sculk arch between two ground-anchored endpoints.
 * Both ends are sunk slightly into the ground to ensure a natural footing.
 *
 * Includes safety checks so very large arches don't get clipped/sheared
 * at generation write-region boundaries.
 */
public class SculkArchFeature extends Feature<NoneFeatureConfiguration> {

    // Keep arches inside a safer feature-write radius to avoid clipped ends.
    // Tune upward carefully if you still want larger arches.
    private static final int SAFE_MAX_HALF_SPAN = 24;

    public SculkArchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        BlockPos origin = ctx.origin();
        RandomSource random = ctx.random();
        WorldGenLevel level = ctx.level();

        // Find a nearby surface by scanning down
        BlockPos basePos = origin;
        while (level.isEmptyBlock(basePos) && basePos.getY() > level.getMinBuildHeight() + 2) {
            basePos = basePos.below();
        }

        // Only generate on sculk-like surface (vanilla sculk OR sculk grass)
        if (!isSculkSurface(level.getBlockState(basePos))) {
            return false;
        }

        // Start a little above the surface so the curve can rise cleanly
        basePos = basePos.above(random.nextInt(3)); // 0–2 above surface

        // Pick a random horizontal direction for the arch
        float angle = random.nextFloat() * (float) (Math.PI * 2.0);
        float dirX = Mth.cos(angle);
        float dirZ = Mth.sin(angle);

        // Size parameters
        final int minHalfSpan = 10;
        final int maxHalfSpan = 22;

        final int minHeight = 8;
        final int maxHeight = 18;

        int halfSpan = Mth.nextInt(random, minHalfSpan, maxHalfSpan);
        halfSpan = Math.min(halfSpan, SAFE_MAX_HALF_SPAN);

        float sizeT = (maxHalfSpan == minHalfSpan)
                ? 0.0F
                : (halfSpan - (float) minHalfSpan) / (float) (maxHalfSpan - minHalfSpan);

        int archHeight = Mth.floor(Mth.lerp(sizeT, minHeight, maxHeight));
        archHeight += random.nextInt(3) - 1; // -1..+1 small variation
        archHeight = Mth.clamp(archHeight, minHeight, maxHeight);

        // Thickness scales with size
        int thickness = 1 + Mth.floor(sizeT * 2.0F);
        thickness = Mth.clamp(thickness, 1, 4);

        // Compute endpoints in X/Z around the basePos
        BlockPos endA = basePos.offset(Mth.floor(-dirX * halfSpan), 0, Mth.floor(-dirZ * halfSpan));
        BlockPos endB = basePos.offset(Mth.floor(dirX * halfSpan), 0, Mth.floor(dirZ * halfSpan));

        // Snap endpoints to ground
        BlockPos groundA = findGround(level, endA.above(12), 48);
        BlockPos groundB = findGround(level, endB.above(12), 48);

        if (groundA == null || groundB == null) {
            return false;
        }

        // Require sculk-like surface at both footings
        if (!isSculkSurface(level.getBlockState(groundA)) || !isSculkSurface(level.getBlockState(groundB))) {
            return false;
        }

        // Sink both ends into ground by 0–3 blocks
        int sinkA = random.nextInt(4);
        int sinkB = random.nextInt(4);

        BlockPos footA = groundA.below(sinkA);
        BlockPos footB = groundB.below(sinkB);

        // Extra guard: ensure feet are still within a safer horizontal radius
        if (!isWithinHorizontalRadius(origin, footA, SAFE_MAX_HALF_SPAN + 8)
                || !isWithinHorizontalRadius(origin, footB, SAFE_MAX_HALF_SPAN + 8)) {
            return false;
        }

        // Steps scale with span so larger arches stay smooth
        int span = halfSpan * 2;
        int steps = Mth.clamp(span * 3, 50, 180);

        // Preflight write-region check: abort if any sampled arch region is not writable
        if (!canWriteEntireArch(level, footA, footB, archHeight, thickness, steps)) {
            return false;
        }

        // Tie feet into ground first
        stampFoot(level, random, footA, thickness + 1);
        stampFoot(level, random, footB, thickness + 1);

        for (int i = 0; i <= steps; i++) {
            float t = (float) i / (float) steps;

            // Linear interpolation in X/Z, baseline Y interpolates between endpoints
            float x = Mth.lerp(t, footA.getX(), footB.getX());
            float z = Mth.lerp(t, footA.getZ(), footB.getZ());
            float baseY = Mth.lerp(t, footA.getY(), footB.getY());

            // Arch lift: sin(pi*t) peaks in the middle and is 0 at ends
            float lift = Mth.sin((float) Math.PI * t) * archHeight;

            int px = Mth.floor(x);
            int pz = Mth.floor(z);
            int py = Mth.floor(baseY + lift);

            BlockPos center = new BlockPos(px, py, pz);

            // Place a thickened tube around this center
            placeBlob(level, random, center, thickness);
        }

        return true;
    }

    private static boolean isSculkSurface(BlockState state) {
        return state.is(Blocks.SCULK) || state.is(ModBlocks.SCULK_GRASS.get());
    }

    /**
     * Finds the first non-air block at or below start by scanning down.
     * Returns that block position (the ground block), or null if not found.
     */
    private static BlockPos findGround(WorldGenLevel level, BlockPos start, int maxDown) {
        BlockPos pos = start;
        int minY = level.getMinBuildHeight() + 2;

        for (int i = 0; i < maxDown && pos.getY() > minY; i++) {
            if (!level.isEmptyBlock(pos)) {
                return pos;
            }
            pos = pos.below();
        }
        return null;
    }

    private static boolean isWithinHorizontalRadius(BlockPos origin, BlockPos p, int radius) {
        return Math.abs(p.getX() - origin.getX()) <= radius
                && Math.abs(p.getZ() - origin.getZ()) <= radius;
    }

    /**
     * Preflight check to avoid clipped/sheared arches at generation boundaries.
     * Samples centerline + shell probe points.
     */
    private static boolean canWriteEntireArch(
            WorldGenLevel level,
            BlockPos footA,
            BlockPos footB,
            int archHeight,
            int thickness,
            int steps
    ) {
        int r = Mth.clamp(thickness, 1, 6);

        for (int i = 0; i <= steps; i++) {
            float t = (float) i / (float) steps;

            float x = Mth.lerp(t, footA.getX(), footB.getX());
            float z = Mth.lerp(t, footA.getZ(), footB.getZ());
            float baseY = Mth.lerp(t, footA.getY(), footB.getY());
            float lift = Mth.sin((float) Math.PI * t) * archHeight;

            BlockPos c = new BlockPos(Mth.floor(x), Mth.floor(baseY + lift), Mth.floor(z));

            if (!level.ensureCanWrite(c)) return false;

            BlockPos[] probes = new BlockPos[]{
                    c.offset( r, 0, 0), c.offset(-r, 0, 0),
                    c.offset(0,  r, 0), c.offset(0, -r, 0),
                    c.offset(0, 0,  r), c.offset(0, 0, -r),
                    c.offset( r, r, 0), c.offset(-r, r, 0),
                    c.offset( r,-r, 0), c.offset(-r,-r, 0),
                    c.offset(0, r,  r), c.offset(0, r, -r),
                    c.offset(0,-r,  r), c.offset(0,-r, -r)
            };

            for (BlockPos p : probes) {
                if (!level.ensureCanWrite(p)) return false;
            }
        }

        return true;
    }

    /**
     * Creates a slightly larger footing at the ends and a short downward column to anchor it.
     */
    private void stampFoot(WorldGenLevel level, RandomSource random, BlockPos foot, int radius) {
        // Small cap at the foot position
        placeBlob(level, random, foot, radius);

        // A short root column downward to ensure it starts at/below ground
        BlockPos p = foot.below();
        int depth = 3 + random.nextInt(4); // 3–6
        for (int i = 0; i < depth && p.getY() > level.getMinBuildHeight() + 4; i++) {
            BlockState state = level.getBlockState(p);
            if (!state.isAir() && !isDirt(state) && !isSculkFillable(state) && !state.is(Blocks.SCULK_VEIN)) {
                break;
            }
            setBlock(level, p, Blocks.SCULK.defaultBlockState());
            p = p.below();
        }
    }

    private static boolean isSculkFillable(BlockState state) {
        return state.is(Blocks.SCULK) || state.is(ModBlocks.SCULK_GRASS.get());
    }

    /**
     * Places a roughly spherical blob of sculk, replacing air/dirt-like/sculk grass/sculk/veins.
     */
    private void placeBlob(WorldGenLevel level, RandomSource random, BlockPos center, int radius) {
        int r = Mth.clamp(radius, 1, 4);
        float rSq = (r + 0.25F) * (r + 0.25F);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    float distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > rSq) continue;

                    // Light roughness so it doesn't look perfectly smooth
                    if (distSq > (rSq - 1.25F) && random.nextFloat() < 0.15F) continue;

                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(p);

                    if (state.isAir()
                            || isDirt(state)
                            || isSculkFillable(state)
                            || state.is(Blocks.SCULK_VEIN)) {
                        setBlock(level, p, Blocks.SCULK.defaultBlockState());
                    }
                }
            }
        }
    }
}