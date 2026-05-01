package net.oldmanyounger.shroud.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.custom.GloamEyedAmalgamSculkShotEntity;

/**
 * Custom model for the Gloam Eyed Amalgam sculk shot projectile.
 *
 * <p>This baked Java model is adapted from the Blockbench export
 * {@code gloam_eyed_amalgam_sculk_shot.json}.
 */
public class GloamEyedAmalgamSculkShotModel extends EntityModel<GloamEyedAmalgamSculkShotEntity> {

    // Model layer used by the projectile renderer
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "gloam_eyed_amalgam_sculk_shot"),
            "main"
    );

    // Main projectile body part
    private final ModelPart body;

    // Creates the baked projectile model
    public GloamEyedAmalgamSculkShotModel(ModelPart root) {
        this.body = root.getChild("body");
    }

    // Defines the projectile model geometry
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        // CenterBlock: [6,-2,6] to [10,2,10], centered from Blockbench's 8,0,8 origin
                        .texOffs(0, 0)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.ZERO
        );

        // TopBlock: [7,2,7] to [9,3,9], pivot [8,2,8]
        body.addOrReplaceChild(
                "top_block",
                CubeListBuilder.create()
                        .texOffs(8, 8)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F)
        );

        // BottomBlock: [7,-2,7] to [9,-1,9], pivot [8,-2,8]
        body.addOrReplaceChild(
                "bottom_block",
                CubeListBuilder.create()
                        .texOffs(0, 11)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, radians(-177.5F), 0.0F, 0.0F)
        );

        // NorthBlock: [7,0,5] to [9,1,7], pivot [8,0,6]
        body.addOrReplaceChild(
                "north_block",
                CubeListBuilder.create()
                        .texOffs(8, 11)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, radians(-90.0F), 0.0F, 0.0F)
        );

        // SouthBlock: [7,0,9] to [9,1,11], pivot [8,0,10]
        body.addOrReplaceChild(
                "south_block",
                CubeListBuilder.create()
                        .texOffs(0, 14)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, radians(90.0F), 0.0F, 0.0F)
        );

        // WestBlock: [5,0,7] to [7,1,9], pivot [6,0,8]
        body.addOrReplaceChild(
                "west_block",
                CubeListBuilder.create()
                        .texOffs(8, 14)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 0.0F, 0.0F, radians(90.0F))
        );

        // EastBlock: [9,0,7] to [11,1,9], pivot [10,0,8]
        body.addOrReplaceChild(
                "east_block",
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 0.0F, 0.0F, radians(-90.0F))
        );

        return LayerDefinition.create(mesh, 32, 32);
    }

    // Converts Blockbench degree rotations into model radians
    private static float radians(float degrees) {
        return degrees * ((float) Math.PI / 180.0F);
    }

    // Animates the projectile model
    @Override
    public void setupAnim(GloamEyedAmalgamSculkShotEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.body.yRot = ageInTicks * 0.25F;
        this.body.xRot = ageInTicks * 0.15F;
    }

    // Renders the projectile model
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
