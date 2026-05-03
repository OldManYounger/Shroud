package net.oldmanyounger.shroud.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
    //  PULL AURA
    // ==================================

    // Time the target must remain unreachable before pull aura windup begins
    private static final int UNREACHABLE_PULL_AURA_TICKS = 360;

    // Delay between pull aura animation start and active pull force
    private static final int PULL_AURA_WINDUP_TICKS = 30;

    // Maximum distance where the pull aura affects the current target
    private static final double PULL_AURA_RADIUS_BLOCKS = 20.0D;

    // Distance where the pull aura reaches its strongest intended pull
    private static final double PULL_AURA_CLOSE_DISTANCE_BLOCKS = 5.0D;

    // Distance under which pull calculations stop to avoid center jitter
    private static final double PULL_AURA_MIN_DISTANCE_BLOCKS = 0.75D;

    // Pull force applied near the outer edge of the aura
    private static final double PULL_AURA_FAR_STRENGTH = 0.010D;

    // Pull force applied near the close-distance threshold
    private static final double PULL_AURA_CLOSE_STRENGTH = 0.085D;

    // Vertical pull multiplier keeps the pull aimed low without launching targets
    private static final double PULL_AURA_VERTICAL_MULTIPLIER = 0.35D;

    // Maximum inward horizontal speed caused by the aura
    private static final double PULL_AURA_MAX_INWARD_SPEED = 0.55D;

    // Height factor used for pulling targets toward the entity's lower half
    private static final double PULL_AURA_CENTER_HEIGHT_FACTOR = 0.35D;

    // Interval between inward particle ring spawns
    private static final int PULL_AURA_PARTICLE_INTERVAL_TICKS = 4;

    // Sparse particle count per ring pulse
    private static final int PULL_AURA_PARTICLES_PER_RING = 14;

    // Ring radius used by the inward-moving particle indicator
    private static final double PULL_AURA_PARTICLE_RING_RADIUS = 4.25D;

    // Small vertical scatter applied to pull aura ring particles
    private static final double PULL_AURA_PARTICLE_Y_VARIANCE = 0.45D;

    // Inward particle travel speed
    private static final double PULL_AURA_PARTICLE_INWARD_SPEED = 0.17D;


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

    // Arise sound loudness
    private static final float ARISE_VOLUME = 3.0F;

    // Arise sound pitch
    private static final float ARISE_PITCH = 1.0F;

    // ==================================
    //  ANIMATION / SYNC IDS
    // ==================================

    // Entity event IDs for client animation triggers
    private static final byte EVENT_ATTACK_ANIM = 60;
    private static final byte EVENT_VIBRATION_REACT_ANIM = 5;
    private static final byte EVENT_ROAR_ANIM = 61;
    private static final byte EVENT_PULL_AURA_ANIM = 62;

    // Duration to lock all actions while the arise animation plays
    private static final int ARISE_ANIMATION_TICKS = 170;

    // Interval between block-level arise particle bursts
    private static final int ARISE_PARTICLE_INTERVAL_TICKS = 2;

    // GeckoLib animation controller names
    private static final String CTRL_LOCOMOTION = "gloam_eyed_amalgam_locomotion_controller";
    private static final String CTRL_VIBRATION = "gloam_eyed_amalgam_vibration_controller";
    private static final String CTRL_ATTACK = "gloam_eyed_amalgam_attack_controller";
    private static final String CTRL_ROAR = "gloam_eyed_amalgam_roar_controller";
    private static final String CTRL_PULL_AURA = "gloam_eyed_amalgam_pull_aura_controller";
    private static final String CTRL_ARISE = "gloam_eyed_amalgam_arise_controller";

    // GeckoLib trigger names
    private static final String TRIG_ATTACK = "attack";
    private static final String TRIG_VIBRATION_REACT = "vibration_react";
    private static final String TRIG_ROAR = "roar";
    private static final String TRIG_PULL_AURA = "pull_aura";

    // Synced client animation state for target pursuit
    private static final EntityDataAccessor<Boolean> DATA_PURSUING_ACQUIRED_TARGET =
            SynchedEntityData.defineId(GloamEyedAmalgamEntity.class, EntityDataSerializers.BOOLEAN);

    // Synced client animation state for target-acquire roar
    private static final EntityDataAccessor<Boolean> DATA_TARGET_ACQUIRE_ROARING =
            SynchedEntityData.defineId(GloamEyedAmalgamEntity.class, EntityDataSerializers.BOOLEAN);

    // Synced client animation state for shrieker-summoned emergence
    private static final EntityDataAccessor<Boolean> DATA_ARISING =
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

    // Game time when the arise animation lock should end
    private long ariseEndGameTime = 0L;

    // Game time when the pull aura windup should become an active pull
    private long pullAuraActivateGameTime = 0L;

    // Earliest game time at which pull aura particles can pulse again
    private long nextPullAuraParticleGameTime = 0L;

    // Tracks whether the pull aura is actively applying force
    private boolean pullAuraActive = false;

    // Target that caused and owns the current pull aura state
    @Nullable
    private LivingEntity pullAuraTarget;

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
        builder.define(DATA_ARISING, false);
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
        this.goalSelector.addGoal(0, new AriseLockGoal(this));
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

    // ============================
    // MISC HELPERS
    // ============================

    // Prevents this boss entity from despawning due to player distance
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    // Starts the shrieker-summoned arise animation lock
    public void beginAriseFromSummon() {
        if (this.level().isClientSide()) return;

        this.setArising(true);
        this.ariseEndGameTime = this.level().getGameTime() + ARISE_ANIMATION_TICKS;
        this.setTarget(null);
        this.lastRoarTarget = null;
        this.clearPullAuraState();
        this.setTargetAcquireRoaring(false);
        this.setPursuingAcquiredTarget(false);
        this.stopAriseMovement();
        this.playAriseSound();
    }

    // Returns whether the entity is currently locked in its arise animation
    public boolean isArising() {
        return this.entityData.get(DATA_ARISING);
    }

    // Syncs arise animation state to clients
    private void setArising(boolean arising) {
        if (this.entityData.get(DATA_ARISING) != arising) {
            this.entityData.set(DATA_ARISING, arising);
        }
    }

    // Stops movement while the entity is crawling out
    private void stopAriseMovement() {
        this.getNavigation().stop();
        this.setSpeed(0.0F);
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
    }

    // Ticks arise particles and unlocks the entity when the animation window ends
    private void tickAriseServer(ServerLevel level) {
        long gameTime = level.getGameTime();

        if (gameTime % ARISE_PARTICLE_INTERVAL_TICKS == 0L) {
            this.spawnAriseParticles(level);
        }

        if (gameTime >= this.ariseEndGameTime) {
            this.setArising(false);
            this.ariseEndGameTime = 0L;
        }
    }

    // Spawns block-level debris and sculk soul particles around the emergence point
    private void spawnAriseParticles(ServerLevel level) {
        BlockPos basePos = this.blockPosition().below();
        BlockState baseState = level.getBlockState(basePos);

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, baseState),
                this.getX(),
                this.getY() + 0.05D,
                this.getZ(),
                18,
                0.75D,
                0.08D,
                0.75D,
                0.12D
        );

        level.sendParticles(
                ParticleTypes.SCULK_SOUL,
                this.getX(),
                this.getY() + 0.15D,
                this.getZ(),
                6,
                0.35D,
                0.05D,
                0.35D,
                0.02D
        );
    }

    // Spawns an immediate heavier particle burst when the entity first begins emerging
    public void spawnInitialAriseParticles(ServerLevel level) {
        this.spawnAriseParticles(level);
        this.spawnAriseParticles(level);
        this.spawnAriseParticles(level);
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
        this.clearPullAuraState();
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
        this.clearPullAuraState();

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
        if (this.isArising() || this.isTargetAcquireRoarActive()) {
            this.stopAriseMovement();
            super.travel(Vec3.ZERO);
            this.stopAriseMovement();
            return;
        }

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
        if (this.level() instanceof ServerLevel serverLevel && !this.isArising()) {
            VibrationSystem.Ticker.tick(serverLevel, this.vibrationData, this.vibrationUser);
            this.tickDarknessAura(serverLevel);
        }

        super.tick();

        if (this.isArising()) {
            this.setTarget(null);
            this.lastRoarTarget = null;
            this.clearPullAuraState();
            this.setTargetAcquireRoaring(false);
            this.setPursuingAcquiredTarget(false);
            this.stopAriseMovement();

            if (this.level() instanceof ServerLevel serverLevel) {
                this.tickAriseServer(serverLevel);
            }

            return;
        }

        this.clearTargetStateIfNeeded();

        if (this.isTargetAcquireRoarActive()) {
            this.stopTargetAcquireRoarMovement();
        }

        this.updatePursuitStateServer();

        if (this.level() instanceof ServerLevel serverLevel) {
            this.tickPullAuraServer(serverLevel);
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

        if (id == EVENT_PULL_AURA_ANIM) {
            this.triggerAnim(CTRL_PULL_AURA, TRIG_PULL_AURA);
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
        return this.allowCustomHeadLook && !this.isTargetAcquireRoaring() && !this.isArising();
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

    // Plays the emergence sound when the shrieker-summoned arise sequence begins
    private void playAriseSound() {
        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                ModSounds.ENTITY_GLOAM_EYED_AMALGAM_ARISE.get(),
                this.getSoundSource(),
                ARISE_VOLUME,
                ARISE_PITCH
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
                    if (this.isArising()) {
                        this.allowCustomHeadLook = false;
                        return PlayState.STOP;
                    }

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

                new AnimationController<>(this, CTRL_ARISE, 0, state -> {
                    if (!this.isArising()) {
                        return PlayState.STOP;
                    }

                    this.allowCustomHeadLook = false;
                    return state.setAndContinue(GloamEyedAmalgamAnimations.ARISE);
                }),

                new AnimationController<>(this, CTRL_VIBRATION, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_VIBRATION_REACT, GloamEyedAmalgamAnimations.VIBRATION_REACT),

                new AnimationController<>(this, CTRL_ATTACK, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_ATTACK, GloamEyedAmalgamAnimations.ATTACK),

                new AnimationController<>(this, CTRL_ROAR, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_ROAR, GloamEyedAmalgamAnimations.ROAR),

                new AnimationController<>(this, CTRL_PULL_AURA, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_PULL_AURA, GloamEyedAmalgamAnimations.PULL_AURA)
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
//  PULL AURA HELPERS
// ==================================

    // Starts the pull aura windup without interrupting movement or attacks
    private void beginPullAuraWindup(LivingEntity target) {
        if (this.isPullAuraStartedFor(target)) {
            return;
        }

        this.pullAuraTarget = target;
        this.pullAuraActive = false;
        this.pullAuraActivateGameTime = this.level().getGameTime() + PULL_AURA_WINDUP_TICKS;
        this.nextPullAuraParticleGameTime = 0L;

        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, EVENT_PULL_AURA_ANIM);
            this.playPullAuraWindupSound();
        }
    }

    // Returns true when the current pull aura belongs to the supplied target
    private boolean isPullAuraStartedFor(LivingEntity target) {
        return this.pullAuraTarget == target
                && (this.pullAuraActive || this.pullAuraActivateGameTime > 0L);
    }

    // Clears all pull aura state
    private void clearPullAuraState() {
        this.pullAuraTarget = null;
        this.pullAuraActive = false;
        this.pullAuraActivateGameTime = 0L;
        this.nextPullAuraParticleGameTime = 0L;
    }

    // Ticks the active pull aura after its windup has completed
    private void tickPullAuraServer(ServerLevel level) {
        LivingEntity target = this.getTarget();

        if (!this.isValidPursuitTarget(target)
                || target != this.pullAuraTarget
                || !this.shouldPursueAcquiredTarget()) {
            this.clearPullAuraState();
            return;
        }

        long gameTime = level.getGameTime();
        if (!this.pullAuraActive) {
            if (gameTime < this.pullAuraActivateGameTime) {
                return;
            }

            this.pullAuraActive = true;
        }

        this.applyPullAuraTo(target);
        this.spawnPullAuraParticles(level);
    }

    // Applies distance-scaled pull force toward the entity's lower body
    private void applyPullAuraTo(LivingEntity target) {
        Vec3 center = this.getPullAuraCenter();
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 toCenter = center.subtract(targetCenter);
        double distance = toCenter.length();

        if (distance < PULL_AURA_MIN_DISTANCE_BLOCKS || distance > PULL_AURA_RADIUS_BLOCKS) {
            return;
        }

        Vec3 direction = toCenter.normalize();
        double clampedDistance = Mth.clamp(
                distance,
                PULL_AURA_CLOSE_DISTANCE_BLOCKS,
                PULL_AURA_RADIUS_BLOCKS
        );
        double closeness = 1.0D - ((clampedDistance - PULL_AURA_CLOSE_DISTANCE_BLOCKS)
                / (PULL_AURA_RADIUS_BLOCKS - PULL_AURA_CLOSE_DISTANCE_BLOCKS));
        double strength = Mth.lerp(closeness, PULL_AURA_FAR_STRENGTH, PULL_AURA_CLOSE_STRENGTH);

        Vec3 pull = new Vec3(
                direction.x * strength,
                direction.y * strength * PULL_AURA_VERTICAL_MULTIPLIER,
                direction.z * strength
        );

        Vec3 nextMovement = target.getDeltaMovement().add(pull);
        nextMovement = this.limitPullAuraInwardSpeed(nextMovement, direction);

        target.setDeltaMovement(nextMovement);
        target.hasImpulse = true;
        this.syncPullAuraMovement(target);
    }

    // Sends forced pull movement to server players so client-side movement prediction receives the new velocity.
    private void syncPullAuraMovement(LivingEntity target) {
        if (target instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
        }
    }

    // Caps only inward horizontal speed so outside movement remains responsive
    private Vec3 limitPullAuraInwardSpeed(Vec3 movement, Vec3 pullDirection) {
        Vec3 horizontalDirection = new Vec3(pullDirection.x, 0.0D, pullDirection.z);

        if (horizontalDirection.lengthSqr() < 1.0E-7D) {
            return movement;
        }

        horizontalDirection = horizontalDirection.normalize();

        double inwardSpeed = movement.x * horizontalDirection.x + movement.z * horizontalDirection.z;
        if (inwardSpeed <= PULL_AURA_MAX_INWARD_SPEED) {
            return movement;
        }

        double excessSpeed = inwardSpeed - PULL_AURA_MAX_INWARD_SPEED;
        return movement.subtract(
                horizontalDirection.x * excessSpeed,
                0.0D,
                horizontalDirection.z * excessSpeed
        );
    }

    // Returns the world point the aura pulls toward
    private Vec3 getPullAuraCenter() {
        return new Vec3(
                this.getX(),
                this.getY() + this.getBbHeight() * PULL_AURA_CENTER_HEIGHT_FACTOR,
                this.getZ()
        );
    }

    // Spawns a sparse ring of particles that drift inward toward the pull center
    private void spawnPullAuraParticles(ServerLevel level) {
        long gameTime = level.getGameTime();

        if (gameTime < this.nextPullAuraParticleGameTime) {
            return;
        }

        this.nextPullAuraParticleGameTime = gameTime + PULL_AURA_PARTICLE_INTERVAL_TICKS;

        Vec3 center = this.getPullAuraCenter();
        double phase = (gameTime % 360L) * Mth.DEG_TO_RAD;

        for (int i = 0; i < PULL_AURA_PARTICLES_PER_RING; i++) {
            double angle = phase + (Mth.TWO_PI * i / PULL_AURA_PARTICLES_PER_RING);
            double x = center.x + Math.cos(angle) * PULL_AURA_PARTICLE_RING_RADIUS;
            double y = center.y + 0.15D + (this.getRandom().nextDouble() - 0.5D) * PULL_AURA_PARTICLE_Y_VARIANCE;
            double z = center.z + Math.sin(angle) * PULL_AURA_PARTICLE_RING_RADIUS;

            Vec3 particlePos = new Vec3(x, y, z);
            Vec3 velocity = center.subtract(particlePos).normalize().scale(PULL_AURA_PARTICLE_INWARD_SPEED);

            level.sendParticles(
                    ParticleTypes.SCULK_SOUL,
                    x,
                    y,
                    z,
                    0,
                    velocity.x,
                    velocity.y,
                    velocity.z,
                    1.0D
            );
        }
    }

    // Plays feedback when the pull aura windup begins
    private void playPullAuraWindupSound() {
        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.WARDEN_SONIC_CHARGE,
                this.getSoundSource(),
                1.6F,
                0.65F
        );
    }

    // ==================================
    //  GOAL IMPLEMENTATIONS
    // ==================================

    // Locks movement, looking, and jumping while the arise animation plays
    private static class AriseLockGoal extends Goal {
        private final GloamEyedAmalgamEntity mob;

        // Creates the arise lock goal
        private AriseLockGoal(GloamEyedAmalgamEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        // Runs while the entity is arising
        @Override
        public boolean canUse() {
            return this.mob.isArising();
        }

        // Continues while the entity is arising
        @Override
        public boolean canContinueToUse() {
            return this.mob.isArising();
        }

        // Keeps the entity fixed in place
        @Override
        public void tick() {
            this.mob.stopAriseMovement();
        }
    }

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
            if (this.mob.isArising()) return false;
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
            if (this.mob.isArising()) return false;

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
            if (this.mob.isArising()) return false;
            return this.mob.shouldPursueAcquiredTarget() && super.canUse();
        }

        // Continues melee pathing only after the roar lock ends
        @Override
        public boolean canContinueToUse() {
            if (this.mob.isArising()) return false;
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
            if (this.mob.isArising()) return false;
            return this.mob.shouldPursueAcquiredTarget() && super.canUse();
        }

        // Continues pursuit only after the roar lock ends
        @Override
        public boolean canContinueToUse() {
            if (this.mob.isArising()) return false;
            return this.mob.shouldPursueAcquiredTarget() && super.canContinueToUse();
        }
    }

    // Fires a sculk shot and later starts pull aura when the target remains unreachable
    private static class GloamEyedAmalgamUnreachableRangedAttackGoal extends Goal {
        private final GloamEyedAmalgamEntity mob;

        @Nullable
        private LivingEntity trackedTarget;

        private int unreachableTicks;
        private long nextPathCheckGameTime;
        private long nextAttackGameTime;

        // Creates the unreachable-target ranged attack and pull aura monitor goal
        private GloamEyedAmalgamUnreachableRangedAttackGoal(GloamEyedAmalgamEntity mob) {
            this.mob = mob;
        }

        // Runs while the mob has completed its roar and has a valid pursuit target
        @Override
        public boolean canUse() {
            if (this.mob.isArising()) return false;
            return this.mob.shouldPursueAcquiredTarget();
        }

        // Continues while the mob has completed its roar and has a valid pursuit target
        @Override
        public boolean canContinueToUse() {
            if (this.mob.isArising()) return false;
            return this.mob.shouldPursueAcquiredTarget();
        }

        // Resets unreachable tracking when the monitor starts
        @Override
        public void start() {
            this.trackedTarget = null;
            this.unreachableTicks = 0;
            this.nextPathCheckGameTime = 0L;
        }

        // Clears unreachable tracking when the monitor stops
        @Override
        public void stop() {
            this.trackedTarget = null;
            this.unreachableTicks = 0;
            this.nextPathCheckGameTime = 0L;
        }

        // Tracks path reachability, fires ranged shots, and escalates into pull aura
        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (!this.mob.isValidPursuitTarget(target)) {
                this.trackedTarget = null;
                this.unreachableTicks = 0;
                return;
            }

            if (target != this.trackedTarget) {
                this.trackedTarget = target;
                this.unreachableTicks = 0;
                this.nextPathCheckGameTime = 0L;
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

            if (this.unreachableTicks >= UNREACHABLE_PULL_AURA_TICKS
                    && !this.mob.isPullAuraStartedFor(target)) {
                this.mob.beginPullAuraWindup(target);
            }

            if (this.unreachableTicks < UNREACHABLE_RANGED_ATTACK_TICKS) {
                return;
            }

            if (gameTime < this.nextAttackGameTime) {
                return;
            }

            this.nextAttackGameTime = gameTime + SCULK_SHOT_COOLDOWN_TICKS;
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
