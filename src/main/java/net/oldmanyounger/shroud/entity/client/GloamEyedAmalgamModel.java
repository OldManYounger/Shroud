package net.oldmanyounger.shroud.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.GloamEyedAmalgamEntity;
import software.bernie.geckolib.model.GeoModel;

/**
 * Supplies GeckoLib model, texture, and animation resources for Gloam Eyed Amalgam.
 *
 * <p>This model class maps {@link GloamEyedAmalgamEntity} to authored geometry, texture, and animation assets for runtime rendering and animation playback.
 *
 * <p>In the broader context of the project, this class is part of Shroud's client asset-binding layer that links entity runtime render systems to content exported from art and animation tooling.
 */
public class GloamEyedAmalgamModel extends GeoModel<GloamEyedAmalgamEntity> {

    // Returns the geometry resource used to build the Gloam Eyed Amalgam model
    @SuppressWarnings("Depreciated")
    @Override
    public ResourceLocation getModelResource(GloamEyedAmalgamEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "geo/gloam_eyed_amalgam.geo.json"
        );
    }

    // Returns the texture resource applied to the Gloam Eyed Amalgam model
    @Override
    public ResourceLocation getTextureResource(GloamEyedAmalgamEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "textures/entity/gloam_eyed_amalgam.png"
        );
    }

    // Returns the animation resource used by GeckoLib for Gloam Eyed Amalgam
    @Override
    public ResourceLocation getAnimationResource(GloamEyedAmalgamEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "animations/gloam_eyed_amalgam.animation.json"
        );
    }
}