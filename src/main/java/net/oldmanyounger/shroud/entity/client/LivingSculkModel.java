package net.oldmanyounger.shroud.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.LivingSculkEntity;
import software.bernie.geckolib.model.GeoModel;

public class LivingSculkModel extends GeoModel<LivingSculkEntity> {

    @Override
    public ResourceLocation getModelResource(LivingSculkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "geo/living_sculk.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(LivingSculkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "textures/entity/living_sculk.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(LivingSculkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "animations/living_sculk.animation.json"
        );
    }
}
