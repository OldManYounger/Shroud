package net.oldmanyounger.shroud.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.oldmanyounger.shroud.Shroud;

public final class CorruptionHeartRenderer {
    private static final ResourceLocation CORRUPTION_CONTAINER =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/gui/sprites/hud/heart/corrupted_heart_container.png");

    private static final ResourceLocation CORRUPTION_FULL =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/gui/sprites/hud/heart/corrupted_heart_full.png");

    private static final ResourceLocation CORRUPTION_HALF =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/gui/sprites/hud/heart/corrupted_heart_half.png");

    private static final ResourceLocation CORRUPTION_FULL_BLINKING =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/gui/sprites/hud/heart/corrupted_heart_full_blinking.png");

    private static final ResourceLocation CORRUPTION_HALF_BLINKING =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/gui/sprites/hud/heart/corrupted_heart_half_blinking.png");

    private CorruptionHeartRenderer() {
    }

    public static void renderPlayerHearts(
            GuiGraphics guiGraphics,
            Player player,
            int left,
            int top,
            int maxHealth,
            int allowedHealth,
            int currentHealth,
            int displayHealth,
            boolean highlight
    ) {
        int totalHeartSlots = Mth.ceil(maxHealth / 2.0F);
        int allowedHeartSlots = Mth.ceil(allowedHealth / 2.0F);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 0.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (int heartIndex = totalHeartSlots - 1; heartIndex >= 0; --heartIndex) {
            int row = heartIndex / 10;
            int column = heartIndex % 10;

            int x = left + column * 8;
            int y = top - row * 10;

            if (heartIndex >= allowedHeartSlots) {
                drawBlockedHeart(guiGraphics, x, y);
                continue;
            }

            drawHeart(guiGraphics, x, y, currentHealth, displayHealth, heartIndex, highlight);
        }

        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();
    }

    private static void drawBlockedHeart(GuiGraphics guiGraphics, int x, int y) {
        blit(guiGraphics, CORRUPTION_CONTAINER, x, y);
        blit(guiGraphics, CORRUPTION_FULL, x, y);
    }

    private static void drawHeart(
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

        blit(guiGraphics, CORRUPTION_CONTAINER, x, y);

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

        if (currentHealth >= hpForFull) {
            blit(guiGraphics, CORRUPTION_FULL, x, y);
        } else if (currentHealth == hpForHalf) {
            blit(guiGraphics, CORRUPTION_HALF, x, y);
        }
    }

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