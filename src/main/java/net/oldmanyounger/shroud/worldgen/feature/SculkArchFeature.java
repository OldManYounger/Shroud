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

/**
 * Generates a large sculk arch between two ground-anchored endpoints
 * Both ends are sunk slightly into the ground to ensure a natural footing
 */
public class SculkArchFeature extends Feature<NoneFeatureConfiguration> {

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

        // Only generate on sculk surface
        if (!level.getBlockState(basePos).is(Blocks.SCULK)) {
            return false;
        }

        // Start a little above the surface so the curve can rise cleanly
        basePos = basePos.above(random.nextInt(3)); // 0–2 above surface

        // Pick a random horizontal direction for the arch
        float angle = random.nextFloat() * (float) (Math.PI * 2.0);
        float dirX = Mth.cos(angle);
        float dirZ = Mth.sin(angle);

        // Size parameters (adjust these ranges as desired)
        final int minHalfSpan = 10; // smallest half-span
        final int maxHalfSpan = 22; // largest half-span

        final int minHeight = 8;    // smallest peak lift
        final int maxHeight = 18;   // largest peak lift

        // Roll a half-span in range, then compute a normalized size factor in [0, 1]
        int halfSpan = Mth.nextInt(random, minHalfSpan, maxHalfSpan);
        float sizeT = (maxHalfSpan == minHalfSpan)
                ? 0.0F
                : (halfSpan - (float) minHalfSpan) / (float) (maxHalfSpan - minHalfSpan);

        // Height can be independent, but tying it loosely to size usually looks better
        // This interpolates within [minHeight, maxHeight], with a little jitter
        int archHeight = Mth.floor(Mth.lerp(sizeT, minHeight, maxHeight));
        archHeight += random.nextInt(3) - 1; // -1..+1 small variation
        archHeight = Mth.clamp(archHeight, minHeight, maxHeight);

        // Thickness scales with size
        // sizeT=0 -> 1, sizeT=1 -> 3 (clamped to 1..4)
        int thickness = 1 + Mth.floor(sizeT * 2.0F);
        thickness = Mth.clamp(thickness, 1, 4);

        // Compute endpoints in X/Z around the basePos
        BlockPos endA = basePos.offset(Mth.floor(-dirX * halfSpan), 0, Mth.floor(-dirZ * halfSpan));
        BlockPos endB = basePos.offset(Mth.floor(dirX * halfSpan), 0, Mth.floor(dirZ * halfSpan));

        // Snap endpoints to ground (scan downward from a bit above)
        BlockPos groundA = findGround(level, endA.above(12), 48);
        BlockPos groundB = findGround(level, endB.above(12), 48);

        if (groundA == null || groundB == null) {
            return false;
        }

        // Require sculk at both footings
        if (!level.getBlockState(groundA).is(Blocks.SCULK) || !level.getBlockState(groundB).is(Blocks.SCULK)) {
            return false;
        }

        // Sink both ends into ground by 0–3 blocks
        int sinkA = random.nextInt(4);
        int sinkB = random.nextInt(4);

        BlockPos footA = groundA.below(sinkA);
        BlockPos footB = groundB.below(sinkB);

        // Tie the feet into the ground a bit more so they don't look pasted on
        stampFoot(level, random, footA, thickness + 1);
        stampFoot(level, random, footB, thickness + 1);

        // Steps scale with span so larger arches stay smooth
        int span = halfSpan * 2;
        int steps = Mth.clamp(span * 3, 50, 180);

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

    /**
     * Finds the first non-air block at or below start by scanning down
     * Returns that block position (the ground block), or null if not found
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

    /**
     * Creates a slightly larger footing at the ends and a short downward column to anchor it
     */
    private void stampFoot(WorldGenLevel level, RandomSource random, BlockPos foot, int radius) {
        // Small cap at the foot position
        placeBlob(level, random, foot, radius);

        // A short root column downward to ensure it starts at/below ground
        BlockPos p = foot.below();
        int depth = 3 + random.nextInt(4); // 3–6
        for (int i = 0; i < depth && p.getY() > level.getMinBuildHeight() + 4; i++) {
            BlockState state = level.getBlockState(p);
            if (!state.isAir() && !isDirt(state) && !state.is(Blocks.SCULK) && !state.is(Blocks.SCULK_VEIN)) {
                break;
            }
            setBlock(level, p, Blocks.SCULK.defaultBlockState());
            p = p.below();
        }
    }

    /**
     * Places a roughly spherical blob of sculk, replacing air/dirt-like/sculk/veins
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
                            || state.is(Blocks.SCULK)
                            || state.is(Blocks.SCULK_VEIN)) {
                        setBlock(level, p, Blocks.SCULK.defaultBlockState());
                    }
                }
            }
        }
    }
}
