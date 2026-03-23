package net.oldmanyounger.shroud.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.PlayerHeartTypeEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.effect.ModMobEffects;

import java.util.Objects;

/**
 * Handles Corruption heart visuals for both vanilla rendering and Colorful Hearts compatibility.
 *
 * <p>When Colorful Hearts is not installed, this class uses the normal
 * {@link PlayerHeartTypeEvent} path so vanilla heart rendering swaps to the custom
 * corruption heart type directly.
 *
 * <p>When Colorful Hearts is installed, vanilla heart rendering is replaced before
 * heart types matter, so this class instead captures the heart HUD position during
 * the health-layer pre-pass and then draws a second corruption overlay pass on top
 * of Colorful Hearts' rendered hearts during the final GUI render stage.
 *
 * <p>In the broader context of the project, this class is the compatibility bridge
 * that keeps Corruption's visual heart effect working whether the player is using
 * vanilla hearts or a third-party custom heart renderer.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public final class ModCorruptionHeartRenderHandler {

    // Shared client instance used for HUD state and player access
    private static final Minecraft CLIENT = Minecraft.getInstance();

    // Whether Colorful Hearts is loaded and therefore replacing vanilla heart rendering
    private static final boolean COLORFUL_HEARTS_LOADED = ModList.get().isLoaded("colorfulhearts");

    // State used to mirror vanilla-style heart blinking behavior
    private static long lastHealthTime;
    private static long healthBlinkTime;
    private static int displayHealth;
    private static int lastHealth;

    // Cached overlay render data captured before Colorful Hearts draws the health bar
    private static boolean queuedOverlay;
    private static int queuedLeft;
    private static int queuedTop;
    private static int queuedMaxHealth;
    private static int queuedHealth;
    private static int queuedDisplayHealth;
    private static boolean queuedHighlight;

    // Prevent instantiation because this class only exposes static event logic
    private ModCorruptionHeartRenderHandler() {
    }

    // Uses the vanilla heart-type path when Colorful Hearts is not present
    @SubscribeEvent
    public static void onPlayerHeartType(PlayerHeartTypeEvent event) {
        if (COLORFUL_HEARTS_LOADED) {
            return;
        }

        if (!event.getEntity().hasEffect(ModMobEffects.CORRUPTION)) {
            return;
        }

        event.setType(ModCorruptionHeartTypes.corrupted());
    }

    // Captures heart HUD positioning before Colorful Hearts replaces the health-layer rendering
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPreRenderPlayerHealth(RenderGuiLayerEvent.Pre event) {
        queuedOverlay = false;

        if (!COLORFUL_HEARTS_LOADED
                || event.isCanceled()
                || CLIENT.options.hideGui
                || !event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)
                || !Objects.requireNonNull(CLIENT.gameMode).canHurtPlayer()
                || !(CLIENT.getCameraEntity() instanceof Player player)
                || !player.hasEffect(ModMobEffects.CORRUPTION)) {
            return;
        }

        int health = Mth.ceil(player.getHealth());

        long tickCount = CLIENT.gui.getGuiTicks();
        boolean highlight = healthBlinkTime > tickCount && (healthBlinkTime - tickCount) / 3L % 2L == 1L;

        // Mirror vanilla and Colorful Hearts blink timing rules so the overlay matches heart flashing
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

        // Match Colorful Hearts' max-health calculation so the overlay lines up with its final heart count
        int maxHealth = Mth.ceil(Math.max((float) player.getAttributeValue(Attributes.MAX_HEALTH), Math.max(displayHealth, health)));

        GuiGraphics guiGraphics = event.getGuiGraphics();

        queuedLeft = guiGraphics.guiWidth() / 2 - 91;
        queuedTop = guiGraphics.guiHeight() - CLIENT.gui.leftHeight;
        queuedMaxHealth = maxHealth;
        queuedHealth = health;
        queuedDisplayHealth = displayHealth;
        queuedHighlight = highlight;
        queuedOverlay = true;
    }

    // Draws the corruption overlay on top of Colorful Hearts after the GUI has finished rendering
    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        if (!queuedOverlay || !COLORFUL_HEARTS_LOADED) {
            return;
        }

        if (!(CLIENT.getCameraEntity() instanceof Player player) || !player.hasEffect(ModMobEffects.CORRUPTION)) {
            queuedOverlay = false;
            return;
        }

        ModCorruptionHeartOverlayRenderer.renderPlayerHearts(
                event.getGuiGraphics(),
                queuedLeft,
                queuedTop,
                queuedMaxHealth,
                queuedHealth,
                queuedDisplayHealth,
                queuedHighlight
        );

        queuedOverlay = false;
    }
}