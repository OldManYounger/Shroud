package net.oldmanyounger.shroud.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.oldmanyounger.shroud.block.entity.ModCorruptedReliquaryBlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side renderer for visualizing Corrupted Reliquary inventory contents above the block.
 *
 * <p>This renderer draws only occupied reliquary slots as an evenly spaced circular ring that
 * rotates around the center of the block. Empty slots are ignored so spacing always adapts to
 * the actual number of displayed items. Items also bob with alternating phase so neighboring
 * entries move in opposite vertical directions.
 *
 * <p>In the broader context of the project, this class provides world-space visual feedback for
 * ritual input buildup, letting players quickly read reliquary contents before ritual systems
 * are fully wired.
 */
public class ModCorruptedReliquaryBlockEntityRenderer implements BlockEntityRenderer<ModCorruptedReliquaryBlockEntity> {

    // ==================================
    //  FIELDS
    // ==================================

    // Base Y height above the reliquary top for item display
    private static final float RING_Y = 1.05F;

    // Minimum ring radius for smaller item counts
    private static final float MIN_RING_RADIUS = 0.20F;

    // Maximum ring radius cap for larger item counts
    private static final float MAX_RING_RADIUS = 0.34F;

    // Per-item radius growth factor used to spread larger rings
    private static final float RING_RADIUS_PER_ITEM = 0.010F;

    // Uniform item scale used for ring rendering
    private static final float ITEM_SCALE = 0.40F;

    // Rotation speed in degrees per tick
    private static final float RING_ROTATION_DEG_PER_TICK = 1.6F;

    // Vertical bobbing amplitude applied per item
    private static final float BOB_AMPLITUDE = 0.035F;

    // Bobbing speed in radians per tick
    private static final float BOB_SPEED_RAD_PER_TICK = 0.11F;

    // Creates the renderer instance
    public ModCorruptedReliquaryBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    // Renders occupied reliquary items in a rotating circular ring with alternating bob motion
    @Override
    public void render(ModCorruptedReliquaryBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        List<ItemStack> occupiedItems = collectOccupiedItems(blockEntity);
        int count = occupiedItems.size();

        if (count <= 0) {
            return;
        }

        var itemRenderer = Minecraft.getInstance().getItemRenderer();

        float gameTime = blockEntity.getLevel() != null
                ? blockEntity.getLevel().getGameTime() + partialTick
                : partialTick;

        float baseAngleDeg = gameTime * RING_ROTATION_DEG_PER_TICK;
        float ringRadius = computeRingRadius(count);
        float stepDeg = 360.0F / count;
        float bobTime = gameTime * BOB_SPEED_RAD_PER_TICK;

        for (int i = 0; i < count; i++) {
            ItemStack stack = occupiedItems.get(i);

            float angleDeg = baseAngleDeg + (stepDeg * i);
            float angleRad = angleDeg * Mth.DEG_TO_RAD;

            float x = 0.5F + (Mth.cos(angleRad) * ringRadius);
            float z = 0.5F + (Mth.sin(angleRad) * ringRadius);

            // Alternates bob phase so every other item moves opposite in vertical motion
            float bobPhase = (i & 1) == 0 ? 0.0F : Mth.PI;
            float bobOffset = Mth.sin(bobTime + bobPhase) * BOB_AMPLITUDE;
            float y = RING_Y + bobOffset;

            poseStack.pushPose();
            poseStack.translate(x, y, z);

            // Rotates each item to face outward from the ring center
            poseStack.mulPose(Axis.YP.rotationDegrees(-angleDeg + 90.0F));

            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);

            itemRenderer.renderStatic(
                    stack,
                    ItemDisplayContext.GROUND,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    blockEntity.getLevel(),
                    i
            );

            poseStack.popPose();
        }
    }

    // ==================================
    //  HELPERS
    // ==================================

    // Collects only non-empty item stacks from reliquary storage
    private List<ItemStack> collectOccupiedItems(ModCorruptedReliquaryBlockEntity blockEntity) {
        List<ItemStack> occupied = new ArrayList<>();
        for (ItemStack stack : blockEntity.copyItems()) {
            if (!stack.isEmpty()) {
                occupied.add(stack);
            }
        }
        return occupied;
    }

    // Computes ring radius based on occupied item count
    private float computeRingRadius(int count) {
        float raw = MIN_RING_RADIUS + (count * RING_RADIUS_PER_ITEM);
        return Mth.clamp(raw, MIN_RING_RADIUS, MAX_RING_RADIUS);
    }
}