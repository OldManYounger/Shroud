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
import net.minecraft.world.level.portal.DimensionTransition;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Responsible for creating or locating Shroud portals in a target dimension.
 * This implementation always ensures a usable portal exists near the requested position.
 */
public final class ShroudPortalForcer {

    /** Post-transition hook that plays a portal travel sound for the teleporting entity */
    public static final DimensionTransition.PostDimensionTransition PLAY_TRAVEL_SOUND =
            ShroudPortalForcer::playTravelSound;

    /** Hidden constructor to prevent instantiation */
    private ShroudPortalForcer() {
    }

    /** Plays the standard Nether portal travel sound at the entity location after teleport */
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

    /**
     * Attempts to locate an existing Shroud portal near the target position, or creates one if none is found.
     * For now this simply builds a new portal frame in a safe nearby location.
     */
    public static BlockPos createOrFindPortal(ServerLevel level, BlockPos targetPos, Direction.Axis axis) {
        BlockPos clamped = clampToWorldBorder(level, targetPos);
        return buildSimplePortal(level, clamped, axis);
    }

    /**
     * Clamps the candidate portal position to remain within the world border and height limits.
     */
    private static BlockPos clampToWorldBorder(ServerLevel level, BlockPos pos) {
        WorldBorder border = level.getWorldBorder();

        int minX = (int) border.getMinX() + 16;
        int maxX = (int) border.getMaxX() - 16;
        int minZ = (int) border.getMinZ() + 16;
        int maxZ = (int) border.getMaxZ() - 16;

        int clampedX = Math.min(Math.max(pos.getX(), minX), maxX);
        int clampedZ = Math.min(Math.max(pos.getZ(), minZ), maxZ);

        int y = Math.min(
                level.getMaxBuildHeight() - 10,
                Math.max(level.getMinBuildHeight() + 10, pos.getY())
        );

        return new BlockPos(clampedX, y, clampedZ);
    }

    /**
     * Builds a simple 4x5 style portal frame using deepslate bricks and fills it with Shroud portal blocks.
     * Returns the position of the interior block at the bottom center of the portal.
     */
    private static BlockPos buildSimplePortal(ServerLevel level, BlockPos basePos, Direction.Axis axis) {
        Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;

        int interiorWidth = 2;
        int interiorHeight = 3;

        BlockPos bottomCenter = basePos;

        BlockPos bottomLeftInterior = bottomCenter.relative(right, -interiorWidth / 2);
        BlockPos bottomLeftFrame = bottomLeftInterior.relative(right, -1).below();

        BlockState frameState = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
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

        BlockPos interiorBottomCenter = bottomLeftInterior.above();
        return interiorBottomCenter;
    }
}
