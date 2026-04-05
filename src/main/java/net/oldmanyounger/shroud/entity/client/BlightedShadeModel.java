package net.oldmanyounger.shroud.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.BlightedShadeEntity;
import software.bernie.geckolib.model.GeoModel;

/**
 * Supplies GeckoLib model, texture, and animation asset paths for Blighted Shade.
 *
 * <p>This model class maps the Blighted Shade entity to its geometry definition,
 * texture file, and animation timeline resources used by the GeckoLib renderer.
 *
 * <p>In the broader context of the project, this class is part of the client
 * rendering asset-binding layer that connects runtime entity rendering to Shroud's
 * authored resource-pack assets.
 */
public class BlightedShadeModel extends GeoModel<BlightedShadeEntity> {

    // Returns the geometry resource for the Blighted Shade model
    @Override
    public ResourceLocation getModelResource(BlightedShadeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "geo/blighted_shade.geo.json");
    }

    // Returns the texture resource used by the Blighted Shade model
    @Override
    public ResourceLocation getTextureResource(BlightedShadeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/entity/blighted_shade.png");
    }

    // Returns the animation resource used by the Blighted Shade model
    @Override
    public ResourceLocation getAnimationResource(BlightedShadeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "animations/blighted_shade.animation.json");
    }
}