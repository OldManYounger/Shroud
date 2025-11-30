package net.oldmanyounger.shroud.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.LivingSculkEntity;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model definition for the {@link LivingSculkEntity}
 * <p>
 * This class provides the resource locations for:
 * <ul>
 *   <li>The entity's geometric model file ({@code .geo.json})</li>
 *   <li>The entity's texture file ({@code .png})</li>
 *   <li>The entity's GeckoLib animation file ({@code .animation.json})</li>
 * </ul>
 * These resources define the Living Sculk’s 3D structure, appearance, and
 * animation timelines, and are automatically consumed by GeckoLib’s renderer
 * during rendering and animation evaluation
 */
public class LivingSculkModel extends GeoModel<LivingSculkEntity> {

    // Returns the geometry resource used to build the entity’s model
    @SuppressWarnings("Depreciated")
    @Override
    public ResourceLocation getModelResource(LivingSculkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "geo/living_sculk.geo.json"
        );
    }

    // Returns the texture applied to the model during rendering
    @Override
    public ResourceLocation getTextureResource(LivingSculkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "textures/entity/living_sculk.png"
        );
    }

    // Returns the animation definition used by GeckoLib for this entity
    @Override
    public ResourceLocation getAnimationResource(LivingSculkEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "animations/living_sculk.animation.json"
        );
    }
}
