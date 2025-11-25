package net.oldmanyounger.shroud.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Provides utility methods for validating portal frames and filling them with Shroud portal blocks.
 * <p>
 * This implementation mirrors vanilla Nether portal behavior:
 * <ul>
 *     <li>Supports rectangular portal frames with outer dimensions from 4x5 up to 23x23.</li>
 *     <li>Uses deepslate bricks as the frame material.</li>
 *     <li>Allows activation by right-clicking any frame block with an echo shard.</li>
 * </ul>
 */
public final class ShroudPortalHelper {

    // Minimum allowed outer frame width (including frame blocks)
    private static final int MIN_OUTER_WIDTH = 4;

    // Minimum allowed outer frame height (including frame blocks)
    private static final int MIN_OUTER_HEIGHT = 5;

    // Maximum allowed outer frame width and height (including frame blocks)
    private static final int MAX_OUTER_SIZE = 23;

    // Hidden constructor to prevent instantiation of this utility class
    private ShroudPortalHelper() {
    }

    /**
     * Returns true if a valid Shroud portal frame exists around the clicked position
     * for either horizontal axis without modifying any blocks.
     */
    public static boolean canCreatePortal(ServerLevel level, BlockPos clickedPos) {
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            if (findFrame(level, clickedPos, axis) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to create a Shroud portal around the clicked frame block for either horizontal axis.
     * This inspects the surrounding deepslate brick frame and, if valid, fills the interior with portal blocks.
     */
    public static boolean tryCreatePortal(ServerLevel level, BlockPos clickedPos) {
        // Try both horizontal axes (X and Z) to match arbitrary player placement
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            if (tryCreatePortalForAxis(level, clickedPos, axis)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to detect and fill a portal frame for a specific axis.
     * The frame must:
     * <ul>
     *     <li>Be made entirely of valid frame blocks.</li>
     *     <li>Form a rectangle with outer dimensions 4x5 to 23x23.</li>
     *     <li>Have an interior that is currently empty (air) or already portal blocks.</li>
     * </ul>
     */
    private static boolean tryCreatePortalForAxis(ServerLevel level, BlockPos clickedPos, Direction.Axis axis) {
        // Locate a valid frame rectangle surrounding the clicked position
        PortalFrame frame = findFrame(level, clickedPos, axis);
        if (frame == null) {
            return false;
        }

        // Prepare the portal state with the correct axis stored in its block state
        BlockState portalState = ModBlocks.SCULK_PORTAL
                .get()
                .defaultBlockState()
                .setValue(ShroudPortalBlock.AXIS, axis);

        // Determine the horizontal direction that spans the frame interior
        Direction right = getRight(axis);

        // Compute the origin of the interior (bottom-left interior block inside the frame border)
        BlockPos interiorOrigin = frame.bottomLeftFrame.relative(right, 1).above(1);
        int interiorWidth = frame.outerWidth - 2;
        int interiorHeight = frame.outerHeight - 2;

        // Fill every interior position with Shroud portal blocks
        for (int x = 0; x < interiorWidth; x++) {
            for (int y = 0; y < interiorHeight; y++) {
                // Compute the current interior block being filled
                BlockPos current = interiorOrigin.relative(right, x).above(y);
                level.setBlockAndUpdate(current, portalState);
            }
        }

        return true;
    }

    // Returns true if the portal block at the given position is still part of a valid frame
    public static boolean isValidExistingPortal(ServerLevel level, BlockPos portalPos, Direction.Axis axis) {
        // Use the axis to determine the horizontal scan direction around the portal
        Direction right = getRight(axis);

        // Scan a box of candidate frame positions around the portal interior
        for (int dx = -MAX_OUTER_SIZE; dx <= MAX_OUTER_SIZE; dx++) {
            for (int dy = -MAX_OUTER_SIZE; dy <= MAX_OUTER_SIZE; dy++) {
                // Translate from the portal interior to a potential frame block position
                BlockPos candidate = portalPos.relative(right, dx).above(dy);

                // Skip positions that are not frame blocks
                if (!isFrameBlock(level.getBlockState(candidate))) {
                    continue;
                }

                // Attempt to reconstruct a frame from this candidate position
                PortalFrame frame = findFrame(level, candidate, axis);
                if (frame != null && isInsideInterior(frame, portalPos)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Returns true if the given block position lies inside the interior of the frame rectangle
    private static boolean isInsideInterior(PortalFrame frame, BlockPos pos) {
        BlockPos bottomLeftFrame = frame.bottomLeftFrame();
        int outerWidth = frame.outerWidth();
        int outerHeight = frame.outerHeight();

        // Compute interior dimensions by stripping one block of frame on all sides
        int interiorWidth = outerWidth - 2;
        int interiorHeight = outerHeight - 2;

        // Handle X-aligned portals where the plane is constant in Z
        if (frame.axis() == Direction.Axis.X) {
            int interiorMinX = bottomLeftFrame.getX() + 1;
            int interiorMaxX = interiorMinX + interiorWidth - 1;
            int interiorMinY = bottomLeftFrame.getY() + 1;
            int interiorMaxY = interiorMinY + interiorHeight - 1;
            int planeZ = bottomLeftFrame.getZ();

            // Check that position lies on the plane and within interior bounds
            return pos.getZ() == planeZ
                    && pos.getX() >= interiorMinX && pos.getX() <= interiorMaxX
                    && pos.getY() >= interiorMinY && pos.getY() <= interiorMaxY;
        } else {
            // Handle Z-aligned portals where the plane is constant in X
            int interiorMinZ = bottomLeftFrame.getZ() + 1;
            int interiorMaxZ = interiorMinZ + interiorWidth - 1;
            int interiorMinY = bottomLeftFrame.getY() + 1;
            int interiorMaxY = interiorMinY + interiorHeight - 1;
            int planeX = bottomLeftFrame.getX();

            // Check that position lies on the plane and within interior bounds
            return pos.getX() == planeX
                    && pos.getZ() >= interiorMinZ && pos.getZ() <= interiorMaxZ
                    && pos.getY() >= interiorMinY && pos.getY() <= interiorMaxY;
        }
    }

    /**
     * Finds a rectangular portal frame around the clicked position for the given axis.
     * The frame is defined by contiguous frame blocks forming a rectangle.
     */
    private static PortalFrame findFrame(ServerLevel level, BlockPos clickedPos, Direction.Axis axis) {
        // Require that the clicked position is itself a frame block
        if (!isFrameBlock(level.getBlockState(clickedPos))) {
            return null;
        }

        // Move down to the lowest frame block in this vertical column
        BlockPos bottomColumnPos = moveDownToBottomFrame(level, clickedPos);

        // Determine the horizontal extent of the bottom frame row
        Direction right = getRight(axis);
        BlockPos bottomLeftFrame = moveToFrameEdge(level, bottomColumnPos, right.getOpposite());
        BlockPos bottomRightFrame = moveToFrameEdge(level, bottomColumnPos, right);

        // Compute outer frame width using the aligned axis distance
        int outerWidth = distanceAlongAxis(bottomLeftFrame, bottomRightFrame, axis) + 1;
        if (outerWidth < MIN_OUTER_WIDTH || outerWidth > MAX_OUTER_SIZE) {
            return null;
        }

        // Measure vertical height of the left frame column
        int outerHeight = measureVerticalHeight(level, bottomLeftFrame);
        if (outerHeight < MIN_OUTER_HEIGHT || outerHeight > MAX_OUTER_SIZE) {
            return null;
        }

        // Validate that all border positions of the rectangle are frame blocks
        if (!validateFrame(level, bottomLeftFrame, right, outerWidth, outerHeight)) {
            return null;
        }

        // Validate that the interior area is empty (air or portal blocks)
        if (!validateInteriorEmpty(level, bottomLeftFrame, right, outerWidth, outerHeight)) {
            return null;
        }

        // Return a description of this frame so callers can fill the interior
        return new PortalFrame(bottomLeftFrame, outerWidth, outerHeight, axis);
    }

    // Returns the direction that corresponds to "right" for a given portal axis
    private static Direction getRight(Direction.Axis axis) {
        // For X-axis portals, right is EAST/WEST; for Z-axis portals, right is SOUTH/NORTH
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    // Returns true if the given state is a valid frame block for the Shroud portal
    private static boolean isFrameBlock(BlockState state) {
        return state.is(Blocks.DEEPSLATE_BRICKS) || state.is(Blocks.REINFORCED_DEEPSLATE);
    }

    // Moves downward from a starting frame block until the lowest frame block in that column is reached
    private static BlockPos moveDownToBottomFrame(ServerLevel level, BlockPos start) {
        BlockPos.MutableBlockPos mutable = start.mutable();

        // Step downward while the block below is still part of the frame
        while (mutable.getY() > level.getMinBuildHeight()
                && isFrameBlock(level.getBlockState(mutable.below()))) {
            mutable.move(Direction.DOWN);
        }

        return mutable.immutable();
    }

    // Moves horizontally from a starting frame block in the given direction until the edge of the frame row is reached
    private static BlockPos moveToFrameEdge(ServerLevel level, BlockPos start, Direction direction) {
        BlockPos.MutableBlockPos mutable = start.mutable();

        // Step along the row while contiguous frame blocks are found
        while (isFrameBlock(level.getBlockState(mutable.relative(direction)))) {
            mutable.move(direction);
        }

        return mutable.immutable();
    }

    /**
     * Measures the number of contiguous frame blocks above the bottom-left frame block.
     * This is used as the outer frame height.
     */
    private static int measureVerticalHeight(ServerLevel level, BlockPos bottomLeftFrame) {
        int height = 1;
        BlockPos.MutableBlockPos mutable = bottomLeftFrame.mutable();
        int maxY = level.getMaxBuildHeight() - 1;

        // Walk upwards until a non-frame block or the vertical limit is reached
        while (mutable.getY() + 1 <= maxY && isFrameBlock(level.getBlockState(mutable.above()))) {
            mutable.move(Direction.UP);
            height++;

            // Guard against runaway height in malformed structures
            if (height > MAX_OUTER_SIZE) {
                break;
            }
        }

        return height;
    }

    /**
     * Validates that the frame formed by bottom-left, width, and height is entirely made of frame blocks
     * and that the interior boundaries (sides and top/bottom frame rows) are correct.
     */
    private static boolean validateFrame(ServerLevel level, BlockPos bottomLeftFrame, Direction right, int outerWidth, int outerHeight) {
        // Loop over every position in the outer rectangle defined by width and height
        for (int x = 0; x < outerWidth; x++) {
            for (int y = 0; y < outerHeight; y++) {
                // Compute the current frame or interior position
                BlockPos current = bottomLeftFrame.relative(right, x).above(y);

                // Identify whether this position lies on the outer border of the rectangle
                boolean isFrame =
                        x == 0 || x == outerWidth - 1 ||
                                y == 0 || y == outerHeight - 1;

                // Only border positions must be frame blocks; interior can be anything at this stage
                if (isFrame && !isFrameBlock(level.getBlockState(current))) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Validates that the interior of the frame is currently empty (air) or already portal blocks.
     * This ensures the portal does not embed itself into solid terrain.
     */
    private static boolean validateInteriorEmpty(ServerLevel level, BlockPos bottomLeftFrame, Direction right, int outerWidth, int outerHeight) {
        // Portal state used to check for already-existing Shroud portal blocks
        BlockState portalBlock = ModBlocks.SCULK_PORTAL.get().defaultBlockState();

        // Compute interior dimensions by removing the border thickness
        int interiorWidth = outerWidth - 2;
        int interiorHeight = outerHeight - 2;

        // Compute the interior origin one block inside the frame border
        BlockPos interiorOrigin = bottomLeftFrame.relative(right, 1).above(1);

        // Check every interior position for emptiness or existing portal
        for (int x = 0; x < interiorWidth; x++) {
            for (int y = 0; y < interiorHeight; y++) {
                // Compute the current interior position being validated
                BlockPos current = interiorOrigin.relative(right, x).above(y);
                BlockState state = level.getBlockState(current);

                // Reject frames whose interior contains solid non-portal blocks
                if (!state.isAir() && !state.is(portalBlock.getBlock())) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Computes the horizontal distance between two positions along the portal axis.
     * This is used to derive the outer frame width.
     */
    private static int distanceAlongAxis(BlockPos a, BlockPos b, Direction.Axis axis) {
        // Use X distance for X-axis portals and Z distance for Z-axis portals
        return axis == Direction.Axis.X
                ? Math.abs(b.getX() - a.getX())
                : Math.abs(b.getZ() - a.getZ());
    }

    /**
     * Describes a detected rectangular portal frame, including placement, size, and axis.
     */
    private record PortalFrame(BlockPos bottomLeftFrame, int outerWidth, int outerHeight, Direction.Axis axis) {
    }
}
