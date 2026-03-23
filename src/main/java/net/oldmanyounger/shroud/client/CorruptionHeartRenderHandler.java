package net.oldmanyounger.shroud.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.effect.CorruptionMobEffect;
import net.oldmanyounger.shroud.effect.ModMobEffects;

import java.util.Objects;

@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public final class CorruptionHeartRenderHandler {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    private static long lastHealthTime;
    private static long healthBlinkTime;
    private static int displayHealth;
    private static int lastHealth;

    private CorruptionHeartRenderHandler() {
    }

    @SubscribeEvent
    public static void onRenderPlayerHealth(RenderGuiLayerEvent.Pre event) {
        if (event.isCanceled()
                || CLIENT.options.hideGui
                || !event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)
                || !Objects.requireNonNull(CLIENT.gameMode).canHurtPlayer()
                || !(CLIENT.getCameraEntity() instanceof Player player)
                || !player.hasEffect(ModMobEffects.CORRUPTION)) {
            return;
        }

        CLIENT.getProfiler().push("shroud_corruption_health");

        int absorption = Mth.ceil(player.getAbsorptionAmount());
        int health = Mth.ceil(player.getHealth());

        long tickCount = CLIENT.gui.getGuiTicks();
        boolean highlight = healthBlinkTime > tickCount && (healthBlinkTime - tickCount) / 3L % 2L == 1L;

        if (health < lastHealth && player.invulnerableTime > 0) {
            lastHealthTime = Util.getMillis();
            healthBlinkTime = tickCount + 20L;
        } else if (health > lastHealth && player.invulnerableTime > 0) {
            lastHealthTime = Util.getMillis();
            healthBlinkTime = tickCount + 10L;
        }

        if (Util.getMillis() - lastHealthTime > 1000L) {
            displayHealth = health;
            lastHealthTime = Util.getMillis();
        }

        lastHealth = health;

        int maxHealth = Mth.ceil((float) CorruptionMobEffect.getUncorruptedMaxHealth(player));
        int allowedHealth = Mth.ceil(CorruptionMobEffect.getAllowedHealth(player));

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        int left = width / 2 - 91;
        int top = height - CLIENT.gui.leftHeight;

        boolean hasAbsorptionRow = (absorption + Math.min(20, maxHealth == 19 ? 20 : maxHealth)) > 20;
        int offset = 10 + (hasAbsorptionRow ? 10 : 0);
        CLIENT.gui.leftHeight += offset;

        CorruptionHeartRenderer.renderPlayerHearts(
                guiGraphics,
                player,
                left,
                top,
                maxHealth,
                allowedHealth,
                health,
                displayHealth,
                highlight
        );

        CLIENT.getProfiler().pop();
        event.setCanceled(true);
    }
}