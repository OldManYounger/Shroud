package net.oldmanyounger.shroud.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.oldmanyounger.shroud.Shroud;

/**
 * Declares custom entity type tag keys used by Shroud runtime logic.
 *
 * <p>This class centralizes tag identifiers for entity grouping checks used by AI,
 * targeting, and vibration filtering behavior.
 *
 * <p>In the broader context of the project, this class is part of Shroud's tag
 * contract layer that keeps data-driven entity grouping consistent across systems.
 */
public final class ModEntityTypeTags {

    // Entity types that should be treated as friendly by vibration-aware sculk mobs
    public static final TagKey<EntityType<?>> VIBRATION_FRIENDLY =
            TagKey.create(
                    Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "vibration_friendly")
            );

    // Prevents instantiation of this static tag holder class
    private ModEntityTypeTags() {}
}