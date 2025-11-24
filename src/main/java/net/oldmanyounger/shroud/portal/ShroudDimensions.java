package net.oldmanyounger.shroud.portal;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.oldmanyounger.shroud.Shroud;

/**
 * Centralizes dimension keys and routing logic for Shroud portals.
 */
public final class ShroudDimensions {

    /** Resource key for the shroud:shroud dimension declared in data/shroud/dimension/shroud.json */
    public static final ResourceKey<Level> SHROUD_LEVEL =
            ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "shroud")
            );

    /** Resource key for the vanilla overworld dimension */
    public static final ResourceKey<Level> OVERWORLD_LEVEL = Level.OVERWORLD;

    /** Hidden constructor to prevent instantiation */
    private ShroudDimensions() {
    }

    /**
     * Resolves the destination dimension for a portal transition.
     * Overworld -> Shroud, Shroud -> Overworld.
     */
    public static ResourceKey<Level> getTargetDimension(ResourceKey<Level> current) {
        if (current.equals(SHROUD_LEVEL)) {
            return OVERWORLD_LEVEL;
        }
        return SHROUD_LEVEL;
    }
}
