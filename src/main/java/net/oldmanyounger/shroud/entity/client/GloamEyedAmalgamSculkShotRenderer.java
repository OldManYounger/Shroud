package net.oldmanyounger.shroud.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.GloamEyedAmalgamSculkShotEntity;

/**
 * Renderer for the Gloam Eyed Amalgam's custom shulker-style projectile.
 *
 * <p>This renderer uses a custom baked model and texture while the entity keeps vanilla
 * shulker bullet movement behavior.
 */
public class GloamEyedAmalgamSculkShotRenderer extends EntityRenderer<GloamEyedAmalgamSculkShotEntity> {

    // Texture used by the custom sculk shot projectile
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Shroud.MOD_ID,
            "textures/entity/gloam_eyed_amalgam_sculk_shot.png"
    );

    // Custom projectile model
    private final GloamEyedAmalgamSculkShotModel model;

    // Creates the custom projectile renderer
    public GloamEyedAmalgamSculkShotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new GloamEyedAmalgamSculkShotModel(context.bakeLayer(GloamEyedAmalgamSculkShotModel.LAYER_LOCATION));
    }

    // Renders the custom projectile model
    @Override
    public void render(GloamEyedAmalgamSculkShotEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot() - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getXRot()));

        float ageInTicks = entity.tickCount + partialTick;
        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    // Returns the custom projectile texture
    @Override
    public ResourceLocation getTextureLocation(GloamEyedAmalgamSculkShotEntity entity) {
        return TEXTURE;
    }
}
