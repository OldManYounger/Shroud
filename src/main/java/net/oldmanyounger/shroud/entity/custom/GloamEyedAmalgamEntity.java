package net.oldmanyounger.shroud.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
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
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
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
 * <p>This entity provides monster combat AI, vibration-based sensing, target-acquire roar gating,
 * synchronized pursuit animation state, unreachable-target ranged pressure, heartbeat audio, and GeckoLib animation control.
 *
 * <p>In the broader context of the project, this class is part of Shroud's hostile-mob systems and
 * coordinates server-side AI state with client-side animation and sound presentation.
 */
public class GloamEyedAmalgamEntity extends Monster implements GeoEntity, VibrationListener, VibrationSystem {

    // ==================================
    //  ATTRIBUTES / MOVEMENT
    // ==================================

    // Normal non-combat movement speed modifier
    private static final double NORMAL_MOVE_SPEED_MODIFIER = 0.8D;

    // Faster movement speed modifier used while pursuing an acquired target
    private static final double PURSUIT_MOVE_SPEED_MODIFIER = 2.0D;

    // Extra melee reach in blocks beyond vanilla-sized baseline
    private static final double MELEE_REACH_BONUS_BLOCKS = 2.0D;

    // Maximum pathing distance used by the target pursuit goal
    private static final int PURSUIT_TARGET_MAX_DISTANCE = 20;

    // Horizontal movement threshold used to select locomotion animations
    private static final double MOVING_HORIZONTAL_DISTANCE_SQR = 1.0E-4D;

    // ==================================
    //  RANGED ATTACK
    // ==================================

    // Time the target must be unreachable before a sculk shot can fire
    private static final int UNREACHABLE_RANGED_ATTACK_TICKS = 80;

    // Interval for expensive path reachability checks
    private static final int UNREACHABLE_PATH_CHECK_INTERVAL_TICKS = 10;

    // Cooldown between sculk shot attacks
    private static final int SCULK_SHOT_COOLDOWN_TICKS = 100;

    // Estimated vertical position of the floating eye projectile source
    private static final double SCULK_SHOT_EYE_HEIGHT_OFFSET = 3.25D;

    // Estimated forward offset of the floating eye projectile source
    private static final double SCULK_SHOT_EYE_FORWARD_OFFSET = 0.45D;

    // ==================================
    //  TARGETING / VIBRATION
    // ==================================

    // Cooldown between accepted vibration events
    private static final int VIBRATION_COOLDOWN_TICKS = 80;

    // Passive proximity needed for player targeting without vibration
    private static final double PASSIVE_PLAYER_DETECT_RANGE = 2.0D;

    // Max distance for acquiring player targets from vibration events
    private static final double VIBRATION_PLAYER_ACQUIRE_RANGE = 12.0D;

    // ==================================
    //  DARKNESS / EFFECTS
    // ==================================

    // Warden-style darkness pulse settings
    private static final int DARKNESS_DISPLAY_LIMIT_TICKS = 200;
    private static final int DARKNESS_DURATION_TICKS = 260;
    private static final int DARKNESS_RADIUS_BLOCKS = 20;
    private static final int DARKNESS_INTERVAL_TICKS = 120;

    // ==================================
    //  ROAR / AUDIO
    // ==================================

    // Duration to block movement while the 3-second roar animation finishes
    private static final int TARGET_ACQUIRE_ROAR_TICKS = 60;

    // Target-acquire roar loudness
    private static final float ROAR_VOLUME = 2.5F;

    // Target-acquire roar pitch
    private static final float ROAR_PITCH = 1.0F;

    // Constant heartbeat interval in ticks
    private static final int HEARTBEAT_INTERVAL_TICKS = 40;

    // Adjustable client heartbeat loudness
    private static final float HEARTBEAT_VOLUME = 5.0F;

    // Normal walking step sound interval in ticks
    private static final int NORMAL_STEP_SOUND_INTERVAL_TICKS = 16;

    // Faster pursuit step sound interval in ticks
    private static final int PURSUIT_STEP_SOUND_INTERVAL_TICKS = 10;

    // Custom step sound loudness
    private static final float STEP_VOLUME = 2.0F;

    // Custom step sound pitch
    private static final float STEP_PITCH = 1.0F;

    // ==================================
    //  ANIMATION / SYNC IDS
    // ==================================

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

    // Synced client animation state for target pursuit
    private static final EntityDataAccessor<Boolean> DATA_PURSUING_ACQUIRED_TARGET =
            SynchedEntityData.defineId(GloamEyedAmalgamEntity.class, EntityDataSerializers.BOOLEAN);

    // Synced client animation state for target-acquire roar
    private static final EntityDataAccessor<Boolean> DATA_TARGET_ACQUIRE_ROARING =
            SynchedEntityData.defineId(GloamEyedAmalgamEntity.class, EntityDataSerializers.BOOLEAN);

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

    // Earliest game time at which a new step sound can be played
    private long nextStepSoundGameTime = 0L;

    // Earliest game time when pursue movement may start after target-acquire roar
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
    //  CONSTRUCTION / SYNC DATA
    // ==================================

    // Creates the entity and wires vibration listener state
    public GloamEyedAmalgamEntity(EntityType<? extends GloamEyedAmalgamEntity> type, Level level) {
        super(type, level);
        this.vibrationUser = new GloamEyedAmalgamVibrationUser();
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));
    }

    // Defines synced entity data used by client-side animation controllers
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PURSUING_ACQUIRED_TARGET, false);
        builder.define(DATA_TARGET_ACQUIRE_ROARING, false);
    }

    // ==================================
    //  ATTRIBUTES
    // ==================================

    // Declares baseline combat and movement attributes using Warden-like core values
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.ATTACK_DAMAGE, 30.0D)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 15.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    // ==================================
    //  AI GOALS
    // ==================================

    // Registers movement, combat, idle, and target-selection goals
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new TargetAcquireRoarGoal(this));
        this.goalSelector.addGoal(2, new GloamEyedAmalgamUnreachableRangedAttackGoal(this));
        this.goalSelector.addGoal(3, new GloamEyedAmalgamMeleeAttackGoal(this));
        this.goalSelector.addGoal(4, new GloamEyedAmalgamPursuitGoal(this));
        this.goalSelector.addGoal(5, new VibrationGoal(this, NORMAL_MOVE_SPEED_MODIFIER));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, NORMAL_MOVE_SPEED_MODIFIER));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Player.class,
                        10,
                        false,
                        false,
                        player -> this.distanceToSqr(player) <= PASSIVE_PLAYER_DETECT_RANGE * PASSIVE_PLAYER_DETECT_RANGE
                )
        );
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
    //  TARGET / PURSUIT STATE
    // ==================================

    // Returns true when pursue movement is allowed
    private boolean isPursueUnlocked() {
        return this.level().getGameTime() >= this.pursueUnlockGameTime;
    }

    // Returns true while target-acquire roar should immobilize the entity
    private boolean isTargetAcquireRoarActive() {
        return this.isValidPursuitTarget(this.getTarget()) && !this.isPursueUnlocked();
    }

    // Returns synced client-visible pursuit state for animation selection
    private boolean isPursuingAcquiredTarget() {
        return this.entityData.get(DATA_PURSUING_ACQUIRED_TARGET);
    }

    // Returns true on the server once this entity has roared at and is pursuing its current target
    private boolean shouldPursueAcquiredTarget() {
        LivingEntity target = this.getTarget();
        return this.isValidPursuitTarget(target)
                && target == this.lastRoarTarget
                && this.isPursueUnlocked();
    }

    // Syncs target-pursuit animation state to clients only when the value changes
    private void setPursuingAcquiredTarget(boolean pursuing) {
        if (this.entityData.get(DATA_PURSUING_ACQUIRED_TARGET) != pursuing) {
            this.entityData.set(DATA_PURSUING_ACQUIRED_TARGET, pursuing);
        }
    }

    // Starts roar lock, roar animation, and roar sound for a newly acquired target
    private void beginRoarForTarget(LivingEntity target) {
        this.pursueUnlockGameTime = this.level().getGameTime() + TARGET_ACQUIRE_ROAR_TICKS;
        this.lastRoarTarget = target;
        this.setTargetAcquireRoaring(true);

        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, EVENT_ROAR_ANIM);
            this.playTargetAcquireRoarSound();
        }
    }

    // Clears stale combat pursuit state when the server no longer has a valid target
    private void clearTargetStateIfNeeded() {
        if (this.level().isClientSide()) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (this.isValidPursuitTarget(target)) {
            return;
        }

        this.setTarget(null);
        this.lastRoarTarget = null;
        this.pursueUnlockGameTime = 0L;
        this.setTargetAcquireRoaring(false);
        this.setPursuingAcquiredTarget(false);

        if (this.getVibrationLocation() == null) {
            this.getNavigation().stop();
            this.setSpeed(0.0F);

            Vec3 movement = this.getDeltaMovement();
            this.setDeltaMovement(0.0D, movement.y, 0.0D);
        }
    }

    // Updates server-owned pursuit and roar state for client animation selection
    private void updatePursuitStateServer() {
        if (!this.level().isClientSide()) {
            boolean roaring = this.isTargetAcquireRoarActive();
            this.setTargetAcquireRoaring(roaring);
            this.setPursuingAcquiredTarget(!roaring && this.shouldPursueAcquiredTarget());
        }
    }

    // Stops navigation and horizontal movement during target-acquire roar
    private void stopTargetAcquireRoarMovement() {
        this.getNavigation().stop();
        this.setSpeed(0.0F);

        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(0.0D, movement.y, 0.0D);
    }

    // Rotates the entity body toward its target without applying model head-bone look overrides
    private void faceTargetBodyOnly(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;

        this.setYRot(yaw);
        this.setYBodyRot(yaw);
    }

    // ==================================
    //  TICKING / MOVEMENT
    // ==================================

    // Suppresses horizontal travel while the target-acquire roar is active
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isTargetAcquireRoarActive()) {
            this.stopTargetAcquireRoarMovement();
            super.travel(Vec3.ZERO);
            this.stopTargetAcquireRoarMovement();
            return;
        }

        super.travel(travelVector);
    }

    // Ticks vibration state, target state, roar immobilization, sync state, and heartbeat sound
    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel serverLevel) {
            VibrationSystem.Ticker.tick(serverLevel, this.vibrationData, this.vibrationUser);
            this.tickDarknessAura(serverLevel);
        }

        super.tick();

        this.clearTargetStateIfNeeded();

        if (this.isTargetAcquireRoarActive()) {
            this.stopTargetAcquireRoarMovement();
        }

        this.updatePursuitStateServer();

        if (!this.level().isClientSide()) {
            this.tickStepSoundServer();
        }

        if (this.level().isClientSide()) {
            this.tickHeartbeatSoundClient();
        }
    }

    // Returns true when locomotion animation should use a moving state
    private boolean isMovingForAnimation() {
        return this.getNavigation().isInProgress()
                || this.getDeltaMovement().horizontalDistanceSqr() > MOVING_HORIZONTAL_DISTANCE_SQR;
    }

    // ==================================
    //  DEATH / SCULK EFFECTS
    // ==================================

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

    // Returns whether model-level head look overrides are currently allowed
    public boolean allowCustomHeadLook() {
        return this.allowCustomHeadLook && !this.isTargetAcquireRoaring();
    }

    // ==================================
    //  SOUND
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

    // Step audio is handled by tickStepSoundServer for controlled normal and pursuit cadence
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
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

    // Plays custom step sounds using separate normal and pursuit movement cadence
    private void tickStepSoundServer() {
        if (this.isSilent() || this.isDeadOrDying() || !this.onGround() || this.isTargetAcquireRoarActive()) {
            return;
        }

        if (this.getDeltaMovement().horizontalDistanceSqr() <= MOVING_HORIZONTAL_DISTANCE_SQR) {
            return;
        }

        long gameTime = this.level().getGameTime();
        if (gameTime < this.nextStepSoundGameTime) {
            return;
        }

        boolean pursuing = this.shouldPursueAcquiredTarget();
        int interval = pursuing ? PURSUIT_STEP_SOUND_INTERVAL_TICKS : NORMAL_STEP_SOUND_INTERVAL_TICKS;
        float pitch = pursuing ? 1.05F : STEP_PITCH;

        this.nextStepSoundGameTime = gameTime + interval;

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                ModSounds.ENTITY_GLOAM_EYED_AMALGAM_STEP.get(),
                this.getSoundSource(),
                STEP_VOLUME,
                pitch
        );
    }

    // Plays the roar sound at the same moment the target-acquire roar animation starts
    private void playTargetAcquireRoarSound() {
        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                ModSounds.ENTITY_GLOAM_EYED_AMALGAM_ROAR.get(),
                this.getSoundSource(),
                ROAR_VOLUME,
                ROAR_PITCH
        );
    }

    // Plays server-side feedback for an accepted vibration
    private void playVibrationReactionSound(ServerLevel level) {
        if (this.isSilent()) {
            return;
        }

        level.playSound(
                null,
                this.blockPosition(),
                ModSounds.ENTITY_LIVING_SCULK_VIBRATION_REACT.get(),
                this.getSoundSource(),
                1.0F,
                1.0F
        );
    }

    // ==================================
    //  GECKOLIB ANIMATION
    // ==================================

    // Registers GeckoLib animation controllers
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, CTRL_LOCOMOTION, 4, state -> {
                    if (this.isMovingForAnimation()) {
                        this.allowCustomHeadLook = true;

                        if (this.isPursuingAcquiredTarget()) {
                            return state.setAndContinue(GloamEyedAmalgamAnimations.WALKING_PURSUIT);
                        }

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

                new AnimationController<>(this, CTRL_VIBRATION, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_VIBRATION_REACT, GloamEyedAmalgamAnimations.VIBRATION_REACT),

                new AnimationController<>(this, CTRL_ATTACK, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_ATTACK, GloamEyedAmalgamAnimations.ATTACK),

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
    @Nullable
    public BlockPos getVibrationLocation() {
        return this.vibrationLocation;
    }

    // Stores or clears the most recently received vibration location
    @Override
    public void setVibrationLocation(@Nullable BlockPos pos) {
        this.vibrationLocation = pos;
    }

    // Indicates this entity dampens vibration propagation
    @Override
    public boolean dampensVibrations() {
        return true;
    }

    // ==================================
    //  SHARED HELPERS
    // ==================================

    // Returns true when the target can still be pursued
    private boolean isValidPursuitTarget(@Nullable LivingEntity target) {
        return target != null
                && target.isAlive()
                && !target.isRemoved()
                && !isCreativePlayer(target);
    }

    // Returns synced client-visible roar state for animation-safe head control
    private boolean isTargetAcquireRoaring() {
        return this.entityData.get(DATA_TARGET_ACQUIRE_ROARING);
    }

    // Syncs target-acquire roar state to clients only when the value changes
    private void setTargetAcquireRoaring(boolean roaring) {
        if (this.entityData.get(DATA_TARGET_ACQUIRE_ROARING) != roaring) {
            this.entityData.set(DATA_TARGET_ACQUIRE_ROARING, roaring);
        }
    }

    // Returns true if the entity is a creative-mode player
    private static boolean isCreativePlayer(@Nullable Entity entity) {
        return entity instanceof Player player && player.isCreative();
    }

    // Returns true when this entity type is marked vibration-friendly
    private boolean isVibrationFriendlySelf() {
        return this.getType().is(ModEntityTypeTags.VIBRATION_FRIENDLY);
    }

    // Returns true when the source entity type is marked vibration-friendly
    private static boolean isVibrationFriendlyEntity(@Nullable Entity entity) {
        return entity != null && entity.getType().is(ModEntityTypeTags.VIBRATION_FRIENDLY);
    }

    // Returns the player responsible for a vibration, preferring the direct source over projectile owner
    @Nullable
    private static Player getVibrationPlayer(@Nullable Entity sourceEntity, @Nullable Entity projectileOwner) {
        if (sourceEntity instanceof Player player) {
            return player;
        }

        if (projectileOwner instanceof Player player) {
            return player;
        }

        return null;
    }

    // Applies Warden-style darkness to nearby survival players at a fixed interval
    private void tickDarknessAura(ServerLevel serverLevel) {
        if ((this.tickCount + this.getId()) % DARKNESS_INTERVAL_TICKS == 0) {
            MobEffectInstance darkness = new MobEffectInstance(
                    MobEffects.DARKNESS,
                    DARKNESS_DURATION_TICKS,
                    0,
                    false,
                    false
            );

            MobEffectUtil.addEffectToPlayersAround(
                    serverLevel,
                    this,
                    this.position(),
                    DARKNESS_RADIUS_BLOCKS,
                    darkness,
                    DARKNESS_DISPLAY_LIMIT_TICKS
            );
        }
    }

    // ==================================
    //  PROJECTILE ATTACK HELPERS
    // ==================================

    // Returns the estimated world position of the floating eye projectile source
    private Vec3 getSculkShotSourcePosition(LivingEntity target) {
        Vec3 toTarget = target.position().subtract(this.position());
        Vec3 horizontal = new Vec3(toTarget.x, 0.0D, toTarget.z);

        if (horizontal.lengthSqr() < 1.0E-7D) {
            horizontal = Vec3.directionFromRotation(0.0F, this.getYRot());
        } else {
            horizontal = horizontal.normalize();
        }

        return this.position().add(
                horizontal.x * SCULK_SHOT_EYE_FORWARD_OFFSET,
                SCULK_SHOT_EYE_HEIGHT_OFFSET,
                horizontal.z * SCULK_SHOT_EYE_FORWARD_OFFSET
        );
    }

    // Fires a custom shulker-style homing sculk shot from the estimated floating eye position
    private void fireSculkShotAt(LivingEntity target) {
        if (this.level().isClientSide()) {
            return;
        }

        Vec3 source = this.getSculkShotSourcePosition(target);

        GloamEyedAmalgamSculkShotEntity projectile = new GloamEyedAmalgamSculkShotEntity(
                this.level(),
                this,
                target,
                Direction.Axis.Y,
                source
        );

        this.level().addFreshEntity(projectile);
    }

    // Returns true when melee can already reach the target
    private boolean isWithinMeleeAttackReach(LivingEntity target) {
        double distToTargetSqr = this.distanceToSqr(target.getX(), target.getY(), target.getZ());
        return distToTargetSqr <= this.getExtendedMeleeReachSqr(target);
    }

    // Returns the entity's extended melee reach squared
    private double getExtendedMeleeReachSqr(LivingEntity target) {
        double baseReachSqr = (this.getBbWidth() * 2.0F) * (this.getBbWidth() * 2.0F) + target.getBbWidth();
        double bonusReachSqr = MELEE_REACH_BONUS_BLOCKS * MELEE_REACH_BONUS_BLOCKS;
        return baseReachSqr + bonusReachSqr;
    }

    // Returns true when navigation can build a complete path to the target
    private boolean hasReachablePathTo(LivingEntity target) {
        Path path = this.getNavigation().createPath(target, 0);
        return path != null && path.canReach();
    }

    // ==================================
    //  GOAL IMPLEMENTATIONS
    // ==================================

    // Tracks newly acquired targets and holds movement until the roar animation ends
    private static class TargetAcquireRoarGoal extends Goal {
        private final GloamEyedAmalgamEntity mob;

        // Creates the target-acquire roar gate goal
        private TargetAcquireRoarGoal(GloamEyedAmalgamEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        // Starts when there is a new live target that has not triggered roar yet
        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            return this.mob.isValidPursuitTarget(target) && target != this.mob.lastRoarTarget;
        }

        // Applies roar lock and animation trigger
        @Override
        public void start() {
            LivingEntity target = this.mob.getTarget();
            if (this.mob.isValidPursuitTarget(target)) {
                this.mob.beginRoarForTarget(target);
            }
        }

        // Keeps MOVE, LOOK, and JUMP locked until the roar timer ends
        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mob.getTarget();
            return this.mob.isValidPursuitTarget(target)
                    && target == this.mob.lastRoarTarget
                    && this.mob.isTargetAcquireRoarActive();
        }

        // Keeps the mob stationary and body-facing its target during the roar
        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (this.mob.isValidPursuitTarget(target)) {
                this.mob.faceTargetBodyOnly(target);
            }

            this.mob.stopTargetAcquireRoarMovement();
        }

        // Stops roar navigation lock when the target-acquire roar goal ends
        @Override
        public void stop() {
            super.stop();
            this.mob.getNavigation().stop();
        }
    }

    // Handles melee pursuit with extended reach after the target-acquire roar unlocks movement
    private static class GloamEyedAmalgamMeleeAttackGoal extends MeleeAttackGoal {
        private final GloamEyedAmalgamEntity mob;

        // Creates the melee attack goal with pursuit movement speed
        private GloamEyedAmalgamMeleeAttackGoal(GloamEyedAmalgamEntity mob) {
            super(mob, PURSUIT_MOVE_SPEED_MODIFIER, false);
            this.mob = mob;
        }

        // Starts melee pathing only after the roar lock ends
        @Override
        public boolean canUse() {
            return this.mob.shouldPursueAcquiredTarget() && super.canUse();
        }

        // Continues melee pathing only after the roar lock ends
        @Override
        public boolean canContinueToUse() {
            return this.mob.shouldPursueAcquiredTarget() && super.canContinueToUse();
        }

        // Extends melee range and preserves swing-based attack animation trigger
        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (!this.mob.isPursueUnlocked()) {
                return;
            }

            if (this.mob.isWithinMeleeAttackReach(target) && this.isTimeToAttack()) {
                this.resetAttackCooldown();
                this.mob.swing(InteractionHand.MAIN_HAND);
                this.mob.doHurtTarget(target);
            }
        }
    }

    // Moves toward the current attack target only after the target-acquire roar unlocks pursuit
    private static class GloamEyedAmalgamPursuitGoal extends MoveTowardsTargetGoal {
        private final GloamEyedAmalgamEntity mob;

        // Creates the target pursuit goal with pursuit movement speed
        private GloamEyedAmalgamPursuitGoal(GloamEyedAmalgamEntity mob) {
            super(mob, PURSUIT_MOVE_SPEED_MODIFIER, PURSUIT_TARGET_MAX_DISTANCE);
            this.mob = mob;
        }

        // Starts pursuit only after the roar lock ends
        @Override
        public boolean canUse() {
            return this.mob.shouldPursueAcquiredTarget() && super.canUse();
        }

        // Continues pursuit only after the roar lock ends
        @Override
        public boolean canContinueToUse() {
            return this.mob.shouldPursueAcquiredTarget() && super.canContinueToUse();
        }
    }

    // Fires a sculk shot when the current target cannot be reached by pathing for several seconds
    private static class GloamEyedAmalgamUnreachableRangedAttackGoal extends Goal {
        private final GloamEyedAmalgamEntity mob;

        private int unreachableTicks;
        private long nextPathCheckGameTime;
        private long nextAttackGameTime;

        // Creates the unreachable-target ranged attack monitor goal
        private GloamEyedAmalgamUnreachableRangedAttackGoal(GloamEyedAmalgamEntity mob) {
            this.mob = mob;
        }

        // Runs while the mob has completed its roar and has a valid pursuit target
        @Override
        public boolean canUse() {
            return this.mob.shouldPursueAcquiredTarget();
        }

        // Continues while the mob has completed its roar and has a valid pursuit target
        @Override
        public boolean canContinueToUse() {
            return this.mob.shouldPursueAcquiredTarget();
        }

        // Resets unreachable tracking when the monitor starts
        @Override
        public void start() {
            this.unreachableTicks = 0;
            this.nextPathCheckGameTime = 0L;
        }

        // Clears unreachable tracking when the monitor stops
        @Override
        public void stop() {
            this.unreachableTicks = 0;
            this.nextPathCheckGameTime = 0L;
        }

        // Tracks path reachability and fires the ranged attack when the target remains unreachable
        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (!this.mob.isValidPursuitTarget(target)) {
                this.unreachableTicks = 0;
                return;
            }

            long gameTime = this.mob.level().getGameTime();
            if (gameTime < this.nextPathCheckGameTime) {
                return;
            }

            this.nextPathCheckGameTime = gameTime + UNREACHABLE_PATH_CHECK_INTERVAL_TICKS;

            if (this.mob.isWithinMeleeAttackReach(target) || this.mob.hasReachablePathTo(target)) {
                this.unreachableTicks = 0;
                return;
            }

            this.unreachableTicks += UNREACHABLE_PATH_CHECK_INTERVAL_TICKS;

            if (this.unreachableTicks < UNREACHABLE_RANGED_ATTACK_TICKS) {
                return;
            }

            if (gameTime < this.nextAttackGameTime) {
                return;
            }

            this.nextAttackGameTime = gameTime + SCULK_SHOT_COOLDOWN_TICKS;
            this.unreachableTicks = 0;
            this.mob.fireSculkShotAt(target);
        }
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

            return level.getGameTime() >= GloamEyedAmalgamEntity.this.nextVibrationGameTime;
        }

        // Handles accepted vibration events and updates behavior and animation
        @Override
        public void onReceiveVibration(
                ServerLevel level,
                BlockPos pos,
                Holder<GameEvent> gameEvent,
                Entity sourceEntity,
                Entity projectileOwner,
                float distance
        ) {
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

            Player playerToTarget = getVibrationPlayer(sourceEntity, projectileOwner);
            if (playerToTarget != null) {
                double maxDistSqr = VIBRATION_PLAYER_ACQUIRE_RANGE * VIBRATION_PLAYER_ACQUIRE_RANGE;
                if (GloamEyedAmalgamEntity.this.distanceToSqr(playerToTarget) <= maxDistSqr) {
                    GloamEyedAmalgamEntity.this.setTarget(playerToTarget);
                }
            }

            GloamEyedAmalgamEntity.this.level().broadcastEntityEvent(GloamEyedAmalgamEntity.this, EVENT_VIBRATION_REACT_ANIM);
            GloamEyedAmalgamEntity.this.playVibrationReactionSound(level);
        }
    }
}
