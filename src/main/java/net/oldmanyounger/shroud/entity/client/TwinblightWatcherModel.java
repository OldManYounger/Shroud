package net.oldmanyounger.shroud.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.TwinblightWatcherEntity;
import software.bernie.geckolib.model.GeoModel;

/**
 * Supplies GeckoLib model, texture, and animation resources for Twinblight Watcher
 *
 * <p>This model class maps {@link TwinblightWatcherEntity} to authored geometry, texture, and animation assets for runtime rendering and animation playback.
 *
 * <p>In the broader context of the project, this class is part of Shroud's client asset-binding layer that links entity runtime render systems to content exported from art and animation tooling.
 */
public class TwinblightWatcherModel extends GeoModel<TwinblightWatcherEntity> {

    // Returns the geometry resource used to build the Twinblight Watcher model
    @SuppressWarnings("Depreciated")
    @Override
    public ResourceLocation getModelResource(TwinblightWatcherEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "geo/twinblight_watcher.geo.json"
        );
    }

    // Returns the texture resource applied to the Twinblight Watcher model
    @Override
    public ResourceLocation getTextureResource(TwinblightWatcherEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "textures/entity/twinblight_watcher.png"
        );
    }

    // Returns the animation resource used by GeckoLib for Twinblight Watcher
    @Override
    public ResourceLocation getAnimationResource(TwinblightWatcherEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "animations/twinblight_watcher.animation.json"
        );
    }
}