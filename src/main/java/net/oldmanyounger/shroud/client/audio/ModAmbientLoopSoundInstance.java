package net.oldmanyounger.shroud.client.audio;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.oldmanyounger.shroud.sound.ModSounds;

public class ModAmbientLoopSoundInstance extends AbstractTickableSoundInstance {

    private final LocalPlayer player;

    public ModAmbientLoopSoundInstance(LocalPlayer player) {
        super(ModSounds.LIMBO_AMBIENT_LOOP.get(), SoundSource.AMBIENT, RandomSource.create());
        this.player = player;

        this.looping = true;      // continuous loop
        this.delay = 0;
        this.volume = 0.55f;
        this.pitch = 1.0f;

        // Make it listener-relative so it is always centered on the player
        this.relative = true;
        this.attenuation = Attenuation.NONE;
    }

    @Override
    public void tick() {
        if (player == null || player.isRemoved() || player.level() == null) {
            this.stop();
            return;
        }

        // Keep coordinates synced anyway (safe even when relative=true)
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }
}
