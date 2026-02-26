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

@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public final class ModAmbientClientHandler {

    private static final ResourceKey<Level> LIMBO_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "limbo")
    );

    private static ModAmbientLoopSoundInstance activeLoop;

    private ModAmbientClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        SoundManager soundManager = mc.getSoundManager();

        if (player == null || player.level() == null) {
            stopLoop(soundManager);
            return;
        }

        boolean inLimbo = player.level().dimension().equals(LIMBO_DIMENSION);

        if (!inLimbo) {
            stopLoop(soundManager);
            return;
        }

        // Start if absent or no longer active
        if (activeLoop == null || !soundManager.isActive(activeLoop)) {
            activeLoop = new ModAmbientLoopSoundInstance(player);
            soundManager.play(activeLoop);
        }
    }

    private static void stopLoop(SoundManager soundManager) {
        if (activeLoop != null) {
            soundManager.stop(activeLoop);
            activeLoop = null;
        }
    }
}
