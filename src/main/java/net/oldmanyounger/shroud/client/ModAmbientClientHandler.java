package net.oldmanyounger.shroud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.oldmanyounger.shroud.sound.ModSounds;

@EventBusSubscriber(modid = "shroud", value = Dist.CLIENT)
public final class ModAmbientClientHandler {

    // Change this if your dimension id is different.
    private static final ResourceKey<Level> LIMBO_DIMENSION = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("shroud", "limbo")
    );

    private static boolean playing = false;
    private static int replayDelayTicks = 0;

    private ModAmbientClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        LocalPlayer player = mc.player;
        if (player == null || player.level() == null) {
            playing = false;
            replayDelayTicks = 0;
            return;
        }

        boolean inLimbo = player.level().dimension().equals(LIMBO_DIMENSION);

        if (!inLimbo) {
            // hard reset when leaving limbo
            playing = false;
            replayDelayTicks = 0;
            return;
        }

        // optional delay between restarts if track ends
        if (replayDelayTicks > 0) {
            replayDelayTicks--;
            return;
        }

        // If not currently playing, trigger one instance for this player.
        // The sound file itself should be authored as a seamless loop if desired.
        if (!playing) {
            player.playNotifySound(
                    ModSounds.LIMBO_AMBIENT_LOOP.get(),
                    SoundSource.AMBIENT,
                    0.55f, // volume
                    1.0f   // pitch
            );

            playing = true;

            // Fallback replay timer if you want auto-retrigger:
            // set near your audio length in ticks (20 ticks = 1 second).
            // Example for 120s track:
            replayDelayTicks = 20 * 120;
        }
    }
}
