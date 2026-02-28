package net.oldmanyounger.shroud.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.oldmanyounger.shroud.entity.client.UmbralHowlerAnimations;
import net.oldmanyounger.shroud.entity.goal.VibrationGoal;
import net.oldmanyounger.shroud.entity.goal.VibrationListener;
import net.oldmanyounger.shroud.sound.ModSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

/**
 * Represents the custom Umbral Howler hostile entity for the Shroud mod.
 * <p>
 * This entity:
 * <ul>
 *   <li>Extends {@link Monster} to participate in vanilla hostile mob AI and combat systems</li>
 *   <li>Implements {@link GeoEntity} to drive animations through GeckoLib</li>
 *   <li>Implements {@link VibrationListener} and {@link VibrationSystem} to react to
 *       in-world game events such as footsteps and projectiles via the vibration system</li>
 * </ul>
 */
public class UmbralHowlerEntity extends Monster implements GeoEntity, VibrationListener, VibrationSystem {

    // ============================================================
    //  CONSTANTS
    // ============================================================

    // Cooldown in ticks before reacting to another vibration (20 ticks = 1 second)
    private static final int VIBRATION_COOLDOWN_TICKS = 80;

    // Passive detection (no vibration): only aggro if the player is extremely close
    private static final double PASSIVE_PLAYER_DETECT_RANGE = 2.0D;

    // Vibration-triggered acquire range (must be within this range when the vibration is processed)
    private static final double VIBRATION_PLAYER_ACQUIRE_RANGE = 12.0D;

    // Entity event id used to sync an attack animation to clients
    private static final byte EVENT_ATTACK_ANIM = 60;

    // Entity event id used to sync a vibration reaction animation to clients
    private static final byte EVENT_VIBRATION_REACT_ANIM = 61;

    private static final String CTRL_LOCOMOTION = "umbral_howler_locomotion_controller";
    private static final String CTRL_VIBRATION = "umbral_howler_vibration_controller";
    private static final String CTRL_ATTACK = "umbral_howler_attack_controller";

    private static final String TRIG_ATTACK = "attack";
    private static final String TRIG_VIBRATION_REACT = "vibration_react";

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
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 15.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    // ============================================================
    //  AI GOALS
    // ============================================================

    // Registers movement, combat, and vibration-based AI goals
    @Override
    protected void registerGoals() {
        // Movement and combat
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Lunge must have higher priority than melee, or it won't reliably start
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.4F));

        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.2D, 25));

        // Pursue the most recent vibration location
        this.goalSelector.addGoal(4, new VibrationGoal(this, 0.8D));

        // Idle behavior
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        // Retaliation and target selection
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

    // Handles custom death behavior including particles, sculk spread, and loot
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

        // Spawn soul-like sculk particles at the death position
        serverLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                this.getX(),
                this.getY() + 0.5,
                this.getZ(),
                20,
                0.4,
                0.4,
                0.4,
                0.02
        );

        // Drop an echo shard as a thematic reward
        this.spawnAtLocation(Items.ECHO_SHARD);
    }

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

    // ============================================================
    //  COMBAT / ENTITY EVENTS
    // ============================================================

    @Override
    public void swing(InteractionHand hand, boolean updateSelf) {
        super.swing(hand, updateSelf);

        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_ANIM);
        }
    }

    // Handles melee attack logic and plays the associated attack sound
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);

        if (result) {
            Level level = this.level();

            level.playSound(
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

    // Handles entity events and routes them to the appropriate GeckoLib animations
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

        super.handleEntityEvent(id);
    }

    // ============================================================
    //  SOUND OVERRIDES
    // ============================================================

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_UMBRAL_HOWLER_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 280;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_UMBRAL_HOWLER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_UMBRAL_HOWLER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.ENTITY_UMBRAL_HOWLER_STEP.get(), 0.15F, 1.0F);
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
                // Base locomotion controller (broad animations first)
                new AnimationController<>(this, CTRL_LOCOMOTION, 4, state -> {
                    boolean moving = this.getNavigation().isInProgress()
                            || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;

                    if (moving) {
                        return state.setAndContinue(UmbralHowlerAnimations.WALKING);
                    }

                    if (state.isCurrentAnimation(UmbralHowlerAnimations.IDLE_SPIKE)) {
                        return state.setAndContinue(UmbralHowlerAnimations.IDLE_SPIKE);
                    }

                    if (this.getRandom().nextInt(1800) == 0) {
                        return state.setAndContinue(UmbralHowlerAnimations.IDLE_SPIKE);
                    }

                    return state.setAndContinue(UmbralHowlerAnimations.IDLE);
                }),

                // Vibration react overlay (register after locomotion so it can override overlapping bones)
                new AnimationController<>(this, CTRL_VIBRATION, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_VIBRATION_REACT, UmbralHowlerAnimations.VIBRATION_REACT),

                // Attack overlay (register last so it can override overlapping bones)
                new AnimationController<>(this, CTRL_ATTACK, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_ATTACK, UmbralHowlerAnimations.ATTACK)
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
     */
    private class UmbralHowlerVibrationUser implements VibrationSystem.User {

        // Position source used to track the listener at the entity's eye height
        private final PositionSource positionSource =
                new EntityPositionSource(UmbralHowlerEntity.this, UmbralHowlerEntity.this.getEyeHeight());

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public int getListenerRadius() {
            return 12;
        }

        @Override
        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.WARDEN_CAN_LISTEN;
        }

        @Override
        public boolean canTriggerAvoidVibration() {
            return true;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> gameEvent, GameEvent.Context context) {
            if (UmbralHowlerEntity.this.isNoAi() || UmbralHowlerEntity.this.isDeadOrDying()) {
                return false;
            }

            if (!level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }

            Entity source = context.sourceEntity();
            if (source == UmbralHowlerEntity.this) {
                return false;
            }

            long gameTime = level.getGameTime();
            return gameTime >= UmbralHowlerEntity.this.nextVibrationGameTime;
        }

        @Override
        public void onReceiveVibration(ServerLevel level,
                                       BlockPos pos,
                                       Holder<GameEvent> gameEvent,
                                       Entity sourceEntity,
                                       Entity projectileOwner,
                                       float distance) {

            if (UmbralHowlerEntity.this.isDeadOrDying()) {
                return;
            }

            long gameTime = level.getGameTime();
            if (gameTime < UmbralHowlerEntity.this.nextVibrationGameTime) {
                return;
            }

            UmbralHowlerEntity.this.nextVibrationGameTime = gameTime + VIBRATION_COOLDOWN_TICKS;

            UmbralHowlerEntity.this.setVibrationLocation(pos);

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

            UmbralHowlerEntity.this.level().broadcastEntityEvent(
                    UmbralHowlerEntity.this,
                    EVENT_VIBRATION_REACT_ANIM
            );

            if (!UmbralHowlerEntity.this.isSilent()) {
                level.playSound(
                        null,
                        UmbralHowlerEntity.this.blockPosition(),
                        ModSounds.ENTITY_UMBRAL_HOWLER_VIBRATION_REACT.get(),
                        UmbralHowlerEntity.this.getSoundSource(),
                        1.0F,
                        1.0F
                );
            }
        }
    }
}