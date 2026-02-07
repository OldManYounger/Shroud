package net.oldmanyounger.shroud.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.UmbralHowlerEntity;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model definition for the {@link UmbralHowlerEntity}
 * <p>
 * This class provides the resource locations for:
 * <ul>
 *   <li>The entity's geometric model file ({@code .geo.json})</li>
 *   <li>The entity's texture file ({@code .png})</li>
 *   <li>The entity's GeckoLib animation file ({@code .animation.json})</li>
 * </ul>
 * These resources define the Umbral Howler’s 3D structure, appearance, and
 * animation timelines, and are automatically consumed by GeckoLib’s renderer
 * during rendering and animation evaluation
 */
public class UmbralHowlerModel extends GeoModel<UmbralHowlerEntity> {

    // Returns the geometry resource used to build the entity’s model
    @SuppressWarnings("Depreciated")
    @Override
    public ResourceLocation getModelResource(UmbralHowlerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "geo/umbral_howler.geo.json"
        );
    }

    // Returns the texture applied to the model during rendering
    @Override
    public ResourceLocation getTextureResource(UmbralHowlerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "textures/entity/umbral_howler.png"
        );
    }

    // Returns the animation definition used by GeckoLib for this entity
    @Override
    public ResourceLocation getAnimationResource(UmbralHowlerEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "animations/umbral_howler.animation.json"
        );
    }
}
