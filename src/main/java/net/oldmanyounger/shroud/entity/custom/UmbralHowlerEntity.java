package net.oldmanyounger.shroud.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.oldmanyounger.shroud.entity.client.UmbralHowlerAnimations;
import net.oldmanyounger.shroud.entity.goal.VibrationGoal;
import net.oldmanyounger.shroud.entity.goal.VibrationListener;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

/**
 * Represents the custom Umbral Howler hostile entity for the Shroud mod
 * <p>
 * This entity:
 * <ul>
 *   <li>Extends {@link Monster} to participate in vanilla hostile mob AI and combat systems</li>
 *   <li>Implements {@link GeoEntity} to drive animations through GeckoLib</li>
 *   <li>Implements {@link VibrationListener} and {@link VibrationSystem} to react to
 *       in-world game events such as footsteps and projectiles via the vibration system</li>
 * </ul>
 * Core responsibilities include:
 * <ul>
 *   <li>Defining custom attributes such as health, damage, and movement speed</li>
 *   <li>Registering AI goals for melee attacks, target acquisition, and vibration-based movement</li>
 *   <li>Handling vibration events to move toward sound sources and trigger reaction animations</li>
 *   <li>Playing themed sounds and spawning particles on attack and death</li>
 *   <li>Managing GeckoLib animation controllers for idle, walking, attacking, and vibration reaction states</li>
 * </ul>
 * The vibration logic is designed to be rate-limited via a configurable cooldown so that
 * the entity does not overstimulate from frequent vibration events in dense environments
 */
public class UmbralHowlerEntity extends Monster implements GeoEntity, VibrationListener, VibrationSystem {

    // ============================================================
    //  CONSTANTS
    // ============================================================

    // Cooldown in ticks before reacting to another vibration (20 ticks = 1 second)
    private static final int VIBRATION_COOLDOWN_TICKS = 60;

    // Passive detection (no vibration): only aggro if the player is extremely close
    private static final double PASSIVE_PLAYER_DETECT_RANGE = 2.0D;

    // Vibration-triggered acquire range (must be within this range when the vibration is processed)
    private static final double VIBRATION_PLAYER_ACQUIRE_RANGE = 12.0D;

    // Entity event id used to sync an attack animation to clients
    private static final byte EVENT_ATTACK_ANIM = 60;

    // Entity event id used to sync a vibration reaction animation to clients
    private static final byte EVENT_VIBRATION_REACT_ANIM = 61;

    // ============================================================
    //  FIELDS
    // ============================================================

    // Cache for GeckoLib animation instance data associated with this entity
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Vibration data used by the underlying vibration system implementation
    private final VibrationSystem.Data vibrationData = new VibrationSystem.Data();

    // Vibration user describing how this entity listens to and filters vibrations
    private final VibrationSystem.User vibrationUser;

    // Dynamic game event listener that subscribes this entity to vibration events at runtime
    private final DynamicGameEventListener<VibrationSystem.Listener> dynamicGameEventListener;

    // Next game time tick when another vibration can be processed
    private long nextVibrationGameTime = 0L;

    // Stores the most recently detected vibration location for AI goals to use
    @Nullable
    private BlockPos vibrationLocation;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    // Constructs a new Umbral Howler entity and initializes vibration handling
    public UmbralHowlerEntity(EntityType<? extends UmbralHowlerEntity> type, Level level) {
        super(type, level);
        this.vibrationUser = new UmbralHowlerVibrationUser();
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));
    }

    // ============================================================
    //  ATTRIBUTES
    // ============================================================

    // Builds the attribute set used when this entity type is registered
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 45.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 15.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    // ============================================================
    //  AI GOALS (HOSTILE MOB)
    // ============================================================

    // Registers movement, combat, and vibration-based AI goals
    @Override
    protected void registerGoals() {
        // Basic movement and combat goals
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.2D, 25));

        // Custom goal that moves the entity toward the last vibration location
        this.goalSelector.addGoal(4, new VibrationGoal(this, 0.8D));

        // Ambient wandering and looking behavior
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        // Targeting behavior for retaliation and attacking nearby players
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false,
                player -> this.distanceToSqr(player) <= (PASSIVE_PLAYER_DETECT_RANGE * PASSIVE_PLAYER_DETECT_RANGE)
        ));

    }

    // ============================================================
    //  TICKING / LIFECYCLE
    // ============================================================

    // Per-tick update hook for this entity
    @Override
    public void tick() {
        // Advance vibration system state only on the server side
        if (this.level() instanceof ServerLevel serverLevel) {
            VibrationSystem.Ticker.tick(serverLevel, this.vibrationData, this.vibrationUser);
        }

        super.tick();
    }

    // Handles custom death behavior including sounds, particles, and loot
    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        // Skip server-side effects when on the client
        if (this.level().isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        BlockPos pos = this.blockPosition();

        this.spreadSculkOnDeath(serverLevel, pos, damageSource);

        // Play a shrieker sound when the entity dies
        serverLevel.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 1.0F, 0.2F);

        // Spawn soul-like sculk particles at the death position
        serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 0.5,
                this.getZ(), 20, 0.4, 0.4, 0.4, 0.02);

        // Drop an echo shard as a thematic reward
        this.spawnAtLocation(Items.ECHO_SHARD);
    }

    // Spreads sculk at the death position similarly to a catalyst, using experience reward as "charge"
    public void spreadSculkOnDeath(ServerLevel level, BlockPos pos, DamageSource damageSource) {
        Entity attacker = damageSource.getEntity();
        int charge = this.getExperienceReward(level, attacker);

        if (charge <= 0) {
            charge = 1;
        }

        SculkSpreader sculkSpreader = SculkSpreader.createLevelSpreader();
        sculkSpreader.addCursors(pos, charge);
        sculkSpreader.updateCursors(level, pos, level.getRandom(), true);
    }

    // ============================================================
    //  COMBAT / ENTITY EVENTS
    // ============================================================

    // Handles melee attack logic and triggers the associated attack animation and sound
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);

        if (result) {
            // Broadcast an entity event so clients can start the attack animation
            Level level = this.level();
            level.broadcastEntityEvent(this, EVENT_ATTACK_ANIM);

            // Play a sculk-themed bloom sound from the entity position
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SCULK_CATALYST_BLOOM,
                    SoundSource.HOSTILE, 1.0F, 0.8F);
        }

        return result;
    }

    // Handles entity events and routes them to the appropriate GeckoLib animations
    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK_ANIM) {
            // Trigger GeckoLib attack animation on clients when the server broadcasts the attack event
            this.triggerAnim("umbral_howler_controller", "attack");
            return;
        }

        if (id == EVENT_VIBRATION_REACT_ANIM) {
            this.triggerAnim("umbral_howler_controller", "vibration_react");
            return;
        }

        super.handleEntityEvent(id);
    }

    // ============================================================
    //  VIBRATION SYSTEM IMPLEMENTATION
    // ============================================================

    // Exposes the vibration data used by this entity
    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    // Exposes the vibration user that controls how this entity listens to vibrations
    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    // Registers or unregisters the dynamic game event listener with the current server level
    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> listenerConsumer) {
        if (this.level() instanceof ServerLevel level) {
            listenerConsumer.accept(this.dynamicGameEventListener, level);
        }
    }

    // Retrieves the current vibration target location for the vibration goal
    @Override
    @Nullable
    public BlockPos getVibrationLocation() {
        return this.vibrationLocation;
    }

    // Updates the vibration target location when a new vibration is processed
    @Override
    public void setVibrationLocation(@Nullable BlockPos pos) {
        this.vibrationLocation = pos;
    }

    // Indicates that this entity dampens vibrations and should not trigger external sculk devices
    @Override
    public boolean dampensVibrations() {
        // Umbral Howler should not cause sculk sensors / shriekers to trigger
        return true;
    }

    // ============================================================
    //  GECKOLIB ANIMATION
    // ============================================================

    // Registers GeckoLib animation controllers for idle, walking, attack, and vibration reaction states
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "umbral_howler_controller", 4, state -> {
                    // Preserve attack animation if it's currently playing (triggered)
                    if (state.isCurrentAnimation(UmbralHowlerAnimations.ATTACK) && !state.getController().hasAnimationFinished()) {
                        return state.setAndContinue(UmbralHowlerAnimations.ATTACK);
                    }

                    // Preserve vibration reaction animation if it's currently playing (triggered)
                    if (state.isCurrentAnimation(UmbralHowlerAnimations.VIBRATION_REACT) && !state.getController().hasAnimationFinished()) {
                        return state.setAndContinue(UmbralHowlerAnimations.VIBRATION_REACT);
                    }

                    // Determine if the entity is moving based on navigation or motion vector
                    boolean moving = this.getNavigation().isInProgress()
                            || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;

                    if (moving) {
                        return state.setAndContinue(UmbralHowlerAnimations.WALKING);
                    }

                    // Preserve the special idle spike animation if already playing
                    if (state.isCurrentAnimation(UmbralHowlerAnimations.IDLE_SPIKE)) {
                        return state.setAndContinue(UmbralHowlerAnimations.IDLE_SPIKE);
                    }

                    // Occasionally play an idle spike animation for ambient flavor
                    if (this.getRandom().nextInt(1800) == 0) {
                        return state.setAndContinue(UmbralHowlerAnimations.IDLE_SPIKE);
                    }

                    // Default to standard idle animation when no other conditions are met
                    return state.setAndContinue(UmbralHowlerAnimations.IDLE);
                })
                        // Make this controller respond to triggerAnim("attack") and triggerAnim("vibration_react")
                        .triggerableAnim("attack", UmbralHowlerAnimations.ATTACK)
                        .triggerableAnim("vibration_react", UmbralHowlerAnimations.VIBRATION_REACT)
        );
    }

    // Provides GeckoLib with this entity's animation instance cache
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ============================================================
    //  VIBRATION USER (INNER CLASS)
    // ============================================================

    /**
     * Vibration system user implementation for the Umbral Howler entity
     * <p>
     * This inner class describes how the entity participates in the vibration system, including:
     * <ul>
     *   <li>Where the listener is located relative to the entity (eye height)</li>
     *   <li>The radius within which it can hear and respond to vibrations</li>
     *   <li>Which game events it is willing to listen to via a tag filter</li>
     *   <li>Custom filtering rules that prevent self-triggering, respect world borders,
     *       and enforce a vibration cooldown</li>
     *   <li>Reaction logic that updates the entity state and triggers a vibration reaction animation</li>
     * </ul>
     * The cooldown logic in this user reduces spam from frequent vibrations and ensures
     * that AI responses feel deliberate and readable to the player
     */
    private class UmbralHowlerVibrationUser implements VibrationSystem.User {

        // Position source used to track the listener at the entity's eye height
        private final PositionSource positionSource =
                new EntityPositionSource(UmbralHowlerEntity.this, UmbralHowlerEntity.this.getEyeHeight());

        // Returns the position source that the vibration system uses to locate this listener
        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        // Returns the radius in blocks within which this entity can hear vibrations
        @Override
        public int getListenerRadius() {
            return 12; // hearing radius
        }

        // Specifies which game events this entity can listen to using a tag filter
        @Override
        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.WARDEN_CAN_LISTEN;
        }

        // Indicates whether this entity can avoid triggering vibrations during certain actions
        @Override
        public boolean canTriggerAvoidVibration() {
            return true;
        }

        // Determines whether a particular vibration should be processed by this entity
        @Override
        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> gameEvent, GameEvent.Context context) {
            // Do not process vibrations if the entity is inactive or already dead
            if (UmbralHowlerEntity.this.isNoAi() || UmbralHowlerEntity.this.isDeadOrDying()) {
                return false;
            }

            // Ignore vibrations that occur outside the world border
            if (!level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }

            // Prevent the entity from reacting to vibrations caused by itself
            Entity source = context.sourceEntity();
            if (source == UmbralHowlerEntity.this) {
                return false;
            }

            // Optional early gating: refuse to even accept vibrations while on cooldown
            long gameTime = level.getGameTime();
            return gameTime >= UmbralHowlerEntity.this.nextVibrationGameTime;
        }

        // Handles the entity's reaction to an accepted vibration event
        @Override
        public void onReceiveVibration(ServerLevel level,
                                       BlockPos pos,
                                       Holder<GameEvent> gameEvent,
                                       Entity sourceEntity,
                                       Entity projectileOwner,
                                       float distance) {
            // Ignore reactions if the entity has already died
            if (UmbralHowlerEntity.this.isDeadOrDying()) {
                return;
            }

            long gameTime = level.getGameTime();

            // Final cooldown check in case multiple vibrations reach at the same tick
            if (gameTime < UmbralHowlerEntity.this.nextVibrationGameTime) {
                return;
            }

            // Set next allowed vibration time to enforce cooldown
            UmbralHowlerEntity.this.nextVibrationGameTime = gameTime + VIBRATION_COOLDOWN_TICKS;

            // React to this vibration by storing the position for the AI goal to pursue
            UmbralHowlerEntity.this.setVibrationLocation(pos);

            // If the vibration was caused by a player (or a player-owned projectile), acquire that player as the target,
            // but only if they're within the acquire range when the vibration is processed.
            Player playerToTarget = null;

            if (sourceEntity instanceof Player p) {
                playerToTarget = p;
            } else if (projectileOwner instanceof Player p) {
                playerToTarget = p;
            }

            if (playerToTarget != null) {
                double maxDistSqr = VIBRATION_PLAYER_ACQUIRE_RANGE * VIBRATION_PLAYER_ACQUIRE_RANGE;
                if (UmbralHowlerEntity.this.distanceToSqr(playerToTarget) <= maxDistSqr) {
                    UmbralHowlerEntity.this.setTarget(playerToTarget);
                }
            }

            // Broadcast a vibration reaction event so clients can play the reaction animation
            UmbralHowlerEntity.this.level().broadcastEntityEvent(
                    UmbralHowlerEntity.this,
                    EVENT_VIBRATION_REACT_ANIM
            );
        }
    }
}
