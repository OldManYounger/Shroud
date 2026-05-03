package net.oldmanyounger.shroud.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.LivingSculkEntity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

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

    // ==================================
    //  CUSTOM LOOK ROTATION
    // ==================================

    // Vanilla-like clamp for idle head yaw in degrees.
    private static final float MAX_HEAD_YAW_DEGREES = 75.0F;

    // Applies vanilla look yaw to the shared parent head during normal idle only.
    @Override
    public void setCustomAnimations(
            LivingSculkEntity animatable,
            long instanceId,
            software.bernie.geckolib.animation.AnimationState<LivingSculkEntity> animationState
    ) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        if (!animatable.allowCustomHeadLook()) {
            return;
        }

        GeoBone head = this.getAnimationProcessor().getBone("Head");
        if (head == null) {
            return;
        }

        EntityModelData modelData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        if (modelData == null) {
            return;
        }

        float yawDeg = clamp(modelData.netHeadYaw(), -MAX_HEAD_YAW_DEGREES, MAX_HEAD_YAW_DEGREES);
        head.setRotY(degreesToRadians(yawDeg));
    }

    // Converts degrees to radians.
    private static float degreesToRadians(float degrees) {
        return degrees * ((float) Math.PI / 180.0F);
    }

    // Clamps a value between min and max.
    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

}