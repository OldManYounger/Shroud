package net.oldmanyounger.shroud.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Client-side handler that tracks when the local player is inside a Shroud
 * portal and renders a fullscreen overlay that fades in during portal use.
 *
 * <p>This class keeps a running count of how long the player has remained inside
 * the custom portal block, resets that state when dimensions change, and uses
 * the result to drive a translucent GUI overlay. The fade timing is aligned with
 * the portal transition window so the visual effect builds as teleportation
 * approaches.
 *
 * <p>In the broader context of the project, this class provides the portal-use
 * feedback layer that helps Shroud's custom portal feel distinct and immersive,
 * reinforcing the mod's dimension travel experience through client-side visual
 * presentation.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public final class ModPortalOverlayHandler {

    // Tracks the last dimension the client was in so portal progress can reset on teleport
    private static ResourceKey<Level> lastDimension = null;

    // Maximum portal buildup time used to scale the overlay fade
    private static final int MAX_PORTAL_TIME = 80;

    // Number of client ticks the player has currently spent inside a Shroud portal
    private static int ticksInShroudPortal = 0;

    // Fullscreen texture used as the portal overlay
    private static final ResourceLocation SHROUD_PORTAL_OVERLAY =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/misc/shroud_portal_overlay.png");

    // Prevent instantiation because this class only exposes static client event handlers
    private ModPortalOverlayHandler() {
    }

    // Updates the player's current portal exposure each client tick
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        // Clear portal state when the client does not currently have a valid player or world
        if (mc.level == null || player == null) {
            ticksInShroudPortal = 0;
            lastDimension = null;
            return;
        }

        // Reset the buildup timer after changing dimensions so the overlay does not persist across teleport
        ResourceKey<Level> currentDim = mc.level.dimension();
        if (lastDimension != currentDim) {
            ticksInShroudPortal = 0;
            lastDimension = currentDim;
        }

        BlockState state = mc.level.getBlockState(player.blockPosition());
        boolean inShroudPortal = state.is(ModBlocks.SCULK_PORTAL.get());

        // Count upward while the player remains inside the portal
        if (inShroudPortal) {
            if (ticksInShroudPortal < MAX_PORTAL_TIME) {
                ticksInShroudPortal++;
            }
        } else {
            // Reset immediately when the player leaves the portal block
            ticksInShroudPortal = 0;
        }
    }

    // Draws the fullscreen portal overlay after the GUI when the player has portal buildup progress
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        // Do nothing if the player is not currently building portal progress
        if (ticksInShroudPortal <= 0) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        // Convert time spent in the portal into a capped overlay alpha value
        float progress = Mth.clamp((float) ticksInShroudPortal / (float) MAX_PORTAL_TIME, 0.0F, 0.8F);
        float alpha = progress;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        poseStack.pushPose();

        // Enable blending so the overlay can fade smoothly over the screen
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Apply the current fade alpha before drawing the overlay texture
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);

        guiGraphics.blit(
                SHROUD_PORTAL_OVERLAY,
                0, 0,
                0.0F, 0.0F,
                width, height,
                256, 256
        );

        // Restore default GUI color and blend state after drawing
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}