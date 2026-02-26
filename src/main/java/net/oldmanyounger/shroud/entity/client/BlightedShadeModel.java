package net.oldmanyounger.shroud.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.BlightedShadeEntity;
import software.bernie.geckolib.model.GeoModel;

public class BlightedShadeModel extends GeoModel<BlightedShadeEntity> {

    @Override
    public ResourceLocation getModelResource(BlightedShadeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "geo/blighted_shade.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlightedShadeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/entity/blighted_shade.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlightedShadeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "animations/blighted_shade.animation.json");
    }
}
