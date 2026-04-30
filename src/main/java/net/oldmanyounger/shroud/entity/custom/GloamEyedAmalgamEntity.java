package net.oldmanyounger.shroud.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.oldmanyounger.shroud.entity.client.GloamEyedAmalgamAnimations;
import net.oldmanyounger.shroud.entity.goal.VibrationGoal;
import net.oldmanyounger.shroud.entity.goal.VibrationListener;
import net.oldmanyounger.shroud.sound.ModSounds;
import net.oldmanyounger.shroud.tag.ModEntityTypeTags;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.BiConsumer;

/**
 * Defines the Gloam Eyed Amalgam hostile entity and its core gameplay behavior.
 *
 * <p>This entity provides baseline monster combat AI, vibration-based sensing, and GeckoLib animation control without conversion mechanics or item-corruption mechanics.
 *
 * <p>In the broader context of the project, this class is part of Shroud's core hostile-mob systems, providing a clean foundation variant that can be extended with specialized mechanics later.
 */
public class GloamEyedAmalgamEntity extends Monster implements GeoEntity, VibrationListener, VibrationSystem {

    // ==================================
    //  CONSTANTS
    // ==================================

    // Cooldown between accepted vibration events
    private static final int VIBRATION_COOLDOWN_TICKS = 80;

    // Passive proximity needed for player targeting without vibration
    private static final double PASSIVE_PLAYER_DETECT_RANGE = 2.0D;

    // Max distance for acquiring player targets from vibration events
    private static final double VIBRATION_PLAYER_ACQUIRE_RANGE = 12.0D;

    // Extra melee reach in blocks beyond vanilla-sized baseline
    private static final double MELEE_REACH_BONUS_BLOCKS = 2.0D;

    // Constant heartbeat interval in ticks
    private static final int HEARTBEAT_INTERVAL_TICKS = 40;

    // Adjustable client heartbeat loudness
    private static final float HEARTBEAT_VOLUME = 5.0F;

    // Duration to block pursue movement while roar animation finishes
    private static final int TARGET_ACQUIRE_ROAR_TICKS = 40;

    // Entity event IDs for client animation triggers
    private static final byte EVENT_ATTACK_ANIM = 60;
    private static final byte EVENT_VIBRATION_REACT_ANIM = 5;
    private static final byte EVENT_ROAR_ANIM = 61;

    // GeckoLib animation controller names
    private static final String CTRL_LOCOMOTION = "gloam_eyed_amalgam_locomotion_controller";
    private static final String CTRL_VIBRATION = "gloam_eyed_amalgam_vibration_controller";
    private static final String CTRL_ATTACK = "gloam_eyed_amalgam_attack_controller";
    private static final String CTRL_ROAR = "gloam_eyed_amalgam_roar_controller";

    // GeckoLib trigger names
    private static final String TRIG_ATTACK = "attack";
    private static final String TRIG_VIBRATION_REACT = "vibration_react";
    private static final String TRIG_ROAR = "roar";

    // ==================================
    //  FIELDS
    // ==================================

    // Per-entity GeckoLib cache
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Vibration system state and listener plumbing
    private final VibrationSystem.Data vibrationData = new VibrationSystem.Data();
    private final VibrationSystem.User vibrationUser;
    private final DynamicGameEventListener<VibrationSystem.Listener> dynamicGameEventListener;

    // Earliest game time at which a new vibration can be accepted
    private long nextVibrationGameTime = 0L;

    // Earliest game time at which a new heartbeat can be played
    private long nextHeartbeatGameTime = 0L;

    // Earliest game time when pursue movement may start after target acquire roar
    private long pursueUnlockGameTime = 0L;

    // Tracks whether model-driven head look should be applied this frame
    private boolean allowCustomHeadLook = true;

    // Most recent target that triggered a roar lock
    @Nullable
    private LivingEntity lastRoarTarget;

    // Most recent vibration location used by vibration-aware goals
    @Nullable
    private BlockPos vibrationLocation;

    // ==================================
    //  STATIC HELPERS
    // ==================================

    // Returns true if the entity is a creative-mode player
    private static boolean isCreativePlayer(@Nullable Entity entity) {
        return entity instanceof Player p && p.isCreative();
    }

    // Returns true when this entity type is marked vibration-friendly
    private boolean isVibrationFriendlySelf() {
        return this.getType().is(ModEntityTypeTags.VIBRATION_FRIENDLY);
    }

    // Returns true when the source entity type is marked vibration-friendly
    private static boolean isVibrationFriendlyEntity(@Nullable Entity entity) {
        return entity != null && entity.getType().is(ModEntityTypeTags.VIBRATION_FRIENDLY);
    }

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Creates the entity and wires vibration listener state
    public GloamEyedAmalgamEntity(EntityType<? extends GloamEyedAmalgamEntity> type, Level level) {
        super(type, level);
        this.vibrationUser = new GloamEyedAmalgamVibrationUser();
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));
    }

    // ==================================
    //  ATTRIBUTES
    // ==================================

    // Declares baseline combat and movement attributes using Warden-like core values
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.19D)
                .add(Attributes.ATTACK_DAMAGE, 30.0D)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 15.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    // ==================================
    //  AI GOALS
    // ==================================

    // Registers AI and target goals
    @Override
    protected void registerGoals() {
        // Movement and combat goals
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false) {
            // Extends melee range and preserves swing-based attack animation trigger
            @Override
            protected void checkAndPerformAttack(LivingEntity target) {
                double distToTargetSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());

                double baseReachSqr = (this.mob.getBbWidth() * 2.0F) * (this.mob.getBbWidth() * 2.0F) + target.getBbWidth();
                double bonusReachSqr = MELEE_REACH_BONUS_BLOCKS * MELEE_REACH_BONUS_BLOCKS;
                double totalReachSqr = baseReachSqr + bonusReachSqr;

                if (distToTargetSqr <= totalReachSqr && this.isTimeToAttack()) {
                    this.resetAttackCooldown();
                    GloamEyedAmalgamEntity.this.swing(InteractionHand.MAIN_HAND);
                    GloamEyedAmalgamEntity.this.doHurtTarget(target);
                }
            }
        });

        // Blocks pursue movement while target-acquire roar plays
        this.goalSelector.addGoal(2, new TargetAcquireRoarGoal(this));

        // Pursue movement begins only after roar gate unlocks
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 0.8D, 20) {
            // Checks if pursue movement can start
            @Override
            public boolean canUse() {
                return super.canUse() && GloamEyedAmalgamEntity.this.isPursueUnlocked();
            }



            // Checks if pursue movement can continue
            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && GloamEyedAmalgamEntity.this.isPursueUnlocked();
            }
        });

        // Navigation toward recently detected vibration locations
        this.goalSelector.addGoal(4, new VibrationGoal(this, 0.8D));

        // Idle and awareness behavior
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        // Retaliation and proximity-based player targeting
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Player.class,
                        10,
                        false,
                        false,
                        player -> this.distanceToSqr(player) <= (PASSIVE_PLAYER_DETECT_RANGE * PASSIVE_PLAYER_DETECT_RANGE)
                )
        );

        // Hostility toward vanilla zombie family entities
        this.targetSelector.addGoal(
                3,
                new NearestAttackableTargetGoal<>(
                        this,
                        Monster.class,
                        10,
                        true,
                        false,
                        target -> target.getType().is(EntityTypeTags.ZOMBIES)
                )
        );
    }

    // ==================================
    //  TARGET ACQUIRE ROAR GATE
    // ==================================

    // Returns true when pursue movement is allowed
    private boolean isPursueUnlocked() {
        return this.level().getGameTime() >= this.pursueUnlockGameTime;
    }

    // Starts roar lock and roar animation for a newly acquired target
    private void beginRoarForTarget(LivingEntity target) {
        this.pursueUnlockGameTime = this.level().getGameTime() + TARGET_ACQUIRE_ROAR_TICKS;
        this.lastRoarTarget = target;

        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, EVENT_ROAR_ANIM);
        }
    }

    // Tracks newly acquired targets and opens roar gate before pursue movement
    private static class TargetAcquireRoarGoal extends Goal {
        private final GloamEyedAmalgamEntity mob;

        // Creates the target-acquire roar gate goal
        private TargetAcquireRoarGoal(GloamEyedAmalgamEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        // Starts when there is a new live target that has not triggered roar yet
        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive() && target != this.mob.lastRoarTarget;
        }

        // Runs once to apply roar lock and animation trigger
        @Override
        public void start() {
            LivingEntity target = this.mob.getTarget();
            if (target != null && target.isAlive()) {
                this.mob.beginRoarForTarget(target);
                this.mob.getNavigation().stop();
            }
        }

        // Stops immediately after start because this is a one-shot gate trigger
        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    // ==================================
    //  TICKING / LIFECYCLE
    // ==================================

    // Ticks vibration state on server and heartbeat on client
    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel serverLevel) {
            VibrationSystem.Ticker.tick(serverLevel, this.vibrationData, this.vibrationUser);
        }

        super.tick();

        if (this.level().isClientSide()) {
            this.tickHeartbeatSoundClient();
        }
    }

    // Plays heartbeat sound locally on a fixed interval with adjustable loudness
    private void tickHeartbeatSoundClient() {
        if (this.isDeadOrDying() || this.isSilent()) {
            return;
        }

        long gameTime = this.level().getGameTime();
        if (gameTime < this.nextHeartbeatGameTime) {
            return;
        }

        this.nextHeartbeatGameTime = gameTime + HEARTBEAT_INTERVAL_TICKS;

        this.level().playLocalSound(
                this.getX(),
                this.getY(),
                this.getZ(),
                ModSounds.ENTITY_GLOAM_EYED_AMALGAM_HEARTBEAT.get(),
                this.getSoundSource(),
                HEARTBEAT_VOLUME,
                this.getVoicePitch(),
                false
        );
    }

    // Runs death-side effects including sculk spread and particles
    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (this.level().isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        BlockPos pos = this.blockPosition();

        this.spreadSculkOnDeath(serverLevel, pos, damageSource);

        serverLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                this.getX(),
                this.getY() + 0.5D,
                this.getZ(),
                20,
                0.4D,
                0.4D,
                0.4D,
                0.02D
        );
    }

    // Spreads sculk using charge derived from experience reward
    private void spreadSculkOnDeath(ServerLevel level, BlockPos pos, DamageSource damageSource) {
        Entity attacker = damageSource.getEntity();
        int charge = this.getExperienceReward(level, attacker);

        if (charge <= 0) {
            charge = 1;
        }

        SculkSpreader sculkSpreader = SculkSpreader.createLevelSpreader();
        sculkSpreader.addCursors(pos, charge);
        sculkSpreader.updateCursors(level, pos, level.getRandom(), true);
    }

    // ==================================
    //  COMBAT / ENTITY EVENTS
    // ==================================

    // Broadcasts attack animation event when swinging main hand on server
    @Override
    public void swing(InteractionHand hand, boolean updateSelf) {
        super.swing(hand, updateSelf);

        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_ANIM);
        }
    }

    // Processes successful melee hits with feedback sound
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);

        if (result) {
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.SCULK_CATALYST_BLOOM,
                    SoundSource.HOSTILE,
                    1.0F,
                    0.8F
            );
        }

        return result;
    }

    // Routes entity event IDs into GeckoLib trigger calls
    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK_ANIM) {
            this.triggerAnim(CTRL_ATTACK, TRIG_ATTACK);
            return;
        }

        if (id == EVENT_VIBRATION_REACT_ANIM) {
            this.triggerAnim(CTRL_VIBRATION, TRIG_VIBRATION_REACT);
            return;
        }

        if (id == EVENT_ROAR_ANIM) {
            this.triggerAnim(CTRL_ROAR, TRIG_ROAR);
            return;
        }

        super.handleEntityEvent(id);
    }

    // ==================================
    //  LOOK ROTATION TUNING
    // ==================================

    // Matches vanilla-like max head yaw relative to body
    @Override
    public int getMaxHeadYRot() {
        return 75;
    }

    // Controls how quickly the head can rotate per tick
    @Override
    public int getHeadRotSpeed() {
        return 10;
    }

    // ==================================
    //  SOUND OVERRIDES
    // ==================================

    // Returns ambient sound event
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_LIVING_SCULK_AMBIENT.get();
    }

    // Returns ambient sound interval in ticks
    @Override
    public int getAmbientSoundInterval() {
        return 180;
    }

    // Returns hurt sound event
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_LIVING_SCULK_HURT.get();
    }

    // Returns death sound event
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_LIVING_SCULK_DEATH.get();
    }

    // Plays custom step sound
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.ENTITY_LIVING_SCULK_STEP.get(), 0.15F, 1.0F);
    }

    // ==================================
    //  VIBRATION SYSTEM IMPLEMENTATION
    // ==================================

    // Exposes vibration data for vibration system ticking
    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    // Exposes vibration user implementation for vibration system ticking
    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    // Registers or unregisters the dynamic game event listener against the current server level
    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> listenerConsumer) {
        if (this.level() instanceof ServerLevel level) {
            listenerConsumer.accept(this.dynamicGameEventListener, level);
        }
    }

    // Returns the most recently stored vibration location
    @Override
    public BlockPos getVibrationLocation() {
        return this.vibrationLocation;
    }

    // Stores the most recently received vibration location
    @Override
    public void setVibrationLocation(BlockPos pos) {
        this.vibrationLocation = pos;
    }

    // Indicates this entity dampens vibration propagation
    @Override
    public boolean dampensVibrations() {
        return true;
    }

    // Returns whether model-level head look overrides are currently allowed
    public boolean allowCustomHeadLook() {
        return this.allowCustomHeadLook;
    }

    // ==================================
    //  GECKOLIB ANIMATION
    // ==================================

    // Registers GeckoLib animation controllers
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                // Locomotion controller for idle and walk selection
                new AnimationController<>(this, CTRL_LOCOMOTION, 4, state -> {
                    boolean moving = this.getNavigation().isInProgress()
                            || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;

                    if (moving) {
                        this.allowCustomHeadLook = true;
                        return state.setAndContinue(GloamEyedAmalgamAnimations.WALKING);
                    }

                    if (state.isCurrentAnimation(GloamEyedAmalgamAnimations.IDLE_EYE_WATCH)) {
                        this.allowCustomHeadLook = false;
                        return state.setAndContinue(GloamEyedAmalgamAnimations.IDLE_EYE_WATCH);
                    }

                    if (this.getRandom().nextInt(1800) == 0) {
                        this.allowCustomHeadLook = false;
                        return state.setAndContinue(GloamEyedAmalgamAnimations.IDLE_EYE_WATCH);
                    }

                    this.allowCustomHeadLook = true;
                    return state.setAndContinue(GloamEyedAmalgamAnimations.IDLE);
                }),

                // Overlay controller for one-shot vibration reaction
                new AnimationController<>(this, CTRL_VIBRATION, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_VIBRATION_REACT, GloamEyedAmalgamAnimations.VIBRATION_REACT),

                // Overlay controller for one-shot attack animation
                new AnimationController<>(this, CTRL_ATTACK, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_ATTACK, GloamEyedAmalgamAnimations.ATTACK),

                // Overlay controller for one-shot target-acquire roar animation
                new AnimationController<>(this, CTRL_ROAR, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_ROAR, GloamEyedAmalgamAnimations.ROAR)
        );
    }

    // Returns the entity's GeckoLib animatable cache
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ==================================
    //  VIBRATION USER
    // ==================================

    // Implements vibration system behavior for this entity
    private class GloamEyedAmalgamVibrationUser implements VibrationSystem.User {

        // Position source tracks entity listener position at eye height
        private final PositionSource positionSource =
                new EntityPositionSource(GloamEyedAmalgamEntity.this, GloamEyedAmalgamEntity.this.getEyeHeight());

        // Returns vibration listener source position
        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        // Returns vibration listening radius in blocks
        @Override
        public int getListenerRadius() {
            return 12;
        }

        // Returns game event tag set this listener can react to
        @Override
        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.WARDEN_CAN_LISTEN;
        }

        // Allows this listener to trigger vibration avoidance checks
        @Override
        public boolean canTriggerAvoidVibration() {
            return true;
        }

        // Filters whether an incoming vibration should be accepted
        @Override
        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> gameEvent, GameEvent.Context context) {
            if (GloamEyedAmalgamEntity.this.isNoAi() || GloamEyedAmalgamEntity.this.isDeadOrDying()) {
                return false;
            }

            if (!level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }

            Entity source = context.sourceEntity();

            if (source == GloamEyedAmalgamEntity.this) {
                return false;
            }

            if (GloamEyedAmalgamEntity.this.isVibrationFriendlySelf() && isVibrationFriendlyEntity(source)) {
                return false;
            }

            if (isCreativePlayer(source)) {
                return false;
            }

            long gameTime = level.getGameTime();
            return gameTime >= GloamEyedAmalgamEntity.this.nextVibrationGameTime;
        }

        // Handles accepted vibration events and updates behavior and animation
        @Override
        public void onReceiveVibration(ServerLevel level,
                                       BlockPos pos,
                                       Holder<GameEvent> gameEvent,
                                       Entity sourceEntity,
                                       Entity projectileOwner,
                                       float distance) {

            if (isCreativePlayer(sourceEntity) || isCreativePlayer(projectileOwner)) {
                return;
            }

            if (GloamEyedAmalgamEntity.this.isVibrationFriendlySelf()
                    && (isVibrationFriendlyEntity(sourceEntity) || isVibrationFriendlyEntity(projectileOwner))) {
                return;
            }

            if (GloamEyedAmalgamEntity.this.isDeadOrDying()) {
                return;
            }

            long gameTime = level.getGameTime();
            if (gameTime < GloamEyedAmalgamEntity.this.nextVibrationGameTime) {
                return;
            }

            GloamEyedAmalgamEntity.this.nextVibrationGameTime = gameTime + VIBRATION_COOLDOWN_TICKS;
            GloamEyedAmalgamEntity.this.setVibrationLocation(pos);

            Player playerToTarget = null;

            if (sourceEntity instanceof Player p) {
                playerToTarget = p;
            } else if (projectileOwner instanceof Player p) {
                playerToTarget = p;
            }

            if (playerToTarget != null) {
                double maxDistSqr = VIBRATION_PLAYER_ACQUIRE_RANGE * VIBRATION_PLAYER_ACQUIRE_RANGE;
                if (GloamEyedAmalgamEntity.this.distanceToSqr(playerToTarget) <= maxDistSqr) {
                    GloamEyedAmalgamEntity.this.setTarget(playerToTarget);
                }
            }

            GloamEyedAmalgamEntity.this.level().broadcastEntityEvent(GloamEyedAmalgamEntity.this, EVENT_VIBRATION_REACT_ANIM);

            if (!GloamEyedAmalgamEntity.this.isSilent()) {
                level.playSound(
                        null,
                        GloamEyedAmalgamEntity.this.blockPosition(),
                        ModSounds.ENTITY_LIVING_SCULK_VIBRATION_REACT.get(),
                        GloamEyedAmalgamEntity.this.getSoundSource(),
                        1.0F,
                        1.0F
                );
            }
        }
    }
}