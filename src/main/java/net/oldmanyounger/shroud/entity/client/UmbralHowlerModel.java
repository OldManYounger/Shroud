package net.oldmanyounger.shroud.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.UmbralHowlerEntity;
import software.bernie.geckolib.model.GeoModel;

/**
 * Supplies GeckoLib model, texture, and animation resources for Umbral Howler.
 *
 * <p>This model class maps {@link UmbralHowlerEntity} to its geometry, texture,
 * and animation files used by GeckoLib during render and animation evaluation.
 *
 * <p>In the broader context of the project, this class is part of the client
 * asset-binding layer that connects entity runtime rendering to Shroud's authored
 * model and animation resource files.
 */
public class UmbralHowlerModel extends GeoModel<UmbralHowlerEntity> {

    // Returns the geometry resource used to build the Umbral Howler model
    @SuppressWarnings("Depreciated")
    @Override
    public ResourceLocation getModelResource(UmbralHowlerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "geo/umbral_howler.geo.json"
        );
    }

    // Returns the texture resource applied to the Umbral Howler model
    @Override
    public ResourceLocation getTextureResource(UmbralHowlerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "textures/entity/umbral_howler.png"
        );
    }

    // Returns the animation resource used by GeckoLib for Umbral Howler
    @Override
    public ResourceLocation getAnimationResource(UmbralHowlerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "animations/umbral_howler.animation.json"
        );
    }
}