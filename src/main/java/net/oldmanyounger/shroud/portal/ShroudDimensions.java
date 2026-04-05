package net.oldmanyounger.shroud.portal;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.oldmanyounger.shroud.Shroud;

/**
 * Defines dimension keys and portal destination routing for Shroud travel.
 *
 * <p>This class centralizes the custom Shroud dimension key, references the
 * Overworld key, and provides helper logic to resolve bidirectional transitions.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * dimension traversal infrastructure that keeps portal destination decisions
 * consistent across teleportation and portal systems.
 */
public final class ShroudDimensions {

    // Resource key for the shroud:shroud custom dimension
    public static final ResourceKey<Level> SHROUD_LEVEL =
            ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "shroud")
            );

    // Resource key for the vanilla overworld dimension
    public static final ResourceKey<Level> OVERWORLD_LEVEL = Level.OVERWORLD;

    // Prevents instantiation of this static utility class
    private ShroudDimensions() {
    }

    // Resolves target dimension for portal travel between Overworld and Shroud
    public static ResourceKey<Level> getTargetDimension(ResourceKey<Level> current) {
        if (current.equals(SHROUD_LEVEL)) {
            return OVERWORLD_LEVEL;
        }
        return SHROUD_LEVEL;
    }
}