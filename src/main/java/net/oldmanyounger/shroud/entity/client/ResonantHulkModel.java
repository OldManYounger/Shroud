package net.oldmanyounger.shroud.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.ResonantHulkEntity;
import software.bernie.geckolib.model.GeoModel;

public class ResonantHulkModel extends GeoModel<ResonantHulkEntity> {

    @Override
    public ResourceLocation getModelResource(ResonantHulkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "geo/living_sculk.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ResonantHulkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/entity/living_sculk.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ResonantHulkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "animations/living_sculk.animation.json");
    }
}
