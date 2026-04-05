package net.oldmanyounger.shroud.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Finds and validates anchor stone positions around a corrupted reliquary.
 *
 * <p>This utility applies spatial constraints for ritual anchors, including
 * horizontal radius, vertical tolerance, and min/max anchor count checks,
 * while returning deterministic ordered results for downstream ritual logic.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * ritual topology layer that keeps anchor detection rules centralized and
 * consistent across interaction, matching, and execution systems.
 */
public final class RitualAnchorSearch {

    // ==================================
    //  FIELDS
    // ==================================

    // Minimum number of anchors required for ritual eligibility
    public static final int MIN_ANCHORS = 4;

    // Maximum number of anchors allowed for ritual eligibility
    public static final int MAX_ANCHORS = 8;

    // Maximum horizontal offset in X or Z from reliquary
    public static final int MAX_HORIZONTAL_RANGE = 7;

    // Maximum vertical difference from reliquary Y
    public static final int MAX_VERTICAL_DIFFERENCE = 1;

    // Prevents instantiation of this static utility class
    private RitualAnchorSearch() {
    }

    // ==================================
    //  SEARCH
    // ==================================

    // Scans for anchor positions near reliquary using provided block predicate
    public static List<BlockPos> findAnchors(Level level, BlockPos reliquaryPos, Predicate<BlockPos> isAnchorAtPos) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(reliquaryPos, "reliquaryPos");
        Objects.requireNonNull(isAnchorAtPos, "isAnchorAtPos");

        List<BlockPos> found = new ArrayList<>();

        int baseY = reliquaryPos.getY();

        for (int dx = -MAX_HORIZONTAL_RANGE; dx <= MAX_HORIZONTAL_RANGE; dx++) {
            for (int dz = -MAX_HORIZONTAL_RANGE; dz <= MAX_HORIZONTAL_RANGE; dz++) {
                // Skips center block where reliquary sits
                if (dx == 0 && dz == 0) {
                    continue;
                }

                // Uses square horizontal search bounds for predictable scanning cost
                int x = reliquaryPos.getX() + dx;
                int z = reliquaryPos.getZ() + dz;

                for (int dy = -MAX_VERTICAL_DIFFERENCE; dy <= MAX_VERTICAL_DIFFERENCE; dy++) {
                    int y = baseY + dy;
                    BlockPos candidate = new BlockPos(x, y, z);

                    if (isAnchorAtPos.test(candidate)) {
                        found.add(candidate.immutable());
                    }
                }
            }
        }

        // Sorts by distance first and then stable coordinate tiebreak
        found.sort(Comparator
                .comparingDouble((BlockPos p) -> p.distSqr(reliquaryPos))
                .thenComparingInt(BlockPos::getX)
                .thenComparingInt(BlockPos::getY)
                .thenComparingInt(BlockPos::getZ));

        return found;
    }

    // ==================================
    //  VALIDATION
    // ==================================

    // Returns true when found anchor count satisfies configured ritual limits
    public static boolean isAnchorCountValid(int count) {
        return count >= MIN_ANCHORS && count <= MAX_ANCHORS;
    }

    // Returns true when all provided anchors satisfy local position rules
    public static boolean allAnchorsInRange(BlockPos reliquaryPos, List<BlockPos> anchors) {
        Objects.requireNonNull(reliquaryPos, "reliquaryPos");
        Objects.requireNonNull(anchors, "anchors");

        for (BlockPos anchor : anchors) {
            if (!isAnchorInRange(reliquaryPos, anchor)) {
                return false;
            }
        }

        return true;
    }

    // Returns true when a single anchor satisfies horizontal and vertical limits
    public static boolean isAnchorInRange(BlockPos reliquaryPos, BlockPos anchorPos) {
        int dx = Math.abs(anchorPos.getX() - reliquaryPos.getX());
        int dz = Math.abs(anchorPos.getZ() - reliquaryPos.getZ());
        int dy = Math.abs(anchorPos.getY() - reliquaryPos.getY());

        return dx <= MAX_HORIZONTAL_RANGE
                && dz <= MAX_HORIZONTAL_RANGE
                && dy <= MAX_VERTICAL_DIFFERENCE
                && !(dx == 0 && dz == 0 && dy == 0);
    }

    // Returns true when current anchor list is fully ritual-eligible
    public static boolean isValidRitualAnchorSet(BlockPos reliquaryPos, List<BlockPos> anchors) {
        return isAnchorCountValid(anchors.size()) && allAnchorsInRange(reliquaryPos, anchors);
    }
}