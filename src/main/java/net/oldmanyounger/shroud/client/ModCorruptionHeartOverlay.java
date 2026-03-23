package net.oldmanyounger.shroud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.effect.CorruptionMobEffect;
import net.oldmanyounger.shroud.effect.ModMobEffects;

import java.lang.reflect.Field;

@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public final class ModCorruptionHeartOverlay {
    private static final ResourceLocation CORRUPTED_HEART_CONTAINER =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/gui/corrupted_heart_container.png");
    private static final ResourceLocation CORRUPTED_HEART_FULL =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/gui/corrupted_heart_full.png");
    private static final ResourceLocation CORRUPTED_HEART_HALF =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "textures/gui/corrupted_heart_half.png");

    private static final Field LEFT_HEIGHT_FIELD = resolveGuiField("leftHeight");

    private static int cachedHealthLeft = 0;
    private static int cachedHealthTop = 0;
    private static boolean cachedAnchorValid = false;

    private ModCorruptionHeartOverlay() {
    }

    @SubscribeEvent
    public static void onPlayerHealthPre(RenderGuiLayerEvent.Pre event) {
        if (!VanillaGuiLayers.PLAYER_HEALTH.equals(event.getName())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || player.isCreative() || player.isSpectator()) {
            cachedAnchorValid = false;
            return;
        }

        if (!player.hasEffect(ModMobEffects.CORRUPTION)) {
            cachedAnchorValid = false;
            return;
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        cachedHealthLeft = screenWidth / 2 - 91;
        cachedHealthTop = screenHeight - getVanillaLeftHeight(minecraft.gui);
        cachedAnchorValid = true;
    }

    @SubscribeEvent
    public static void onPlayerHealthPost(RenderGuiLayerEvent.Post event) {
        if (!VanillaGuiLayers.PLAYER_HEALTH.equals(event.getName())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || player.isCreative() || player.isSpectator()) {
            return;
        }

        if (!player.hasEffect(ModMobEffects.CORRUPTION) || !cachedAnchorValid) {
            return;
        }

        int usableHeartContainers = Mth.ceil(player.getMaxHealth() / 2.0F);
        int totalHeartContainers = Mth.ceil((float) CorruptionMobEffect.getUncorruptedMaxHealth(player) / 2.0F);
        int blockedHealthPoints = CorruptionMobEffect.getBlockedHealthPoints(player);

        if (blockedHealthPoints <= 0 || totalHeartContainers <= usableHeartContainers) {
            return;
        }

        int rowHeight = Math.max(10 - (Mth.ceil(totalHeartContainers / 10.0F) - 2), 3);

        for (int containerIndex = usableHeartContainers; containerIndex < totalHeartContainers; containerIndex++) {
            int blockedOffset = (containerIndex - usableHeartContainers) * 2;
            int remainingBlockedPoints = blockedHealthPoints - blockedOffset;
            if (remainingBlockedPoints <= 0) {
                break;
            }

            int row = containerIndex / 10;
            int column = containerIndex % 10;

            int x = cachedHealthLeft + column * 8;
            int y = cachedHealthTop - row * rowHeight;

            // Draw the sealed/outlined heart container first so the black border is always present.
            event.getGuiGraphics().blit(CORRUPTED_HEART_CONTAINER, x, y, 0, 0, 9, 9, 9, 9);

            // Then draw the corrupted fill on top.
            if (remainingBlockedPoints >= 2) {
                event.getGuiGraphics().blit(CORRUPTED_HEART_FULL, x, y, 0, 0, 9, 9, 9, 9);
            } else {
                event.getGuiGraphics().blit(CORRUPTED_HEART_HALF, x, y, 0, 0, 9, 9, 9, 9);
            }
        }
    }

    private static Field resolveGuiField(String fieldName) {
        try {
            Field field = Gui.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static int getVanillaLeftHeight(Gui gui) {
        if (LEFT_HEIGHT_FIELD == null) {
            return 39;
        }

        try {
            return LEFT_HEIGHT_FIELD.getInt(gui);
        } catch (IllegalAccessException ignored) {
            return 39;
        }
    }
}