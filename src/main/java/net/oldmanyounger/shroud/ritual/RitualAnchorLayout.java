package net.oldmanyounger.shroud.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Defines canonical anchor pedestal directions and position mapping around a reliquary.
 *
 * <p>This utility enforces a single directional ordering and coordinate mapping for
 * anchor stones, ensuring recipes, block entities, and ritual checks all reference
 * the same north-east-south-west layout.
 *
 * <p>In the broader context of the project, this class is part of Shroud's ritual
 * coordination layer that keeps spatial rules deterministic across matching,
 * execution, and synchronization logic.
 */
public final class RitualAnchorLayout {

    // ==================================
    //  FIELDS
    // ==================================

    // Fixed recipe and validation order for anchor slots
    public static final List<AnchorSlot> ORDERED_SLOTS = List.of(
            AnchorSlot.NORTH,
            AnchorSlot.EAST,
            AnchorSlot.SOUTH,
            AnchorSlot.WEST
    );

    // Horizontal distance from reliquary center to each anchor stone
    private static final int DEFAULT_RADIUS = 1;

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Prevents instantiation of this static utility class
    private RitualAnchorLayout() {
    }

    // ==================================
    //  POSITION HELPERS
    // ==================================

    // Returns anchor position for a specific slot at default radius
    public static BlockPos anchorPos(BlockPos reliquaryPos, AnchorSlot slot) {
        return anchorPos(reliquaryPos, slot, DEFAULT_RADIUS);
    }

    // Returns anchor position for a specific slot at custom radius
    public static BlockPos anchorPos(BlockPos reliquaryPos, AnchorSlot slot, int radius) {
        return reliquaryPos.relative(slot.direction, radius);
    }

    // Returns all anchor positions in deterministic slot order
    public static List<BlockPos> orderedAnchorPositions(BlockPos reliquaryPos) {
        return ORDERED_SLOTS.stream()
                .map(slot -> anchorPos(reliquaryPos, slot))
                .toList();
    }

    // Returns map of slot to anchor position for quick keyed lookups
    public static Map<AnchorSlot, BlockPos> anchorPositionMap(BlockPos reliquaryPos) {
        EnumMap<AnchorSlot, BlockPos> result = new EnumMap<>(AnchorSlot.class);
        for (AnchorSlot slot : ORDERED_SLOTS) {
            result.put(slot, anchorPos(reliquaryPos, slot));
        }
        return result;
    }

    // Returns true when a position matches one of the four anchors around this reliquary
    public static boolean isAnchorFor(BlockPos reliquaryPos, BlockPos candidate) {
        for (AnchorSlot slot : ORDERED_SLOTS) {
            if (anchorPos(reliquaryPos, slot).equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    // ==================================
    //  SLOT ENUM
    // ==================================

    // Canonical directional anchor slots around the reliquary
    public enum AnchorSlot {
        NORTH(Direction.NORTH),
        EAST(Direction.EAST),
        SOUTH(Direction.SOUTH),
        WEST(Direction.WEST);

        // World direction corresponding to this slot
        public final Direction direction;

        // Creates slot with backing world direction
        AnchorSlot(Direction direction) {
            this.direction = direction;
        }

        // Returns slot from cardinal direction or null if direction is vertical
        public static AnchorSlot fromDirection(Direction direction) {
            return switch (direction) {
                case NORTH -> NORTH;
                case EAST -> EAST;
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                default -> null;
            };
        }
    }
}