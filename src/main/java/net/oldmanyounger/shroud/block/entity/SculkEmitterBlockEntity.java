package net.oldmanyounger.shroud.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.sound.ModSounds;

public class SculkEmitterBlockEntity extends BlockEntity {

    // Hard bounds between spews (ticks)
    private static final int MIN_EMISSION_INTERVAL_TICKS = 90;
    private static final int MAX_EMISSION_INTERVAL_TICKS = 180;

    private static final int MIN_EMISSION_HEIGHT_BLOCKS = 6;
    private static final int MAX_EMISSION_HEIGHT_BLOCKS = 11;
    private static final int PARTICLES_PER_LAYER = 5;

    private int ticksUntilNextSpew = -1;

    public SculkEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SCULK_EMITTER.get(), pos, state);
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        RandomSource random = serverLevel.getRandom();

        if (ticksUntilNextSpew < 0) {
            ticksUntilNextSpew = sampleInterval(random);
            return;
        }

        if (ticksUntilNextSpew > 0) {
            ticksUntilNextSpew--;
            return;
        }

        // Spew now
        int emissionHeight = random.nextInt(
                MAX_EMISSION_HEIGHT_BLOCKS - MIN_EMISSION_HEIGHT_BLOCKS + 1
        ) + MIN_EMISSION_HEIGHT_BLOCKS;

        spawnThickColumn(serverLevel, worldPosition, random, emissionHeight);
        playSpewSound(serverLevel, worldPosition, random);

        // Reset timer using hard-bounded random interval
        ticksUntilNextSpew = sampleInterval(random);
        setChanged();
    }

    private int sampleInterval(RandomSource random) {
        return random.nextInt(
                MAX_EMISSION_INTERVAL_TICKS - MIN_EMISSION_INTERVAL_TICKS + 1
        ) + MIN_EMISSION_INTERVAL_TICKS;
    }

    private void spawnThickColumn(ServerLevel level, BlockPos pos, RandomSource random, int emissionHeight) {
        for (int step = 0; step < emissionHeight; step++) {
            double y = pos.getY() + 1.0D + step; // top surface, flush

            for (int i = 0; i < PARTICLES_PER_LAYER; i++) {
                double radius = 0.42D;
                double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * radius * 2.0D;
                double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * radius * 2.0D;

                double vx = (random.nextDouble() - 0.5D) * 0.02D;
                double vy = 0.06D + random.nextDouble() * 0.04D;
                double vz = (random.nextDouble() - 0.5D) * 0.02D;

                level.sendParticles(ParticleTypes.SCULK_SOUL, x, y, z, 1, vx, vy, vz, 0.0D);
            }
        }
    }

    private void playSpewSound(ServerLevel level, BlockPos pos, RandomSource random) {
        float volume = 0.55F + random.nextFloat() * 0.2F;
        float pitch = 0.9F + random.nextFloat() * 0.2F;

        level.playSound(
                null,
                pos,
                ModSounds.BLOCK_SCULK_EMITTER_SPEW.get(),
                SoundSource.BLOCKS,
                volume,
                pitch
        );
    }
}
