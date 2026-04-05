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
 * Generates large sculk arches anchored between two grounded endpoints.
 *
 * <p>This feature scans for valid sculk-like footing, computes a curved span with
 * variable height and thickness, and stamps blob-based geometry while guarding
 * against region clipping and invalid write boundaries.
 *
 * <p>In the broader context of the project, this class is part of Shroud's terrain
 * feature generation layer that provides signature large-scale environmental forms
 * reinforcing the mod's corrupted biome identity.
 */
public class SculkArchFeature extends Feature<NoneFeatureConfiguration> {

    // ==================================
    //  FIELDS
    // ==================================

    // Conservative half-span cap used to reduce clipped generation at write borders
    private static final int SAFE_MAX_HALF_SPAN = 24;

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Creates the feature with the provided configuration codec
    public SculkArchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    // ==================================
    //  FEATURE PLACEMENT
    // ==================================

    // Attempts to place a sculk arch at the requested origin
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        BlockPos origin = ctx.origin();
        RandomSource random = ctx.random();
        WorldGenLevel level = ctx.level();

        // Scans downward to find nearby non-air surface
        BlockPos basePos = origin;
        while (level.isEmptyBlock(basePos) && basePos.getY() > level.getMinBuildHeight() + 2) {
            basePos = basePos.below();
        }

        // Requires sculk-compatible surface at initial anchor
        if (!isSculkSurface(level.getBlockState(basePos))) {
            return false;
        }

        // Lifts base slightly to improve curve clearance
        basePos = basePos.above(random.nextInt(3));

        // Chooses random horizontal heading for arch span
        float angle = random.nextFloat() * (float) (Math.PI * 2.0);
        float dirX = Mth.cos(angle);
        float dirZ = Mth.sin(angle);

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
        archHeight += random.nextInt(3) - 1;
        archHeight = Mth.clamp(archHeight, minHeight, maxHeight);

        int thickness = 1 + Mth.floor(sizeT * 2.0F);
        thickness = Mth.clamp(thickness, 1, 4);

        // Computes arch endpoints in horizontal plane
        BlockPos endA = basePos.offset(Mth.floor(-dirX * halfSpan), 0, Mth.floor(-dirZ * halfSpan));
        BlockPos endB = basePos.offset(Mth.floor(dirX * halfSpan), 0, Mth.floor(dirZ * halfSpan));

        // Snaps endpoints to discovered ground
        BlockPos groundA = findGround(level, endA.above(12), 48);
        BlockPos groundB = findGround(level, endB.above(12), 48);

        if (groundA == null || groundB == null) {
            return false;
        }

        // Requires sculk-compatible footing at both endpoints
        if (!isSculkSurface(level.getBlockState(groundA)) || !isSculkSurface(level.getBlockState(groundB))) {
            return false;
        }

        // Sinks feet slightly into terrain for natural anchoring
        int sinkA = random.nextInt(4);
        int sinkB = random.nextInt(4);

        BlockPos footA = groundA.below(sinkA);
        BlockPos footB = groundB.below(sinkB);

        // Enforces conservative horizontal safety radius
        if (!isWithinHorizontalRadius(origin, footA, SAFE_MAX_HALF_SPAN + 8)
                || !isWithinHorizontalRadius(origin, footB, SAFE_MAX_HALF_SPAN + 8)) {
            return false;
        }

        int span = halfSpan * 2;
        int steps = Mth.clamp(span * 3, 50, 180);

        // Aborts early when any sampled arch point would be unwritable
        if (!canWriteEntireArch(level, footA, footB, archHeight, thickness, steps)) {
            return false;
        }

        // Stamps thicker support feet before drawing centerline
        stampFoot(level, random, footA, thickness + 1);
        stampFoot(level, random, footB, thickness + 1);

        for (int i = 0; i <= steps; i++) {
            float t = (float) i / (float) steps;

            float x = Mth.lerp(t, footA.getX(), footB.getX());
            float z = Mth.lerp(t, footA.getZ(), footB.getZ());
            float baseY = Mth.lerp(t, footA.getY(), footB.getY());

            float lift = Mth.sin((float) Math.PI * t) * archHeight;

            int px = Mth.floor(x);
            int pz = Mth.floor(z);
            int py = Mth.floor(baseY + lift);

            BlockPos center = new BlockPos(px, py, pz);

            // Places tube-like blob geometry around centerline sample
            placeBlob(level, random, center, thickness);
        }

        return true;
    }

    // ==================================
    //  SURFACE / POSITION HELPERS
    // ==================================

    // Returns true for allowed footing surface blocks
    private static boolean isSculkSurface(BlockState state) {
        return state.is(Blocks.SCULK) || state.is(ModBlocks.SCULK_GRASS.get());
    }

    // Scans downward and returns first non-air block or null when not found
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

    // Returns true when position is within axis-aligned horizontal radius
    private static boolean isWithinHorizontalRadius(BlockPos origin, BlockPos p, int radius) {
        return Math.abs(p.getX() - origin.getX()) <= radius
                && Math.abs(p.getZ() - origin.getZ()) <= radius;
    }

    // ==================================
    //  REGION SAFETY
    // ==================================

    // Preflights sampled centerline and shell points for write safety
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
                    c.offset(r, 0, 0), c.offset(-r, 0, 0),
                    c.offset(0, r, 0), c.offset(0, -r, 0),
                    c.offset(0, 0, r), c.offset(0, 0, -r),
                    c.offset(r, r, 0), c.offset(-r, r, 0),
                    c.offset(r, -r, 0), c.offset(-r, -r, 0),
                    c.offset(0, r, r), c.offset(0, r, -r),
                    c.offset(0, -r, r), c.offset(0, -r, -r)
            };

            for (BlockPos p : probes) {
                if (!level.ensureCanWrite(p)) return false;
            }
        }

        return true;
    }

    // ==================================
    //  GEOMETRY PLACEMENT
    // ==================================

    // Stamps thicker endpoint cap and short downward root support
    private void stampFoot(WorldGenLevel level, RandomSource random, BlockPos foot, int radius) {
        placeBlob(level, random, foot, radius);

        BlockPos p = foot.below();
        int depth = 3 + random.nextInt(4);
        for (int i = 0; i < depth && p.getY() > level.getMinBuildHeight() + 4; i++) {
            BlockState state = level.getBlockState(p);
            if (!state.isAir() && !isDirt(state) && !isSculkFillable(state) && !state.is(Blocks.SCULK_VEIN)) {
                break;
            }
            setBlock(level, p, Blocks.SCULK.defaultBlockState());
            p = p.below();
        }
    }

    // Returns true for blocks that can be replaced by sculk fill
    private static boolean isSculkFillable(BlockState state) {
        return state.is(Blocks.SCULK) || state.is(ModBlocks.SCULK_GRASS.get());
    }

    // Places rough spherical blob by replacing allowed local blocks
    private void placeBlob(WorldGenLevel level, RandomSource random, BlockPos center, int radius) {
        int r = Mth.clamp(radius, 1, 4);
        float rSq = (r + 0.25F) * (r + 0.25F);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    float distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > rSq) continue;

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