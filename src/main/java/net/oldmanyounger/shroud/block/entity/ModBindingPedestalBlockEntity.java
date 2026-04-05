package net.oldmanyounger.shroud.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Block entity that stores and manages one mob bound to a Binding Pedestal.
 *
 * <p>This block entity automatically captures a mob when one enters pedestal space, persists
 * the bound mob identity and snapshot metadata, and keeps the bound mob positioned on top of
 * the pedestal each server tick. It intentionally focuses on pedestal behavior and does not
 * execute ritual crafting.
 *
 * <p>In the broader context of the project, this class provides the mob-input anchor needed for
 * future ritual validation flows while remaining usable as a standalone world mechanic first.
 */
public class ModBindingPedestalBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {

    // ==================================
    //  FIELDS
    // ==================================

    // NBT key for persisted bound mob UUID
    private static final String TAG_BOUND_MOB_UUID = "BoundMobUuid";

    // NBT key for persisted bound mob type id string
    private static final String TAG_BOUND_MOB_TYPE_ID = "BoundMobTypeId";

    // NBT key for persisted last known bound mob runtime entity id
    private static final String TAG_LAST_KNOWN_ENTITY_ID = "LastKnownEntityId";

    // NBT key for persisted last known bound mob health
    private static final String TAG_LAST_KNOWN_HEALTH = "LastKnownHealth";

    // NBT key for persisted last known bound mob max health
    private static final String TAG_LAST_KNOWN_MAX_HEALTH = "LastKnownMaxHealth";

    // NBT key for persisted ritual lock state
    private static final String TAG_RITUAL_LOCKED = "RitualLocked";

    // Y offset where bound mobs are held above pedestal top
    private static final double HOLD_Y_OFFSET = 1.01D;

    // Minimum change threshold before health sync updates are pushed
    private static final float HEALTH_SYNC_EPSILON = 0.01F;

    // Runtime cached UUID of the bound mob
    @Nullable
    private UUID boundMobUuid = null;

    // Persisted last known bound mob type id
    private String boundMobTypeId = "";

    // Persisted last known runtime entity id
    private int lastKnownEntityId = -1;

    // Persisted last known health snapshot
    private float lastKnownHealth = 0.0F;

    // Persisted last known max health snapshot
    private float lastKnownMaxHealth = 0.0F;

    // Ritual lock scaffold for future integration
    private boolean ritualLocked = false;

    // Creates the binding pedestal block entity
    public ModBindingPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BINDING_PEDESTAL.get(), pos, state);
    }

    // ==================================
    //  BINDING API
    // ==================================

    // Tries to bind a mob to this pedestal
    public boolean tryBindEntity(Entity entity) {
        if (level == null || level.isClientSide) return false;
        if (ritualLocked) return false;
        if (boundMobUuid != null) return false;
        if (!(entity instanceof Mob mob)) return false;
        if (!mob.isAlive() || mob.isRemoved()) return false;

        this.boundMobUuid = mob.getUUID();
        snapshotBoundEntity(mob);
        markChangedAndSync();
        return true;
    }

    // Releases any currently bound mob and clears stored snapshot metadata
    public void releaseBoundMob() {
        if (boundMobUuid == null
                && boundMobTypeId.isEmpty()
                && lastKnownEntityId == -1
                && lastKnownHealth == 0.0F
                && lastKnownMaxHealth == 0.0F) {
            return;
        }

        this.boundMobUuid = null;
        this.boundMobTypeId = "";
        this.lastKnownEntityId = -1;
        this.lastKnownHealth = 0.0F;
        this.lastKnownMaxHealth = 0.0F;
        markChangedAndSync();
    }

    // Returns true when this pedestal currently has a bound mob UUID
    public boolean hasBoundMob() {
        return boundMobUuid != null;
    }

    // Returns last known bound mob type id string
    public String getBoundMobTypeId() {
        return boundMobTypeId;
    }

    // Returns last known runtime entity id
    public int getLastKnownEntityId() {
        return lastKnownEntityId;
    }

    // Returns last known bound mob health snapshot
    public float getLastKnownHealth() {
        return lastKnownHealth;
    }

    // Returns last known bound mob max health snapshot
    public float getLastKnownMaxHealth() {
        return lastKnownMaxHealth;
    }

    // Returns current ritual lock state scaffold
    public boolean isRitualLocked() {
        return ritualLocked;
    }

    // Sets current ritual lock state scaffold
    public void setRitualLocked(boolean ritualLocked) {
        if (this.ritualLocked == ritualLocked) return;

        this.ritualLocked = ritualLocked;
        markChangedAndSync();
    }

    // ==================================
    //  TICK
    // ==================================

    // Server-side pedestal tick that keeps a bound mob held at the pedestal top
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (boundMobUuid == null) return;

        Entity entity = resolveBoundEntity(serverLevel);
        if (!(entity instanceof Mob mob) || !mob.isAlive() || mob.isRemoved()) {
            releaseBoundMob();
            return;
        }

        holdMobOnPedestal(mob);

        boolean snapshotChanged = snapshotBoundEntity(mob);
        if (snapshotChanged) {
            markChangedAndSync();
        }
    }

    // ==================================
    //  NETWORK SYNC
    // ==================================

    // Sends block entity update packets to clients for visible state refresh
    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Provides update tag data for client-side block entity synchronization
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        return tag;
    }

    // Applies update tag data on client
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        this.loadAdditional(tag, registries);
    }

    // ==================================
    //  PERSISTENCE
    // ==================================

    // Saves pedestal state and bound mob snapshot metadata
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (boundMobUuid != null) {
            tag.putUUID(TAG_BOUND_MOB_UUID, boundMobUuid);
        }

        tag.putString(TAG_BOUND_MOB_TYPE_ID, boundMobTypeId);
        tag.putInt(TAG_LAST_KNOWN_ENTITY_ID, lastKnownEntityId);
        tag.putFloat(TAG_LAST_KNOWN_HEALTH, lastKnownHealth);
        tag.putFloat(TAG_LAST_KNOWN_MAX_HEALTH, lastKnownMaxHealth);
        tag.putBoolean(TAG_RITUAL_LOCKED, ritualLocked);
    }

    // Loads pedestal state and bound mob snapshot metadata
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.boundMobUuid = tag.hasUUID(TAG_BOUND_MOB_UUID) ? tag.getUUID(TAG_BOUND_MOB_UUID) : null;
        this.boundMobTypeId = tag.getString(TAG_BOUND_MOB_TYPE_ID);
        this.lastKnownEntityId = tag.contains(TAG_LAST_KNOWN_ENTITY_ID) ? tag.getInt(TAG_LAST_KNOWN_ENTITY_ID) : -1;
        this.lastKnownHealth = tag.contains(TAG_LAST_KNOWN_HEALTH) ? tag.getFloat(TAG_LAST_KNOWN_HEALTH) : 0.0F;
        this.lastKnownMaxHealth = tag.contains(TAG_LAST_KNOWN_MAX_HEALTH) ? tag.getFloat(TAG_LAST_KNOWN_MAX_HEALTH) : 0.0F;
        this.ritualLocked = tag.getBoolean(TAG_RITUAL_LOCKED);
    }

    // ==================================
    //  INTERNAL HELPERS
    // ==================================

    // Resolves the currently bound entity from cached id or UUID lookup
    @Nullable
    private Entity resolveBoundEntity(ServerLevel serverLevel) {
        if (lastKnownEntityId != -1) {
            Entity byId = serverLevel.getEntity(lastKnownEntityId);
            if (byId != null && byId.getUUID().equals(boundMobUuid)) {
                return byId;
            }
        }

        Entity byUuid = serverLevel.getEntity(boundMobUuid);
        if (byUuid != null) {
            lastKnownEntityId = byUuid.getId();
        }

        return byUuid;
    }

    // Keeps the bound mob centered and stabilized on top of this pedestal
    private void holdMobOnPedestal(Mob mob) {
        double targetX = worldPosition.getX() + 0.5D;
        double targetY = worldPosition.getY() + HOLD_Y_OFFSET;
        double targetZ = worldPosition.getZ() + 0.5D;

        mob.teleportTo(targetX, targetY, targetZ);
        mob.setDeltaMovement(0.0D, 0.0D, 0.0D);
        mob.fallDistance = 0.0F;
    }

    // Updates stored mob type id and health snapshot and returns true when values changed
    private boolean snapshotBoundEntity(LivingEntity livingEntity) {
        boolean changed = false;

        String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(livingEntity.getType()).toString();
        if (!typeId.equals(this.boundMobTypeId)) {
            this.boundMobTypeId = typeId;
            changed = true;
        }

        int entityId = livingEntity.getId();
        if (entityId != this.lastKnownEntityId) {
            this.lastKnownEntityId = entityId;
            changed = true;
        }

        float health = livingEntity.getHealth();
        if (Math.abs(health - this.lastKnownHealth) > HEALTH_SYNC_EPSILON) {
            this.lastKnownHealth = health;
            changed = true;
        }

        float maxHealth = livingEntity.getMaxHealth();
        if (Math.abs(maxHealth - this.lastKnownMaxHealth) > HEALTH_SYNC_EPSILON) {
            this.lastKnownMaxHealth = maxHealth;
            changed = true;
        }

        return changed;
    }

    // Marks block entity dirty and pushes client updates
    private void markChangedAndSync() {
        super.setChanged();

        if (level == null || level.isClientSide) return;

        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
    }
}