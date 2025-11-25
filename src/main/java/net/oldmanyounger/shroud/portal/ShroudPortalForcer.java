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
import net.minecraft.world.level.levelgen.Heightmap;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Responsible for finding or constructing Shroud portals in a target dimension.
 * Prefers reusing existing portals to keep travel consistent, but can also build
 * a new frame and portal on suitable terrain when necessary.
 */
public final class ShroudPortalForcer {

    // Guard flag to temporarily suppress portal validation during construction
    private static boolean BUILDING_PORTAL = false;

    static boolean isBuildingPortal() {
        return BUILDING_PORTAL;
    }

    /** Post-transition hook used by Shroud portals to play a travel sound after teleport */
    public static final DimensionTransition.PostDimensionTransition PLAY_TRAVEL_SOUND =
            ShroudPortalForcer::playTravelSound;

    /** Prevents instantiation, since this class only exposes static utility methods */
    private ShroudPortalForcer() {
    }

    /** Plays the standard portal travel sound at the entity's location after teleport finishes */
    private static void playTravelSound(Entity entity) {
        // Only play the sound on the logical server to avoid double playback
        if (entity.level().isClientSide()) {
            return;
        }

        // Emit the portal travel sound centered on the entity
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
     * Locates an existing Shroud portal near a target position, or constructs a new one if none exists.
     * Reuses portals when possible so repeated Overworld <-> Shroud travel tends to link the same locations.
     */
    public static BlockPos createOrFindPortal(ServerLevel level, BlockPos targetPos, Direction.Axis axis) {
        // Clamp the desired target position into the world border before searching or building
        BlockPos clamped = clampToWorldBorder(level, targetPos);

        // Try to reuse an existing Shroud portal close to the clamped position
        BlockPos existing = findNearestPortal(level, clamped, 32);
        if (existing != null) {
            return moveToBottomOfPortal(level, existing);
        }

        // Determine a suitable ground position in this column for constructing a new portal
        BlockPos ground = findGroundNear(level, clamped);

        // Use the block directly above the ground as the interior bottom-center of the portal
        BlockPos basePos = ground.above();

        // Build the frame and interior blocks for a simple Shroud portal, returning its interior origin.
        // Suppress neighbor-based validation during this construction.
        BUILDING_PORTAL = true;
        try {
            return buildSimplePortal(level, basePos, axis);
        } finally {
            BUILDING_PORTAL = false;
        }
    }

    /**
     * Clamps a candidate portal position into the world border's inner area on the X/Z axes.
     * Y is left unchanged and is adjusted separately using heightmap and ground detection logic.
     */
    private static BlockPos clampToWorldBorder(ServerLevel level, BlockPos pos) {
        // Read the current world border limits for the target dimension
        WorldBorder border = level.getWorldBorder();

        // Apply a small inner margin so portals are not created flush against the border
        int minX = (int) border.getMinX() + 16;
        int maxX = (int) border.getMaxX() - 16;
        int minZ = (int) border.getMinZ() + 16;
        int maxZ = (int) border.getMaxZ() - 16;

        // Clamp the requested X/Z into the allowable inner region
        int clampedX = Math.min(Math.max(pos.getX(), minX), maxX);
        int clampedZ = Math.min(Math.max(pos.getZ(), minZ), maxZ);

        // Preserve Y so vertical placement can be handled by other helpers
        return new BlockPos(clampedX, pos.getY(), clampedZ);
    }

    /**
     * Searches a cubic region around a center point for the nearest Shroud portal block.
     * Scans the full build height so portals stacked at different elevations can still be found.
     */
    private static BlockPos findNearestPortal(ServerLevel level, BlockPos center, int radius) {
        // Track the closest portal position found so far and its squared distance
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        // Use world build limits to bound the vertical search range
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;

        // Search a (2r+1)^2 horizontal square around the center
        for (int dx = -radius; dx <= radius; dx++) {
            int x = center.getX() + dx;

            for (int dz = -radius; dz <= radius; dz++) {
                int z = center.getZ() + dz;

                // Scan vertically through the column at (x, z)
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    // Only consider blocks that match the Shroud portal block
                    if (level.getBlockState(pos).is(ModBlocks.SCULK_PORTAL.get())) {
                        double distSq = pos.distSqr(center);

                        // Update if this portal is the closest discovered so far
                        if (distSq < nearestDistSq) {
                            nearestDistSq = distSq;
                            nearest = pos;
                        }
                    }
                }
            }
        }

        // If a portal was found, normalize the position down to its base block
        if (nearest != null) {
            return moveToBottomOfPortal(level, nearest);
        }

        // Return null to signal that no nearby portal exists
        return null;
    }

    /**
     * Moves a portal position downward until the lowest portal block in that vertical column is found.
     * Ensures that entities spawn at the correct base position of an existing frame.
     */
    private static BlockPos moveToBottomOfPortal(ServerLevel level, BlockPos portalPos) {
        // Use a mutable position to step downward without creating many new BlockPos instances
        BlockPos.MutableBlockPos mutable = portalPos.mutable();

        // Walk downwards while staying above the build limit and remaining inside portal blocks
        while (mutable.getY() > level.getMinBuildHeight()
                && level.getBlockState(mutable.below()).is(ModBlocks.SCULK_PORTAL.get())) {
            mutable.move(Direction.DOWN);
        }

        // Return an immutable copy at the bottom-most portal block
        return mutable.immutable();
    }

    /**
     * Uses the MOTION_BLOCKING heightmap to find solid ground near the target X/Z position.
     * Returns the block that should support the base of the portal frame.
     */
    private static BlockPos findGroundNear(ServerLevel level, BlockPos pos) {
        // Query the topmost solid or motion-blocking block in this column
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());

        // Choose the block directly below that as the ground supporting the frame
        int groundY = Math.max(level.getMinBuildHeight(), topY - 1);

        // Construct a new BlockPos anchored to ground level at the same X/Z
        return new BlockPos(pos.getX(), groundY, pos.getZ());
    }

    /**
     * Builds a simple rectangular portal frame and fills its interior with Shroud portal blocks.
     * Returns the position of the interior bottom-center portal block for entity placement.
     */
    private static BlockPos buildSimplePortal(ServerLevel level, BlockPos basePos, Direction.Axis axis) {
        // Choose the horizontal direction that spans the portal width, based on its facing axis
        Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;

        // Interior dimensions of the portal opening (width x height)
        int interiorWidth = 2;
        int interiorHeight = 3;

        // Treat the provided base position as the interior bottom-center of the portal
        BlockPos bottomCenter = basePos;

        // Compute the interior bottom-left coordinate by shifting from center along the width
        BlockPos bottomLeftInterior = bottomCenter.relative(right, -interiorWidth / 2);

        // Compute the bottom-left frame coordinate by moving one block outward and one block down
        BlockPos bottomLeftFrame = bottomLeftInterior.relative(right, -1).below();

        // Use reinforced deepslate as the structural frame material
        BlockState frameState = Blocks.REINFORCED_DEEPSLATE.defaultBlockState();

        // Prepare the Shroud portal block state, including its horizontal axis
        BlockState portalState = ModBlocks.SCULK_PORTAL
                .get()
                .defaultBlockState()
                .setValue(ShroudPortalBlock.AXIS, axis);

        // Total frame dimensions include the border around the interior
        int frameWidth = interiorWidth + 2;
        int frameHeight = interiorHeight + 2;

        // Iterate over the full frame rectangle and place either frame or portal blocks
        for (int x = 0; x < frameWidth; x++) {
            for (int y = 0; y < frameHeight; y++) {
                // Compute the current block position within the frame
                BlockPos current = bottomLeftFrame.relative(right, x).above(y);

                // Determine whether this block lies on the outer frame edges
                boolean isFrame =
                        x == 0 || x == frameWidth - 1 ||
                                y == 0 || y == frameHeight - 1;

                // Place frame blocks on the border and portal blocks in the interior
                if (isFrame) {
                    level.setBlockAndUpdate(current, frameState);
                } else {
                    level.setBlockAndUpdate(current, portalState);
                }
            }
        }

        // The interior bottom-center portal block sits one block above the interior base position
        BlockPos interiorBottomCenter = bottomLeftInterior.above();
        return interiorBottomCenter;
    }
}
