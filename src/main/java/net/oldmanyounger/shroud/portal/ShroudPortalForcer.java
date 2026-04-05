package net.oldmanyounger.shroud.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Finds existing Shroud portals or constructs new ones in a target dimension.
 *
 * <p>This utility prioritizes nearest-portal reuse for stable return linking, then
 * falls back to terrain-aware frame construction and interior portal placement when
 * no valid portal is found nearby.
 *
 * <p>In the broader context of the project, this class is part of Shroud's portal
 * infrastructure that guarantees reliable cross-dimension travel endpoints and
 * consistent portal lifecycle behavior.
 */
public final class ShroudPortalForcer {

    // ==================================
    //  FIELDS
    // ==================================

    // Guard flag that suppresses validation while a portal is being built
    private static boolean BUILDING_PORTAL = false;

    // Post-transition hook used to play travel sound after teleport
    public static final DimensionTransition.PostDimensionTransition PLAY_TRAVEL_SOUND =
            ShroudPortalForcer::playTravelSound;

    // ==================================
    //  CONSTRUCTOR / STATE
    // ==================================

    // Prevents instantiation of this static utility class
    private ShroudPortalForcer() {
    }

    // Returns whether portal construction is currently in progress
    static boolean isBuildingPortal() {
        return BUILDING_PORTAL;
    }

    // ==================================
    //  PUBLIC ENTRY POINT
    // ==================================

    // Finds nearest portal or builds a new one at a suitable target position
    public static BlockPos createOrFindPortal(ServerLevel level, BlockPos targetPos, Direction.Axis axis) {
        BlockPos clamped = clampToWorldBorder(level, targetPos);

        BlockPos existing = findNearestPortal(level, clamped, 32);
        if (existing != null) {
            return moveToBottomOfPortal(level, existing);
        }

        BlockPos ground = findGroundNear(level, clamped);
        BlockPos basePos = ground.above();

        BUILDING_PORTAL = true;
        try {
            return buildSimplePortal(level, basePos, axis);
        } finally {
            BUILDING_PORTAL = false;
        }
    }

    // ==================================
    //  TRAVEL FEEDBACK
    // ==================================

    // Plays portal travel sound after transition completes
    private static void playTravelSound(Entity entity) {
        if (entity.level().isClientSide()) {
            return;
        }

        entity.level().playSound(
                null,
                entity.blockPosition(),
                SoundEvents.PORTAL_TRAVEL,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    // ==================================
    //  POSITION / SEARCH HELPERS
    // ==================================

    // Clamps X/Z target position to world border safe interior margin
    private static BlockPos clampToWorldBorder(ServerLevel level, BlockPos pos) {
        WorldBorder border = level.getWorldBorder();

        int minX = (int) border.getMinX() + 16;
        int maxX = (int) border.getMaxX() - 16;
        int minZ = (int) border.getMinZ() + 16;
        int maxZ = (int) border.getMaxZ() - 16;

        int clampedX = Math.min(Math.max(pos.getX(), minX), maxX);
        int clampedZ = Math.min(Math.max(pos.getZ(), minZ), maxZ);

        return new BlockPos(clampedX, pos.getY(), clampedZ);
    }

    // Searches for nearest Shroud portal block in a cubic radius around center
    private static BlockPos findNearestPortal(ServerLevel level, BlockPos center, int radius) {
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;

        for (int dx = -radius; dx <= radius; dx++) {
            int x = center.getX() + dx;

            for (int dz = -radius; dz <= radius; dz++) {
                int z = center.getZ() + dz;

                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (level.getBlockState(pos).is(ModBlocks.SCULK_PORTAL.get())) {
                        double distSq = pos.distSqr(center);

                        if (distSq < nearestDistSq) {
                            nearestDistSq = distSq;
                            nearest = pos;
                        }
                    }
                }
            }
        }

        if (nearest != null) {
            return moveToBottomOfPortal(level, nearest);
        }

        return null;
    }

    // Moves down to lowest connected portal block in the same vertical column
    private static BlockPos moveToBottomOfPortal(ServerLevel level, BlockPos portalPos) {
        BlockPos.MutableBlockPos mutable = portalPos.mutable();

        while (mutable.getY() > level.getMinBuildHeight()
                && level.getBlockState(mutable.below()).is(ModBlocks.SCULK_PORTAL.get())) {
            mutable.move(Direction.DOWN);
        }

        return mutable.immutable();
    }

    // Finds solid supporting ground using motion-blocking heightmap data
    private static BlockPos findGroundNear(ServerLevel level, BlockPos pos) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        int groundY = Math.max(level.getMinBuildHeight(), topY - 1);
        return new BlockPos(pos.getX(), groundY, pos.getZ());
    }

    // ==================================
    //  PORTAL CONSTRUCTION
    // ==================================

    // Builds rectangular frame and interior portal blocks and returns interior bottom center
    private static BlockPos buildSimplePortal(ServerLevel level, BlockPos basePos, Direction.Axis axis) {
        Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;

        int interiorWidth = 2;
        int interiorHeight = 3;

        BlockPos bottomCenter = basePos;
        BlockPos bottomLeftInterior = bottomCenter.relative(right, -interiorWidth / 2);
        BlockPos bottomLeftFrame = bottomLeftInterior.relative(right, -1).below();

        BlockState frameState = Blocks.REINFORCED_DEEPSLATE.defaultBlockState();

        BlockState portalState = ModBlocks.SCULK_PORTAL
                .get()
                .defaultBlockState()
                .setValue(ShroudPortalBlock.AXIS, axis);

        int frameWidth = interiorWidth + 2;
        int frameHeight = interiorHeight + 2;

        for (int x = 0; x < frameWidth; x++) {
            for (int y = 0; y < frameHeight; y++) {
                BlockPos current = bottomLeftFrame.relative(right, x).above(y);

                boolean isFrame =
                        x == 0 || x == frameWidth - 1 ||
                                y == 0 || y == frameHeight - 1;

                if (isFrame) {
                    level.setBlockAndUpdate(current, frameState);
                } else {
                    level.setBlockAndUpdate(current, portalState);
                }
            }
        }

        return bottomLeftInterior.above();
    }
}