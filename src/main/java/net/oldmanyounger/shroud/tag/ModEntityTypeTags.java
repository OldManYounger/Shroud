package net.oldmanyounger.shroud.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.oldmanyounger.shroud.Shroud;

public final class ModEntityTypeTags {

    public static final TagKey<EntityType<?>> VIBRATION_FRIENDLY =
            TagKey.create(
                    Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "vibration_friendly")
            );

    private ModEntityTypeTags() {}
}