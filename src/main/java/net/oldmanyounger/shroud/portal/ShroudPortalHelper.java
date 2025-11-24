package net.oldmanyounger.shroud.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Provides utility methods for validating portal frames and filling them with Shroud portal blocks.
 */
public final class ShroudPortalHelper {

    /** Fixed interior width for Shroud portals (matching Nether portal) */
    private static final int INTERIOR_WIDTH = 2;

    /** Fixed interior height for Shroud portals (matching Nether portal) */
    private static final int INTERIOR_HEIGHT = 3;

    /** Hidden constructor to prevent instantiation */
    private ShroudPortalHelper() {
    }

    /**
     * Attempts to create a Shroud portal around the clicked frame block.
     * This searches a small area for a 4x5 deepslate brick frame with an empty interior.
     */
    public static boolean tryCreatePortal(ServerLevel level, BlockPos clickedPos) {
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            if (tryCreatePortalForAxis(level, clickedPos, axis)) {
                return true;
            }
        }
        return false;
    }

    /** Searches for a valid frame and fills it with portal blocks for the given axis */
    private static boolean tryCreatePortalForAxis(ServerLevel level, BlockPos clickedPos, Direction.Axis axis) {
        int searchRadiusHorizontal = 4;
        int searchRadiusVertical = 4;

        for (int dy = -searchRadiusVertical; dy <= searchRadiusVertical; dy++) {
            for (int offset = -searchRadiusHorizontal; offset <= searchRadiusHorizontal; offset++) {
                BlockPos interiorOrigin;

                if (axis == Direction.Axis.X) {
                    interiorOrigin = clickedPos.offset(0, dy, offset);
                } else {
                    interiorOrigin = clickedPos.offset(offset, dy, 0);
                }

                if (tryFillFrameAt(level, interiorOrigin, axis)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Validates a potential frame location and, if valid, fills its interior with Shroud portal blocks.
     * The interior is assumed to start at interiorOrigin and extend along the primary axis and up.
     */
    private static boolean tryFillFrameAt(ServerLevel level, BlockPos interiorOrigin, Direction.Axis axis) {
        Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;

        BlockPos bottomLeftInterior = interiorOrigin;
        BlockPos bottomLeftFrame = bottomLeftInterior.relative(right, -1).below();

        BlockState frameState = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState portalState = ModBlocks.SCULK_PORTAL
                .get()
                .defaultBlockState()
                .setValue(ShroudPortalBlock.AXIS, axis);

        int frameWidth = INTERIOR_WIDTH + 2;
        int frameHeight = INTERIOR_HEIGHT + 2;

        int portalBlockCount = 0;

        for (int x = 0; x < frameWidth; x++) {
            for (int y = 0; y < frameHeight; y++) {
                BlockPos current = bottomLeftFrame.relative(right, x).above(y);
                BlockState currentState = level.getBlockState(current);

                boolean isFrame =
                        x == 0 || x == frameWidth - 1 ||
                                y == 0 || y == frameHeight - 1;

                if (isFrame) {
                    if (!currentState.is(frameState.getBlock())) {
                        return false;
                    }
                } else {
                    if (!currentState.isAir() && !currentState.is(ModBlocks.SCULK_PORTAL.get())) {
                        return false;
                    }
                    if (currentState.is(ModBlocks.SCULK_PORTAL.get())) {
                        portalBlockCount++;
                    }
                }
            }
        }

        if (portalBlockCount > 0) {
            return false;
        }

        for (int x = 0; x < frameWidth; x++) {
            for (int y = 0; y < frameHeight; y++) {
                BlockPos current = bottomLeftFrame.relative(right, x).above(y);

                boolean isFrame =
                        x == 0 || x == frameWidth - 1 ||
                                y == 0 || y == frameHeight - 1;

                if (!isFrame) {
                    level.setBlockAndUpdate(current, portalState);
                }
            }
        }

        return true;
    }
}
