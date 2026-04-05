package net.oldmanyounger.shroud.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Provides frame validation and interior fill helpers for Shroud portal creation.
 *
 * <p>This utility detects rectangular frame geometry, verifies border and interior
 * constraints, and fills valid interiors with Shroud portal blocks across both
 * horizontal axes.
 *
 * <p>In the broader context of the project, this class is part of Shroud's portal
 * construction pipeline that ensures activation logic remains deterministic and
 * compatible with custom frame requirements.
 */
public final class ShroudPortalHelper {

    // ==================================
    //  FIELDS
    // ==================================

    // Minimum allowed outer frame width including frame border
    private static final int MIN_OUTER_WIDTH = 4;

    // Minimum allowed outer frame height including frame border
    private static final int MIN_OUTER_HEIGHT = 5;

    // Maximum allowed outer frame width and height including frame border
    private static final int MAX_OUTER_SIZE = 23;

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Prevents instantiation of this static utility class
    private ShroudPortalHelper() {
    }

    // ==================================
    //  PUBLIC API
    // ==================================

    // Returns true when a valid frame exists around clicked position for either horizontal axis
    public static boolean canCreatePortal(ServerLevel level, BlockPos clickedPos) {
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            if (findFrame(level, clickedPos, axis) != null) {
                return true;
            }
        }
        return false;
    }

    // Attempts to create a portal around clicked frame block for either horizontal axis
    public static boolean tryCreatePortal(ServerLevel level, BlockPos clickedPos) {
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            if (tryCreatePortalForAxis(level, clickedPos, axis)) {
                return true;
            }
        }
        return false;
    }

    // Returns true when the given portal block still belongs to a valid detected frame
    public static boolean isValidExistingPortal(ServerLevel level, BlockPos portalPos, Direction.Axis axis) {
        Direction right = getRight(axis);

        for (int dx = -MAX_OUTER_SIZE; dx <= MAX_OUTER_SIZE; dx++) {
            for (int dy = -MAX_OUTER_SIZE; dy <= MAX_OUTER_SIZE; dy++) {
                BlockPos candidate = portalPos.relative(right, dx).above(dy);

                if (!isFrameBlock(level.getBlockState(candidate))) {
                    continue;
                }

                PortalFrame frame = findFrame(level, candidate, axis);
                if (frame != null && isInsideInterior(frame, portalPos)) {
                    return true;
                }
            }
        }

        return false;
    }

    // ==================================
    //  PORTAL CREATION FLOW
    // ==================================

    // Detects and fills portal interior for a specific axis
    private static boolean tryCreatePortalForAxis(ServerLevel level, BlockPos clickedPos, Direction.Axis axis) {
        PortalFrame frame = findFrame(level, clickedPos, axis);
        if (frame == null) {
            return false;
        }

        BlockState portalState = ModBlocks.SCULK_PORTAL
                .get()
                .defaultBlockState()
                .setValue(ShroudPortalBlock.AXIS, axis);

        Direction right = getRight(axis);

        BlockPos interiorOrigin = frame.bottomLeftFrame.relative(right, 1).above(1);
        int interiorWidth = frame.outerWidth - 2;
        int interiorHeight = frame.outerHeight - 2;

        for (int x = 0; x < interiorWidth; x++) {
            for (int y = 0; y < interiorHeight; y++) {
                BlockPos current = interiorOrigin.relative(right, x).above(y);
                level.setBlockAndUpdate(current, portalState);
            }
        }

        return true;
    }

    // ==================================
    //  FRAME DETECTION
    // ==================================

    // Finds rectangular frame around clicked position for a specific axis
    private static PortalFrame findFrame(ServerLevel level, BlockPos clickedPos, Direction.Axis axis) {
        if (!isFrameBlock(level.getBlockState(clickedPos))) {
            return null;
        }

        BlockPos bottomColumnPos = moveDownToBottomFrame(level, clickedPos);

        Direction right = getRight(axis);
        BlockPos bottomLeftFrame = moveToFrameEdge(level, bottomColumnPos, right.getOpposite());
        BlockPos bottomRightFrame = moveToFrameEdge(level, bottomColumnPos, right);

        int outerWidth = distanceAlongAxis(bottomLeftFrame, bottomRightFrame, axis) + 1;
        if (outerWidth < MIN_OUTER_WIDTH || outerWidth > MAX_OUTER_SIZE) {
            return null;
        }

        int outerHeight = measureVerticalHeight(level, bottomLeftFrame);
        if (outerHeight < MIN_OUTER_HEIGHT || outerHeight > MAX_OUTER_SIZE) {
            return null;
        }

        if (!validateFrame(level, bottomLeftFrame, right, outerWidth, outerHeight)) {
            return null;
        }

        if (!validateInteriorEmpty(level, bottomLeftFrame, right, outerWidth, outerHeight)) {
            return null;
        }

        return new PortalFrame(bottomLeftFrame, outerWidth, outerHeight, axis);
    }

    // ==================================
    //  VALIDATION HELPERS
    // ==================================

    // Returns true when a position is inside the interior region of a detected frame
    private static boolean isInsideInterior(PortalFrame frame, BlockPos pos) {
        BlockPos bottomLeftFrame = frame.bottomLeftFrame();
        int outerWidth = frame.outerWidth();
        int outerHeight = frame.outerHeight();

        int interiorWidth = outerWidth - 2;
        int interiorHeight = outerHeight - 2;

        if (frame.axis() == Direction.Axis.X) {
            int interiorMinX = bottomLeftFrame.getX() + 1;
            int interiorMaxX = interiorMinX + interiorWidth - 1;
            int interiorMinY = bottomLeftFrame.getY() + 1;
            int interiorMaxY = interiorMinY + interiorHeight - 1;
            int planeZ = bottomLeftFrame.getZ();

            return pos.getZ() == planeZ
                    && pos.getX() >= interiorMinX && pos.getX() <= interiorMaxX
                    && pos.getY() >= interiorMinY && pos.getY() <= interiorMaxY;
        } else {
            int interiorMinZ = bottomLeftFrame.getZ() + 1;
            int interiorMaxZ = interiorMinZ + interiorWidth - 1;
            int interiorMinY = bottomLeftFrame.getY() + 1;
            int interiorMaxY = interiorMinY + interiorHeight - 1;
            int planeX = bottomLeftFrame.getX();

            return pos.getX() == planeX
                    && pos.getZ() >= interiorMinZ && pos.getZ() <= interiorMaxZ
                    && pos.getY() >= interiorMinY && pos.getY() <= interiorMaxY;
        }
    }

    // Returns horizontal right direction for the given portal axis
    private static Direction getRight(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    // Returns true when state is an allowed portal frame block
    private static boolean isFrameBlock(BlockState state) {
        return state.is(Blocks.DEEPSLATE_BRICKS) || state.is(Blocks.REINFORCED_DEEPSLATE);
    }

    // Moves down to lowest contiguous frame block in current column
    private static BlockPos moveDownToBottomFrame(ServerLevel level, BlockPos start) {
        BlockPos.MutableBlockPos mutable = start.mutable();

        while (mutable.getY() > level.getMinBuildHeight()
                && isFrameBlock(level.getBlockState(mutable.below()))) {
            mutable.move(Direction.DOWN);
        }

        return mutable.immutable();
    }

    // Moves horizontally to edge of contiguous frame row
    private static BlockPos moveToFrameEdge(ServerLevel level, BlockPos start, Direction direction) {
        BlockPos.MutableBlockPos mutable = start.mutable();

        while (isFrameBlock(level.getBlockState(mutable.relative(direction)))) {
            mutable.move(direction);
        }

        return mutable.immutable();
    }

    // Measures contiguous vertical frame height from bottom-left frame position
    private static int measureVerticalHeight(ServerLevel level, BlockPos bottomLeftFrame) {
        int height = 1;
        BlockPos.MutableBlockPos mutable = bottomLeftFrame.mutable();
        int maxY = level.getMaxBuildHeight() - 1;

        while (mutable.getY() + 1 <= maxY && isFrameBlock(level.getBlockState(mutable.above()))) {
            mutable.move(Direction.UP);
            height++;

            if (height > MAX_OUTER_SIZE) {
                break;
            }
        }

        return height;
    }

    // Validates rectangle border consists entirely of frame blocks
    private static boolean validateFrame(ServerLevel level, BlockPos bottomLeftFrame, Direction right, int outerWidth, int outerHeight) {
        for (int x = 0; x < outerWidth; x++) {
            for (int y = 0; y < outerHeight; y++) {
                BlockPos current = bottomLeftFrame.relative(right, x).above(y);

                boolean isFrame =
                        x == 0 || x == outerWidth - 1 ||
                                y == 0 || y == outerHeight - 1;

                if (isFrame && !isFrameBlock(level.getBlockState(current))) {
                    return false;
                }
            }
        }

        return true;
    }

    // Validates interior consists only of air or existing portal blocks
    private static boolean validateInteriorEmpty(ServerLevel level, BlockPos bottomLeftFrame, Direction right, int outerWidth, int outerHeight) {
        BlockState portalBlock = ModBlocks.SCULK_PORTAL.get().defaultBlockState();

        int interiorWidth = outerWidth - 2;
        int interiorHeight = outerHeight - 2;

        BlockPos interiorOrigin = bottomLeftFrame.relative(right, 1).above(1);

        for (int x = 0; x < interiorWidth; x++) {
            for (int y = 0; y < interiorHeight; y++) {
                BlockPos current = interiorOrigin.relative(right, x).above(y);
                BlockState state = level.getBlockState(current);

                if (!state.isAir() && !state.is(portalBlock.getBlock())) {
                    return false;
                }
            }
        }

        return true;
    }

    // Computes absolute horizontal distance along axis between two positions
    private static int distanceAlongAxis(BlockPos a, BlockPos b, Direction.Axis axis) {
        return axis == Direction.Axis.X
                ? Math.abs(b.getX() - a.getX())
                : Math.abs(b.getZ() - a.getZ());
    }

    // ==================================
    //  INNER TYPES
    // ==================================

    // Describes detected frame placement, size, and orientation
    private record PortalFrame(BlockPos bottomLeftFrame, int outerWidth, int outerHeight, Direction.Axis axis) {
    }
}