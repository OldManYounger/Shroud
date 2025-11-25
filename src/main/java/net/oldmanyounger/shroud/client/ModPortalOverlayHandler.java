package net.oldmanyounger.shroud.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Client-side handler that renders a Shroud-specific portal overlay
 * while the local player is inside a Shroud portal.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public final class ModPortalOverlayHandler {
    /** Tracks the last dimension the client was in, for resetting on teleport */
    private static ResourceKey<Level> lastDimension = null;

    /** Match your portal transition time (ticks) so fade aligns with teleport */
    private static final int MAX_PORTAL_TIME = 80;

    /** Tracks how many ticks the local player has been in a Shroud portal */
    private static int ticksInShroudPortal = 0;

    /** Texture drawn over the screen when inside the Shroud portal */
    private static final ResourceLocation SHROUD_PORTAL_OVERLAY =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/misc/shroud_portal_overlay.png");

    private ModPortalOverlayHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (mc.level == null || player == null) {
            ticksInShroudPortal = 0;
            lastDimension = null;
            return;
        }

        // Reset the overlay when a dimension change occurs (e.g. after teleport)
        ResourceKey<Level> currentDim = mc.level.dimension();
        if (lastDimension != currentDim) {
            ticksInShroudPortal = 0;
            lastDimension = currentDim;
        }

        BlockState state = mc.level.getBlockState(player.blockPosition());
        boolean inShroudPortal = state.is(ModBlocks.SCULK_PORTAL.get());

        if (inShroudPortal) {
            if (ticksInShroudPortal < MAX_PORTAL_TIME) {
                ticksInShroudPortal++;
            }
        } else {
            ticksInShroudPortal = 0;
        }
    }


    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (ticksInShroudPortal <= 0) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        float progress = Mth.clamp((float) ticksInShroudPortal / (float) MAX_PORTAL_TIME, 0.0F, 0.8F);
        float alpha = progress;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);

        guiGraphics.blit(
                SHROUD_PORTAL_OVERLAY,
                0, 0,
                0.0F, 0.0F,
                width, height,
                256, 256
        );

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

}
