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

public class SculkEmitterBlockEntity extends BlockEntity {

    private static final int MIN_EMISSION_INTERVAL_TICKS = 90;
    private static final int MAX_EMISSION_INTERVAL_TICKS = 180;

    private static final int MIN_EMISSION_DISTANCE_BLOCKS = 6;
    private static final int MAX_EMISSION_DISTANCE_BLOCKS = 11;
    private static final int PARTICLES_PER_STEP = 5;

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

        int emissionDistance = random.nextInt(
                MAX_EMISSION_DISTANCE_BLOCKS - MIN_EMISSION_DISTANCE_BLOCKS + 1
        ) + MIN_EMISSION_DISTANCE_BLOCKS;

        Direction facing = getBlockState().getValue(DirectionalBlock.FACING);
        spawnDirectionalJet(serverLevel, worldPosition, facing, random, emissionDistance);
        playSpewSound(serverLevel, worldPosition, random);

        ticksUntilNextSpew = sampleInterval(random);
        setChanged();
    }

    private int sampleInterval(RandomSource random) {
        return random.nextInt(
                MAX_EMISSION_INTERVAL_TICKS - MIN_EMISSION_INTERVAL_TICKS + 1
        ) + MIN_EMISSION_INTERVAL_TICKS;
    }

    private void spawnDirectionalJet(ServerLevel level, BlockPos pos, Direction facing, RandomSource random, int distance) {
        final double sx = pos.getX() + 0.5D;
        final double sy = pos.getY() + 0.5D;
        final double sz = pos.getZ() + 0.5D;

        final double dx = facing.getStepX();
        final double dy = facing.getStepY();
        final double dz = facing.getStepZ();

        for (int step = 1; step <= distance; step++) {
            double bx = sx + dx * step;
            double by = sy + dy * step;
            double bz = sz + dz * step;

            for (int i = 0; i < PARTICLES_PER_STEP; i++) {
                double spread = 0.28D;

                // jitter orthogonal to facing axis
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

                // velocity biased in facing direction
                double vx = (random.nextDouble() - 0.5D) * 0.02D + dx * (0.08D + random.nextDouble() * 0.04D);
                double vy = (random.nextDouble() - 0.5D) * 0.02D + dy * (0.08D + random.nextDouble() * 0.04D);
                double vz = (random.nextDouble() - 0.5D) * 0.02D + dz * (0.08D + random.nextDouble() * 0.04D);

                level.sendParticles(ParticleTypes.SCULK_SOUL, px, py, pz, 1, vx, vy, vz, 0.0D);
            }
        }
    }

    private void playSpewSound(ServerLevel level, BlockPos pos, RandomSource random) {
        float volume = 0.55F + random.nextFloat() * 0.2F;
        float pitch = 0.9F + random.nextFloat() * 0.2F;

        level.playSound(null, pos, ModSounds.BLOCK_SCULK_EMITTER_SPEW.get(), SoundSource.BLOCKS, volume, pitch);
    }
}