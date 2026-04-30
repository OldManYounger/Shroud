package net.oldmanyounger.shroud.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.GloamEyedAmalgamEntity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

/**
 * Supplies GeckoLib model, texture, and animation resources for Gloam Eyed Amalgam.
 *
 * <p>This model class maps {@link GloamEyedAmalgamEntity} to authored geometry, texture, and animation assets for runtime rendering and animation playback.
 *
 * <p>In the broader context of the project, this class is part of Shroud's client asset-binding layer that links entity runtime render systems to content exported from art and animation tooling.
 */
public class GloamEyedAmalgamModel extends GeoModel<GloamEyedAmalgamEntity> {

    // ==================================
    //  CONSTANTS
    // ==================================

    // Vanilla-like clamp values for head look in degrees
    private static final float MAX_HEAD_YAW_DEGREES = 75.0F;
    private static final float MIN_HEAD_PITCH_DEGREES = -35.0F;
    private static final float MAX_HEAD_PITCH_DEGREES = 40.0F;

    // Secondary head follows with slight lag and spread
    private static final float SECONDARY_YAW_SCALE = 0.82F;
    private static final float SECONDARY_PITCH_SCALE = 0.85F;
    private static final float OUTWARD_YAW_OFFSET_DEGREES = 6.0F;

    // ==================================
    //  MODEL RESOURCES
    // ==================================

    // Returns the geometry resource used to build the Gloam Eyed Amalgam model
    @SuppressWarnings("Depreciated")
    @Override
    public ResourceLocation getModelResource(GloamEyedAmalgamEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "geo/gloam_eyed_amalgam.geo.json"
        );
    }

    // Returns the texture resource applied to the Gloam Eyed Amalgam model
    @Override
    public ResourceLocation getTextureResource(GloamEyedAmalgamEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "textures/entity/gloam_eyed_amalgam.png"
        );
    }

    // Returns the animation resource used by GeckoLib for Gloam Eyed Amalgam
    @Override
    public ResourceLocation getAnimationResource(GloamEyedAmalgamEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(
                Shroud.MOD_ID,
                "animations/gloam_eyed_amalgam.animation.json"
        );
    }

    // ==================================
    //  CUSTOM LOOK ROTATION
    // ==================================

    // Applies vanilla look yaw and pitch directly to left and right head pivots
    @Override
    public void setCustomAnimations(
            GloamEyedAmalgamEntity animatable,
            long instanceId,
            software.bernie.geckolib.animation.AnimationState<GloamEyedAmalgamEntity> animationState
    ) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        if (!animatable.allowCustomHeadLook()) {
            return;
        }

        GeoBone headLeft = this.getAnimationProcessor().getBone("HeadLeft");
        GeoBone headRight = this.getAnimationProcessor().getBone("HeadRight");
        if (headLeft == null || headRight == null) {
            return;
        }

        EntityModelData modelData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        if (modelData == null) {
            return;
        }

        float yawDeg = clamp(modelData.netHeadYaw(), -MAX_HEAD_YAW_DEGREES, MAX_HEAD_YAW_DEGREES);
        float pitchDeg = clamp(modelData.headPitch(), MIN_HEAD_PITCH_DEGREES, MAX_HEAD_PITCH_DEGREES);

        // Left head acts as primary look head
        float leftYawRad = degreesToRadians(yawDeg + OUTWARD_YAW_OFFSET_DEGREES);
        float leftPitchRad = degreesToRadians(pitchDeg);

        // Right head trails slightly and points a little outward for two-head identity
        float rightYawRad = degreesToRadians((yawDeg * SECONDARY_YAW_SCALE) - OUTWARD_YAW_OFFSET_DEGREES);
        float rightPitchRad = degreesToRadians(pitchDeg * SECONDARY_PITCH_SCALE);

        headLeft.setRotY(leftYawRad);
        headLeft.setRotX(leftPitchRad);

        headRight.setRotY(rightYawRad);
        headRight.setRotX(rightPitchRad);
    }

    // Converts degrees to radians
    private static float degreesToRadians(float degrees) {
        return degrees * ((float) Math.PI / 180.0F);
    }

    // Clamps a value between min and max
    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}