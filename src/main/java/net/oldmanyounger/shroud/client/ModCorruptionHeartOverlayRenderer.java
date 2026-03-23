package net.oldmanyounger.shroud.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.oldmanyounger.shroud.Shroud;

/**
 * Renders the Corruption texture layer on top of already-rendered heart icons.
 *
 * <p>This renderer is used specifically for compatibility with custom heart HUD
 * implementations such as Colorful Hearts. Instead of replacing the heart layout,
 * it draws the existing corruption textures over the heart positions that were
 * already rendered by the other mod, preserving that mod's heart count and layout
 * while still visually marking the hearts as corrupted.
 *
 * <p>In the broader context of the project, this class is the client-side overlay
 * renderer that lets Corruption coexist with third-party heart renderers instead
 * of requiring full control over the health HUD.
 */
public final class ModCorruptionHeartOverlayRenderer {

    // Base empty-heart corruption frame used to mark all visible health slots
    private static final ResourceLocation CORRUPTION_CONTAINER =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/gui/sprites/hud/heart/corrupted_heart_container.png");

    // Filled full-heart corruption overlay
    private static final ResourceLocation CORRUPTION_FULL =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/gui/sprites/hud/heart/corrupted_heart_full.png");

    // Filled half-heart corruption overlay
    private static final ResourceLocation CORRUPTION_HALF =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/gui/sprites/hud/heart/corrupted_heart_half.png");

    // Blinking full-heart corruption overlay
    private static final ResourceLocation CORRUPTION_FULL_BLINKING =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/gui/sprites/hud/heart/corrupted_heart_full_blinking.png");

    // Blinking half-heart corruption overlay
    private static final ResourceLocation CORRUPTION_HALF_BLINKING =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/gui/sprites/hud/heart/corrupted_heart_half_blinking.png");

    // Prevent instantiation because this class only exposes static rendering helpers
    private ModCorruptionHeartOverlayRenderer() {
    }

    // Draws the corruption overlay over the currently visible health hearts
    public static void renderPlayerHearts(
            GuiGraphics guiGraphics,
            int left,
            int top,
            int maxHealth,
            int currentHealth,
            int displayHealth,
            boolean highlight
    ) {
        int totalHeartSlots = Mth.ceil(maxHealth / 2.0F);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Draw from highest index downward to match vanilla-style heart ordering
        for (int heartIndex = totalHeartSlots - 1; heartIndex >= 0; --heartIndex) {
            int row = heartIndex / 10;
            int column = heartIndex % 10;

            int x = left + column * 8;
            int y = top - row * 10;

            drawHeartOverlay(guiGraphics, x, y, currentHealth, displayHealth, heartIndex, highlight);
        }

        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();
    }

    // Draws the corruption frame plus the current filled state for one heart slot
    private static void drawHeartOverlay(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int currentHealth,
            int displayHealth,
            int heartIndex,
            boolean highlight
    ) {
        int hpForHalf = heartIndex * 2 + 1;
        int hpForFull = heartIndex * 2 + 2;

        // Always draw the corruption container so empty hearts still look corrupted
        blit(guiGraphics, CORRUPTION_CONTAINER, x, y);

        // During blink windows, draw the blinking full or half overlay based on display health
        if (highlight) {
            if (displayHealth >= hpForFull) {
                blit(guiGraphics, CORRUPTION_FULL_BLINKING, x, y);
                return;
            }

            if (displayHealth == hpForHalf) {
                blit(guiGraphics, CORRUPTION_HALF_BLINKING, x, y);
                return;
            }
        }

        // Otherwise draw the current health fill overlay
        if (currentHealth >= hpForFull) {
            blit(guiGraphics, CORRUPTION_FULL, x, y);
        } else if (currentHealth == hpForHalf) {
            blit(guiGraphics, CORRUPTION_HALF, x, y);
        }
    }

    // Draws one 9x9 heart sprite at the supplied GUI position
    private static void blit(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y) {
        guiGraphics.blit(
                texture,
                x, y,
                0.0F, 0.0F,
                9, 9,
                9, 9
        );
    }
}