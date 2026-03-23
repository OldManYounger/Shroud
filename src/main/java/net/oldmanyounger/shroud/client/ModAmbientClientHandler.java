package net.oldmanyounger.shroud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.client.audio.ModAmbientLoopSoundInstance;

/**
 * Client-side handler that starts and stops Shroud's ambient Limbo loop based on
 * the local player's current dimension.
 *
 * <p>This class watches client ticks, determines whether the player is inside
 * the Limbo dimension, and ensures that exactly one looping ambient sound
 * instance is active when appropriate. If the player leaves Limbo or the client
 * loses its current player/world context, the handler stops the loop cleanly.
 *
 * <p>In the broader context of the project, this class provides the runtime
 * control layer for dimension-specific ambient sound design, helping custom
 * environments feel distinct and continuously atmospheric without requiring
 * manual triggers in multiple unrelated systems.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public final class ModAmbientClientHandler {

    // Resource key for the Limbo dimension used to decide when the ambient loop should play
    private static final ResourceKey<Level> LIMBO_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "limbo")
    );

    // Currently active ambient loop instance, if one is playing
    private static ModAmbientLoopSoundInstance activeLoop;

    // Prevent instantiation because this class only exposes static client event logic
    private ModAmbientClientHandler() {
    }

    // Checks each client tick whether the ambient Limbo loop should be started or stopped
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        SoundManager soundManager = mc.getSoundManager();

        // Stop any active loop if the player or world context is unavailable
        if (player == null || player.level() == null) {
            stopLoop(soundManager);
            return;
        }

        // Determine whether the player is currently inside the Limbo dimension
        boolean inLimbo = player.level().dimension().equals(LIMBO_DIMENSION);

        // Stop the loop immediately if the player is not in Limbo
        if (!inLimbo) {
            stopLoop(soundManager);
            return;
        }

        // Start the loop only if there is no active instance already playing
        if (activeLoop == null || !soundManager.isActive(activeLoop)) {
            activeLoop = new ModAmbientLoopSoundInstance(player);
            soundManager.play(activeLoop);
        }
    }

    // Stops the current ambient loop and clears the tracked reference
    private static void stopLoop(SoundManager soundManager) {
        if (activeLoop != null) {
            soundManager.stop(activeLoop);
            activeLoop = null;
        }
    }
}