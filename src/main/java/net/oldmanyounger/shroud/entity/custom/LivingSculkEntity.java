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
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.levelgen.feature.IceSpikeFeature;
import net.oldmanyounger.shroud.entity.client.LivingSculkAnimations;
import net.oldmanyounger.shroud.entity.goal.VibrationGoal;
import net.oldmanyounger.shroud.entity.goal.VibrationListener;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

/**
 * Represents the custom Living Sculk hostile entity for the Shroud mod
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
public class LivingSculkEntity extends Monster implements GeoEntity, VibrationListener, VibrationSystem {

    // ============================================================
    //  CONSTANTS
    // ============================================================

    // Cooldown in ticks before reacting to another vibration (20 ticks = 1 second)
    private static final int VIBRATION_COOLDOWN_TICKS = 40;

    // Entity event id used to sync an attack animation to clients
    private static final byte EVENT_ATTACK_ANIM = 4;

    // Entity event id used to sync a vibration reaction animation to clients
    private static final byte EVENT_VIBRATION_REACT_ANIM = 5;

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

    // Constructs a new Living Sculk entity and initializes vibration handling
    public LivingSculkEntity(EntityType<? extends LivingSculkEntity> type, Level level) {
        super(type, level);
        this.vibrationUser = new LivingSculkVibrationUser();
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));
    }

    // ============================================================
    //  ATTRIBUTES
    // ============================================================

    // Builds the attribute set used when this entity type is registered
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 5.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    // ============================================================
    //  AI GOALS (HOSTILE MOB)
    // ============================================================

    // Registers movement, combat, and vibration-based AI goals
    @Override
    protected void registerGoals() {
        // Basic movement and combat goals
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.8D, 20));

        // Custom goal that moves the entity toward the last vibration location
        this.goalSelector.addGoal(4, new VibrationGoal(this, 0.8D));

        // Ambient wandering and looking behavior
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        // Targeting behavior for retaliation and attacking nearby players
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
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

        // Play a shrieker sound when the entity dies
        serverLevel.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 1.0F, 0.2F);

        // Spawn soul-like sculk particles at the death position
        serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 0.5,
                this.getZ(), 20, 0.4, 0.4, 0.4, 0.02);

        // Drop an echo shard as a thematic reward
        this.spawnAtLocation(Items.ECHO_SHARD);
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
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_ANIM);

            // Play a sculk-themed bloom sound from the entity position
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SCULK_CATALYST_BLOOM,
                    SoundSource.HOSTILE, 1.0F, 0.8F);
        }

        return result;
    }

    // Handles entity events and routes them to the appropriate GeckoLib animations
    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK_ANIM) {
            this.triggerAnim("living_sculk_controller", "attack");
        } else if (id == EVENT_VIBRATION_REACT_ANIM) {
            this.triggerAnim("living_sculk_controller", "vibration_react");
        } else {
            super.handleEntityEvent(id);
        }
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
    public void updateDynamicGameEventListener(java.util.function.BiConsumer<DynamicGameEventListener<?>, ServerLevel> listenerConsumer) {
        if (this.level() instanceof ServerLevel level) {
            listenerConsumer.accept(this.dynamicGameEventListener, level);
        }
    }

    // Retrieves the current vibration target location for the vibration goal
    @Override
    public BlockPos getVibrationLocation() {
        return this.vibrationLocation;
    }

    // Updates the vibration target location when a new vibration is processed
    @Override
    public void setVibrationLocation(BlockPos pos) {
        this.vibrationLocation = pos;
    }

    // Indicates that this entity dampens vibrations and should not trigger external sculk devices
    @Override
    public boolean dampensVibrations() {
        // Living Sculk should not cause sculk sensors / shriekers to trigger
        return true;
    }

    // ============================================================
    //  GECKOLIB ANIMATION
    // ============================================================

    // Registers GeckoLib animation controllers for idle, walking, attack, and vibration reaction states
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "living_sculk_controller", 4, state -> {
                    // Attack takes priority
                    if (this.swinging && this.getTarget() != null) {
                        return state.setAndContinue(LivingSculkAnimations.ATTACK);
                    }

                    // Determine if the entity is moving based on navigation or motion vector
                    boolean moving = this.getNavigation().isInProgress()
                            || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;

                    if (moving) {
                        return state.setAndContinue(LivingSculkAnimations.WALKING);
                    }

                    // Preserve the special idle head split animation if already playing
                    if (state.isCurrentAnimation(LivingSculkAnimations.IDLE_HEAD_SPLIT)) {
                        return state.setAndContinue(LivingSculkAnimations.IDLE_HEAD_SPLIT);
                    }

                    // Occasionally play an idle head split animation for ambient flavor
                    if (this.getRandom().nextInt(1800) == 0) {
                        return state.setAndContinue(LivingSculkAnimations.IDLE_HEAD_SPLIT);
                    }

                    // Default to standard idle animation when no other conditions are met
                    return state.setAndContinue(LivingSculkAnimations.IDLE);
                })
                        // Make this controller respond to triggerAnim("vibration_react")
                        .triggerableAnim("vibration_react", LivingSculkAnimations.VIBRATION_REACT)
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
     * Vibration system user implementation for the Living Sculk entity
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
    private class LivingSculkVibrationUser implements VibrationSystem.User {

        // Position source used to track the listener at the entity's eye height
        private final PositionSource positionSource =
                new EntityPositionSource(LivingSculkEntity.this, LivingSculkEntity.this.getEyeHeight());

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
            if (LivingSculkEntity.this.isNoAi() || LivingSculkEntity.this.isDeadOrDying()) {
                return false;
            }

            // Ignore vibrations that occur outside the world border
            if (!level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }

            // Prevent the entity from reacting to vibrations caused by itself
            Entity source = context.sourceEntity();
            if (source == LivingSculkEntity.this) {
                return false;
            }

            // Optional early gating: refuse to even accept vibrations while on cooldown
            long gameTime = level.getGameTime();
            return gameTime >= LivingSculkEntity.this.nextVibrationGameTime;
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
            if (LivingSculkEntity.this.isDeadOrDying()) {
                return;
            }

            long gameTime = level.getGameTime();

            // Final cooldown check in case multiple vibrations reach at the same tick
            if (gameTime < LivingSculkEntity.this.nextVibrationGameTime) {
                return;
            }

            // Set next allowed vibration time to enforce cooldown
            LivingSculkEntity.this.nextVibrationGameTime = gameTime + VIBRATION_COOLDOWN_TICKS;

            // React to this vibration by storing the position for the AI goal to pursue
            LivingSculkEntity.this.setVibrationLocation(pos);

            // Broadcast a vibration reaction event so clients can play the reaction animation
            LivingSculkEntity.this.level().broadcastEntityEvent(
                    LivingSculkEntity.this,
                    EVENT_VIBRATION_REACT_ANIM
            );
        }
    }
}
