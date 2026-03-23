package net.oldmanyounger.shroud.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.sound.ModSounds;

/**
 * Block entity that periodically emits a directional burst of sculk particles
 * and a matching sound effect from the attached emitter block.
 *
 * <p>This class handles the timed behavior behind the sculk emitter block by
 * waiting for a randomized interval, then spawning a jet of particles outward
 * in the block's facing direction. The result is a more organic ambient effect
 * than a fixed constant emitter, helping the block feel alive and reactive
 * within the environment.
 *
 * <p>In the broader context of the project, this block entity provides one of
 * Shroud's atmospheric world-detail systems, supporting environmental props and
 * biome dressing that reinforce the mod's sculk-infested tone through motion,
 * sound, and direction-aware effects.
 */
public class ModSculkEmitterBlockEntity extends BlockEntity {

    // Minimum delay before the next emission cycle can occur
    private static final int MIN_EMISSION_INTERVAL_TICKS = 90;

    // Maximum delay before the next emission cycle can occur
    private static final int MAX_EMISSION_INTERVAL_TICKS = 180;

    // Minimum number of blocks the particle jet can travel
    private static final int MIN_EMISSION_DISTANCE_BLOCKS = 6;

    // Maximum number of blocks the particle jet can travel
    private static final int MAX_EMISSION_DISTANCE_BLOCKS = 11;

    // Number of particles spawned at each step of the directional jet
    private static final int PARTICLES_PER_STEP = 5;

    // Countdown until the next particle emission event
    private int ticksUntilNextSpew = -1;

    // Creates the sculk emitter block entity for the registered emitter block
    public ModSculkEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SCULK_EMITTER.get(), pos, state);
    }

    // Server-side tick entry point that waits for the next emission cycle, then spawns particles and sound
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        RandomSource random = serverLevel.getRandom();

        // Initialize the first randomized wait period after placement or load
        if (ticksUntilNextSpew < 0) {
            ticksUntilNextSpew = sampleInterval(random);
            return;
        }

        // Continue counting down until the next emission event
        if (ticksUntilNextSpew > 0) {
            ticksUntilNextSpew--;
            return;
        }

        // Choose a randomized travel distance for this emission burst
        int emissionDistance = random.nextInt(
                MAX_EMISSION_DISTANCE_BLOCKS - MIN_EMISSION_DISTANCE_BLOCKS + 1
        ) + MIN_EMISSION_DISTANCE_BLOCKS;

        // Emit particles in the direction the block is facing
        Direction facing = getBlockState().getValue(DirectionalBlock.FACING);
        spawnDirectionalJet(serverLevel, worldPosition, facing, random, emissionDistance);

        // Play the associated spew sound effect from the emitter's position
        playSpewSound(serverLevel, worldPosition, random);

        // Schedule the next randomized interval
        ticksUntilNextSpew = sampleInterval(random);
        setChanged();
    }

    // Samples a randomized delay between the configured minimum and maximum interval values
    private int sampleInterval(RandomSource random) {
        return random.nextInt(
                MAX_EMISSION_INTERVAL_TICKS - MIN_EMISSION_INTERVAL_TICKS + 1
        ) + MIN_EMISSION_INTERVAL_TICKS;
    }

    // Spawns a line of sculk soul particles extending outward along the block's facing direction
    private void spawnDirectionalJet(ServerLevel level, BlockPos pos, Direction facing, RandomSource random, int distance) {
        final double sx = pos.getX() + 0.5D;
        final double sy = pos.getY() + 0.5D;
        final double sz = pos.getZ() + 0.5D;

        final double dx = facing.getStepX();
        final double dy = facing.getStepY();
        final double dz = facing.getStepZ();

        // Step outward from the emitter and place a small cluster of particles at each distance increment
        for (int step = 1; step <= distance; step++) {
            double bx = sx + dx * step;
            double by = sy + dy * step;
            double bz = sz + dz * step;

            for (int i = 0; i < PARTICLES_PER_STEP; i++) {
                double spread = 0.28D;

                // Apply randomized spread perpendicular to the active facing axis
                double px = bx, py = by, pz = bz;
                if (facing.getAxis() == Direction.Axis.X) {
                    py += (random.nextDouble() - 0.5D) * spread * 2.0D;
                    pz += (random.nextDouble() - 0.5D) * spread * 2.0D;
                } else if (facing.getAxis() == Direction.Axis.Y) {
                    px += (random.nextDouble() - 0.5D) * spread * 2.0D;
                    pz += (random.nextDouble() - 0.5D) * spread * 2.0D;
                } else {
                    px += (random.nextDouble() - 0.5D) * spread * 2.0D;
                    py += (random.nextDouble() - 0.5D) * spread * 2.0D;
                }

                // Bias the particle velocity in the block's facing direction
                double vx = (random.nextDouble() - 0.5D) * 0.02D + dx * (0.08D + random.nextDouble() * 0.04D);
                double vy = (random.nextDouble() - 0.5D) * 0.02D + dy * (0.08D + random.nextDouble() * 0.04D);
                double vz = (random.nextDouble() - 0.5D) * 0.02D + dz * (0.08D + random.nextDouble() * 0.04D);

                level.sendParticles(ParticleTypes.SCULK_SOUL, px, py, pz, 1, vx, vy, vz, 0.0D);
            }
        }
    }

    // Plays the sculk emitter's spew sound with slight randomized pitch and volume variation
    private void playSpewSound(ServerLevel level, BlockPos pos, RandomSource random) {
        float volume = 0.55F + random.nextFloat() * 0.2F;
        float pitch = 0.9F + random.nextFloat() * 0.2F;

        level.playSound(null, pos, ModSounds.BLOCK_SCULK_EMITTER_SPEW.get(), SoundSource.BLOCKS, volume, pitch);
    }
}