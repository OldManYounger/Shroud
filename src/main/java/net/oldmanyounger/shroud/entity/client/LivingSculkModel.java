package net.oldmanyounger.shroud.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.LivingSculkEntity;
import software.bernie.geckolib.model.GeoModel;

/**
 * Supplies GeckoLib model, texture, and animation resources for Living Sculk.
 *
 * <p>This model class maps {@link LivingSculkEntity} to its geometry, texture,
 * and animation files used by GeckoLib during render and animation evaluation.
 *
 * <p>In the broader context of the project, this class is part of the client
 * asset-binding layer that connects entity runtime rendering to Shroud's authored
 * model and animation resource files.
 */
public class LivingSculkModel extends GeoModel<LivingSculkEntity> {

    // Returns the geometry resource used to build the Living Sculk model
    @SuppressWarnings("Depreciated")
    @Override
    public ResourceLocation getModelResource(LivingSculkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "geo/living_sculk.geo.json"
        );
    }

    // Returns the texture resource applied to the Living Sculk model
    @Override
    public ResourceLocation getTextureResource(LivingSculkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "textures/entity/living_sculk.png"
        );
    }

    // Returns the animation resource used by GeckoLib for Living Sculk
    @Override
    public ResourceLocation getAnimationResource(LivingSculkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "animations/living_sculk.animation.json"
        );
    }
}