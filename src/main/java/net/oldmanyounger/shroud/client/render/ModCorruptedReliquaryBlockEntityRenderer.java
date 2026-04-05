package net.oldmanyounger.shroud.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.oldmanyounger.shroud.block.entity.ModCorruptedReliquaryBlockEntity;

/**
 * Client-side renderer for visualizing Corrupted Reliquary inventory contents above the block.
 *
 * <p>This renderer reads the reliquary block entity slot contents and draws each occupied item
 * as a small display object arranged in a compact top-surface grid. It is purely visual and does
 * not change gameplay state.
 *
 * <p>In the broader context of the project, this class provides the first world-space feedback
 * layer for ritual input buildup, allowing players to visually inspect stored items before any
 * future ritual systems are activated.
 */
public class ModCorruptedReliquaryBlockEntityRenderer implements BlockEntityRenderer<ModCorruptedReliquaryBlockEntity> {

    // ==================================
    //  FIELDS
    // ==================================

    // Number of display columns across the reliquary top
    private static final int DISPLAY_COLUMNS = 8;

    // Spacing between displayed items
    private static final float DISPLAY_SPACING = 0.11F;

    // Vertical offset above block top for displayed items
    private static final float DISPLAY_Y = 1.02F;

    // Uniform item scale used for top display rendering
    private static final float DISPLAY_SCALE = 0.20F;

    // Creates the renderer instance
    public ModCorruptedReliquaryBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    // Renders stored item stacks above the reliquary in an 8x8 grid
    @Override
    public void render(ModCorruptedReliquaryBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        var items = blockEntity.copyItems();

        float start = 0.5F - DISPLAY_SPACING * ((DISPLAY_COLUMNS - 1) * 0.5F);

        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) {
                continue;
            }

            int col = slot % DISPLAY_COLUMNS;
            int row = slot / DISPLAY_COLUMNS;

            float x = start + (col * DISPLAY_SPACING);
            float z = start + (row * DISPLAY_SPACING);

            poseStack.pushPose();
            poseStack.translate(x, DISPLAY_Y, z);
            poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);

            itemRenderer.renderStatic(
                    stack,
                    ItemDisplayContext.GROUND,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    blockEntity.getLevel(),
                    slot
            );

            poseStack.popPose();
        }
    }
}