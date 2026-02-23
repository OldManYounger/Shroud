package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.sound.ModSounds;

public class ModUpwardParticlePillarBlock extends RotatedPillarBlock {

    // Spew less often: randomized chance window
    private static final int MIN_EMISSION_INTERVAL_TICKS = 80;
    private static final int MAX_EMISSION_INTERVAL_TICKS = 180;

    // Projection range
    private static final int MIN_EMISSION_HEIGHT_BLOCKS = 6;
    private static final int MAX_EMISSION_HEIGHT_BLOCKS = 11;

    // Thickness
    private static final int PARTICLES_PER_LAYER = 6;

    public ModUpwardParticlePillarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        // Randomized interval between two values
        int sampledInterval = random.nextInt(
                MAX_EMISSION_INTERVAL_TICKS - MIN_EMISSION_INTERVAL_TICKS + 1
        ) + MIN_EMISSION_INTERVAL_TICKS;

        if (random.nextInt(sampledInterval) != 0) {
            return;
        }

        int emissionHeight = random.nextInt(
                MAX_EMISSION_HEIGHT_BLOCKS - MIN_EMISSION_HEIGHT_BLOCKS + 1
        ) + MIN_EMISSION_HEIGHT_BLOCKS;

        // Start at block top surface (flush), not floating one extra block above
        spawnThickColumn(level, pos, random, emissionHeight);

        // Sound linked to same spew trigger (no separate timing logic needed)
        playSpewSound(level, pos, random);
    }

    private void spawnThickColumn(Level level, BlockPos pos, RandomSource random, int emissionHeight) {
        for (int step = 0; step < emissionHeight; step++) {
            double y = pos.getY() + 1.0D + step; // top surface of block

            for (int i = 0; i < PARTICLES_PER_LAYER; i++) {
                double radius = 0.48D; // thicker
                double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * radius * 2.0D;
                double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * radius * 2.0D;

                double vx = (random.nextDouble() - 0.5D) * 0.02D;
                double vy = 0.07D + random.nextDouble() * 0.05D;
                double vz = (random.nextDouble() - 0.5D) * 0.02D;

                level.addParticle(ParticleTypes.SCULK_SOUL, x, y, z, vx, vy, vz);
            }
        }
    }

    private void playSpewSound(Level level, BlockPos pos, RandomSource random) {
        float volume = 0.55F + random.nextFloat() * 0.2F;
        float pitch = 0.9F + random.nextFloat() * 0.2F;

        level.playLocalSound(
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D,
                ModSounds.BLOCK_SCULK_EMITTER_SPEW.get(),
                SoundSource.BLOCKS,
                volume,
                pitch,
                false
        );
    }
}
