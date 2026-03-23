package net.oldmanyounger.shroud.client.audio;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.oldmanyounger.shroud.sound.ModSounds;

/**
 * Client-side looping sound instance used to play Shroud's Limbo ambient track
 * continuously while the player remains in the appropriate environment.
 *
 * <p>This class wraps the ambient loop sound in a tickable sound instance so it
 * can follow the local player and automatically stop itself if the player or
 * world reference becomes invalid. The sound is configured as listener-relative,
 * which keeps it centered on the player instead of behaving like a distant
 * world-positioned source.
 *
 * <p>In the broader context of the project, this class provides the low-level
 * audio object used by the ambient client handler to make the Limbo dimension
 * feel persistent, immersive, and location-aware without requiring repeated
 * one-shot sound playback.
 */
public class ModAmbientLoopSoundInstance extends AbstractTickableSoundInstance {

    // Local player that owns and carries this ambient loop
    private final LocalPlayer player;

    // Creates a looping ambient sound instance bound to the given local player
    public ModAmbientLoopSoundInstance(LocalPlayer player) {
        super(ModSounds.LIMBO_AMBIENT_LOOP.get(), SoundSource.AMBIENT, RandomSource.create());
        this.player = player;

        // Configure the sound as an always-running loop with no startup delay
        this.looping = true;
        this.delay = 0;
        this.volume = 0.55f;
        this.pitch = 1.0f;

        // Keep the sound centered on the listener rather than attenuating by world distance
        this.relative = true;
        this.attenuation = Attenuation.NONE;
    }

    // Keeps the sound synchronized with the player and stops it if the player context becomes invalid
    @Override
    public void tick() {
        // Shut down the loop if the player or world is no longer valid
        if (player == null || player.isRemoved() || player.level() == null) {
            this.stop();
            return;
        }

        // Keep the backing coordinates aligned with the player position
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }
}