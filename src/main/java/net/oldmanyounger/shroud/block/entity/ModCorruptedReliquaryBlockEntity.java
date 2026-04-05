package net.oldmanyounger.shroud.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.oldmanyounger.shroud.effect.ModMobEffects;

/**
 * Stores, stabilizes, and releases corruption charge for the Corrupted Reliquary block.
 *
 * <p>This block entity owns the reliquary's persistent runtime state, including buffered
 * corruption charge, pulse cooldown timing, and server-side emission behavior that applies
 * Corruption to nearby players when enough charge has been accumulated.
 *
 * <p>In the broader context of the project, this class serves as the mechanical core for
 * reliquary progression loops by translating world interactions into a persistent corruption
 * reservoir that can drive risk, area pressure, and ritual-style gameplay beats.
 */
public class ModCorruptedReliquaryBlockEntity extends BlockEntity {

    // ==================================
    //  FIELDS
    // ==================================

    // Max corruption the reliquary can hold
    private static final int MAX_CORRUPTION = 1000;

    // Corruption required to trigger one pulse
    private static final int PULSE_COST = 200;

    // Ticks between pulse attempts after a successful pulse
    private static final int PULSE_COOLDOWN_TICKS = 80;

    // Radius around the reliquary affected by a pulse
    private static final double PULSE_RADIUS = 8.0D;

    // Corruption effect duration applied by a pulse
    private static final int CORRUPTION_DURATION_TICKS = 160;

    // Corruption effect amplifier applied by a pulse
    private static final int CORRUPTION_AMPLIFIER = 0;

    // How often idle corruption decays
    private static final int DECAY_INTERVAL_TICKS = 40;

    // Buffered corruption currently stored
    private int storedCorruption = 0;

    // Cooldown before next pulse can occur
    private int pulseCooldown = 0;

    // Tick accumulator used for passive decay pacing
    private int decayTicker = 0;

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Creates the block entity instance for Corrupted Reliquary blocks
    public ModCorruptedReliquaryBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CORRUPTED_RELIQUARY.get(), pos, blockState);
    }

    // ==================================
    //  TICK
    // ==================================

    // Static server tick bridge used by the block ticker hook
    public static void serverTick(Level level, BlockPos pos, BlockState state, ModCorruptedReliquaryBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        blockEntity.serverTickInternal((ServerLevel) level, pos);
    }

    // Runs one server tick of reliquary runtime behavior
    private void serverTickInternal(ServerLevel level, BlockPos pos) {
        if (pulseCooldown > 0) {
            pulseCooldown--;
        }

        if (storedCorruption >= PULSE_COST && pulseCooldown == 0) {
            emitPulse(level, pos);
            storedCorruption -= PULSE_COST;
            pulseCooldown = PULSE_COOLDOWN_TICKS;
            markDirtyAndSync(level, pos);
            return;
        }

        decayTicker++;
        if (decayTicker >= DECAY_INTERVAL_TICKS) {
            decayTicker = 0;
            if (storedCorruption > 0) {
                storedCorruption--;
                markDirtyAndSync(level, pos);
            }
        }
    }

    // ==================================
    //  CORRUPTION STORAGE
    // ==================================

    // Adds corruption to the reliquary and returns the amount accepted
    public int addCorruption(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int capacity = MAX_CORRUPTION - storedCorruption;
        if (capacity <= 0) {
            return 0;
        }

        int accepted = Math.min(amount, capacity);
        storedCorruption += accepted;

        if (level instanceof ServerLevel serverLevel) {
            markDirtyAndSync(serverLevel, worldPosition);
        } else {
            setChanged();
        }

        return accepted;
    }

    // Removes corruption from the reliquary and returns the amount extracted
    public int removeCorruption(int amount) {
        if (amount <= 0 || storedCorruption <= 0) {
            return 0;
        }

        int removed = Math.min(amount, storedCorruption);
        storedCorruption -= removed;

        if (level instanceof ServerLevel serverLevel) {
            markDirtyAndSync(serverLevel, worldPosition);
        } else {
            setChanged();
        }

        return removed;
    }

    // Returns current stored corruption
    public int getStoredCorruption() {
        return storedCorruption;
    }

    // Returns max corruption capacity
    public int getMaxCorruption() {
        return MAX_CORRUPTION;
    }

    // Returns true when enough corruption is buffered to pulse immediately
    public boolean canPulseNow() {
        return storedCorruption >= PULSE_COST && pulseCooldown == 0;
    }

    // ==================================
    //  PULSE LOGIC
    // ==================================

    // Applies corruption and visuals to nearby players
    private void emitPulse(ServerLevel level, BlockPos pos) {
        AABB bounds = new AABB(pos).inflate(PULSE_RADIUS);

        for (Player player : level.getEntitiesOfClass(Player.class, bounds, this::canAffectPlayer)) {
            player.addEffect(new MobEffectInstance(
                    ModMobEffects.CORRUPTION,
                    CORRUPTION_DURATION_TICKS,
                    CORRUPTION_AMPLIFIER,
                    true,
                    true,
                    true
            ));
        }

        level.sendParticles(
                ParticleTypes.SCULK_SOUL,
                pos.getX() + 0.5D,
                pos.getY() + 1.1D,
                pos.getZ() + 0.5D,
                24,
                0.35D,
                0.25D,
                0.35D,
                0.03D
        );

        level.sendParticles(
                ParticleTypes.SCULK_CHARGE_POP,
                pos.getX() + 0.5D,
                pos.getY() + 1.0D,
                pos.getZ() + 0.5D,
                16,
                0.30D,
                0.20D,
                0.30D,
                0.01D
        );
    }

    // Validates whether a player can be affected by reliquary pulses
    private boolean canAffectPlayer(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    // ==================================
    //  PERSISTENCE
    // ==================================

    // Loads persisted reliquary state from NBT
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        storedCorruption = tag.getInt("stored_corruption");
        pulseCooldown = tag.getInt("pulse_cooldown");
        decayTicker = tag.getInt("decay_ticker");
    }

    // Saves reliquary state to NBT
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("stored_corruption", storedCorruption);
        tag.putInt("pulse_cooldown", pulseCooldown);
        tag.putInt("decay_ticker", decayTicker);
    }

    // ==================================
    //  INTERNAL HELPERS
    // ==================================

    // Marks the block entity dirty and pushes a block update for client sync
    private void markDirtyAndSync(ServerLevel level, BlockPos pos) {
        setChanged();
        level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
    }
}