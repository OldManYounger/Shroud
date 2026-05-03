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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.oldmanyounger.shroud.entity.ModEntities;
import net.oldmanyounger.shroud.entity.client.LivingSculkAnimations;
import net.oldmanyounger.shroud.entity.goal.VibrationGoal;
import net.oldmanyounger.shroud.entity.goal.VibrationListener;
import net.oldmanyounger.shroud.event.ModTotemCorruption;
import net.oldmanyounger.shroud.item.ModItems;
import net.oldmanyounger.shroud.sound.ModSounds;
import net.oldmanyounger.shroud.tag.ModEntityTypeTags;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

/**
 * Defines the Living Sculk hostile entity and its gameplay behavior.
 *
 * <p>This entity combines monster combat AI, GeckoLib-driven animation control,
 * vibration-based sensing, kill-triggered zombie conversion, and custom combat
 * interactions such as offhand totem corruption pressure.
 *
 * <p>In the broader context of the project, this class is part of Shroud's core
 * hostile-mob systems, connecting custom AI, conversion mechanics, sensory
 * reactions, and synchronized client animation cues.
 */
public class LivingSculkEntity extends Monster implements GeoEntity, VibrationListener, VibrationSystem {

    // ============================================================
    //  CONSTANTS
    // ============================================================

    // Cooldown between accepted vibration events.
    private static final int VIBRATION_COOLDOWN_TICKS = 80;

    // Cooldown between zombie conversion attempts.
    private static final int CONVERSION_COOLDOWN_TICKS = 40;

    // Passive proximity needed for player targeting without vibration.
    private static final double PASSIVE_PLAYER_DETECT_RANGE = 2.0D;

    // Max distance for acquiring player targets from vibration events.
    private static final double VIBRATION_PLAYER_ACQUIRE_RANGE = 12.0D;

    // Entity event IDs for client animation triggers.
    private static final byte EVENT_ATTACK_ANIM = 60;
    private static final byte EVENT_VIBRATION_REACT_ANIM = 5;

    // GeckoLib animation controller names.
    private static final String CTRL_LOCOMOTION = "living_sculk_locomotion_controller";
    private static final String CTRL_VIBRATION = "living_sculk_vibration_controller";
    private static final String CTRL_ATTACK = "living_sculk_attack_controller";

    // GeckoLib trigger names.
    private static final String TRIG_ATTACK = "attack";
    private static final String TRIG_VIBRATION_REACT = "vibration_react";

    // ============================================================
    //  FIELDS
    // ============================================================

    // Per-entity GeckoLib cache.
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Vibration system state and listener plumbing.
    private final VibrationSystem.Data vibrationData = new VibrationSystem.Data();
    private final VibrationSystem.User vibrationUser;
    private final DynamicGameEventListener<VibrationSystem.Listener> dynamicGameEventListener;

    // Earliest game time at which a new vibration can be accepted.
    private long nextVibrationGameTime = 0L;

    // Earliest game time at which a new conversion can occur.
    private long nextConversionGameTime = 0L;

    // Most recent vibration location used by vibration-aware goals.
    @Nullable
    private BlockPos vibrationLocation;

    // Tracks whether model-driven idle head look should be applied this frame.
    private boolean allowCustomHeadLook = true;

    // ============================================================
    //  STATIC HELPERS
    // ============================================================

    // Returns true if the entity is a creative-mode player.
    private static boolean isCreativePlayer(@javax.annotation.Nullable Entity entity) {
        return entity instanceof Player p && p.isCreative();
    }

    // Returns true when this entity type is marked vibration-friendly.
    private boolean isVibrationFriendlySelf() {
        return this.getType().is(ModEntityTypeTags.VIBRATION_FRIENDLY);
    }

    // Returns true when the source entity type is marked vibration-friendly.
    private static boolean isVibrationFriendlyEntity(@javax.annotation.Nullable Entity entity) {
        return entity != null && entity.getType().is(ModEntityTypeTags.VIBRATION_FRIENDLY);
    }

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    // Creates the entity and wires vibration listener state.
    public LivingSculkEntity(EntityType<? extends LivingSculkEntity> type, Level level) {
        super(type, level);

        // Initializes vibration listener integration.
        this.vibrationUser = new LivingSculkVibrationUser();
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));
    }

    // ============================================================
    //  ATTRIBUTES
    // ============================================================

    // Declares baseline combat and movement attributes.
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 15.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    // ============================================================
    //  AI GOALS
    // ============================================================

    // Registers AI and target goals.
    @Override
    protected void registerGoals() {
        // Movement and combat goals.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.8D, 20));

        // Navigation toward recently detected vibration locations.
        this.goalSelector.addGoal(4, new VibrationGoal(this, 0.8D));

        // Idle and awareness behavior.
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        // Retaliation and proximity-based player targeting.
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

        // Hostility toward vanilla zombie family entities.
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

    // ============================================================
    //  TICKING / LIFECYCLE
    // ============================================================

    // Ticks vibration state each update.
    @Override
    public void tick() {
        // Runs vibration logic on the server only.
        if (this.level() instanceof ServerLevel serverLevel) {
            VibrationSystem.Ticker.tick(serverLevel, this.vibrationData, this.vibrationUser);
        }

        super.tick();
    }

    // Runs death-side effects including sculk spread, particles, and drop.
    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        // Skips server-only death effects on the client.
        if (this.level().isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        BlockPos pos = this.blockPosition();

        // Spreads sculk charge derived from XP reward.
        this.spreadSculkOnDeath(serverLevel, pos, damageSource);

        // Emits thematic death particles.
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

        // Drops echo shard on death.
        this.spawnAtLocation(Items.ECHO_SHARD);
    }

    // Spreads sculk using charge derived from experience reward.
    private void spreadSculkOnDeath(ServerLevel level, BlockPos pos, DamageSource damageSource) {
        Entity attacker = damageSource.getEntity();
        int charge = this.getExperienceReward(level, attacker);

        // Ensures at least one charge is applied.
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

    // Broadcasts attack animation event when swinging main hand on server.
    @Override
    public void swing(InteractionHand hand, boolean updateSelf) {
        super.swing(hand, updateSelf);

        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_ANIM);
        }
    }

    // Processes successful melee hits with extra effects.
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);

        if (result) {
            // Attempts offhand totem corruption behavior.
            this.tryCorruptOffhandTotem(target);

            // Plays catalyst-like hit feedback sound.
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

    // Tries zombie conversion after kill resolution.
    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity victim) {
        boolean result = super.killedEntity(level, victim);

        this.tryConvertZombieOnHit(victim);

        return result;
    }

    // Converts dead zombie-tagged victims into Living Sculk when allowed.
    private void tryConvertZombieOnHit(Entity target) {
        // Runs only on the server.
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Enforces per-attacker conversion cooldown.
        long gameTime = serverLevel.getGameTime();
        if (gameTime < this.nextConversionGameTime) {
            return;
        }

        // Requires a living target.
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        // Requires a zombie family target.
        if (!livingTarget.getType().is(EntityTypeTags.ZOMBIES)) {
            return;
        }

        // Requires target to be in valid post-kill state.
        if (!livingTarget.isDeadOrDying() || livingTarget.isRemoved()) {
            return;
        }

        // Prevents converting players.
        if (livingTarget instanceof Player) {
            return;
        }

        // Prevents converting already-converted variants.
        if (livingTarget instanceof LivingSculkEntity) {
            return;
        }

        this.nextConversionGameTime = gameTime + CONVERSION_COOLDOWN_TICKS;

        // Creates replacement entity.
        LivingSculkEntity replacement = ModEntities.LIVING_SCULK.get().create(serverLevel);
        if (replacement == null) {
            return;
        }

        // Copies position and rotation.
        replacement.moveTo(
                livingTarget.getX(),
                livingTarget.getY(),
                livingTarget.getZ(),
                livingTarget.getYRot(),
                livingTarget.getXRot()
        );

        // Initializes replacement at full health.
        replacement.setHealth(replacement.getMaxHealth());

        // Copies custom name and visibility.
        if (livingTarget.hasCustomName()) {
            replacement.setCustomName(livingTarget.getCustomName());
            replacement.setCustomNameVisible(livingTarget.isCustomNameVisible());
        }

        // Preserves persistence flag when applicable.
        if (livingTarget instanceof Mob mobTarget && mobTarget.isPersistenceRequired()) {
            replacement.setPersistenceRequired();
        }

        // Copies equipment from victim to replacement.
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = livingTarget.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                replacement.setItemSlot(slot, stack.copy());
            }
        }

        // Finalizes spawn as a conversion event.
        replacement.finalizeSpawn(
                serverLevel,
                serverLevel.getCurrentDifficultyAt(replacement.blockPosition()),
                MobSpawnType.CONVERSION,
                null
        );

        // Emits conversion particles at the victim location.
        serverLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                livingTarget.getX(),
                livingTarget.getY() + 0.5D,
                livingTarget.getZ(),
                20,
                0.4D,
                0.4D,
                0.4D,
                0.02D
        );

        // Adds replacement and removes original victim.
        serverLevel.addFreshEntity(replacement);
        livingTarget.discard();
    }

    // Routes entity event IDs into GeckoLib trigger calls.
    @Override
    public void handleEntityEvent(byte id) {
        // Triggers attack animation.
        if (id == EVENT_ATTACK_ANIM) {
            this.triggerAnim(CTRL_ATTACK, TRIG_ATTACK);
            return;
        }

        // Triggers vibration reaction animation.
        if (id == EVENT_VIBRATION_REACT_ANIM) {
            this.triggerAnim(CTRL_VIBRATION, TRIG_VIBRATION_REACT);
            return;
        }

        super.handleEntityEvent(id);
    }

    // ============================================================
    //  SOUND OVERRIDES
    // ============================================================

    // Returns ambient sound event.
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_LIVING_SCULK_AMBIENT.get();
    }

    // Returns ambient sound interval in ticks.
    @Override
    public int getAmbientSoundInterval() {
        return 180;
    }

    // Returns hurt sound event.
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_LIVING_SCULK_HURT.get();
    }

    // Returns death sound event.
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_LIVING_SCULK_DEATH.get();
    }

    // Plays custom step sound.
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.ENTITY_LIVING_SCULK_STEP.get(), 0.15F, 1.0F);
    }

    // ============================================================
    //  VIBRATION SYSTEM IMPLEMENTATION
    // ============================================================

    // Exposes vibration data for vibration system ticking.
    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    // Exposes vibration user implementation for vibration system ticking.
    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    // Registers or unregisters the dynamic game event listener against the current server level.
    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> listenerConsumer) {
        if (this.level() instanceof ServerLevel level) {
            listenerConsumer.accept(this.dynamicGameEventListener, level);
        }
    }

    // Returns the most recently stored vibration location.
    @Override
    public BlockPos getVibrationLocation() {
        return this.vibrationLocation;
    }

    // Stores the most recently received vibration location.
    @Override
    public void setVibrationLocation(BlockPos pos) {
        this.vibrationLocation = pos;
    }

    // Indicates this entity dampens vibration propagation.
    @Override
    public boolean dampensVibrations() {
        return true;
    }

    // Returns whether model-level idle head look overrides are currently allowed.
    public boolean allowCustomHeadLook() {
        return this.allowCustomHeadLook;
    }

    // ============================================================
    //  GECKOLIB ANIMATION
    // ============================================================

    // Registers GeckoLib animation controllers.
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                // Locomotion controller for idle/walk selection.
                new AnimationController<>(this, CTRL_LOCOMOTION, 4, state -> {
                    boolean moving = this.getNavigation().isInProgress()
                            || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;

                    if (moving) {
                        this.allowCustomHeadLook = false;
                        return state.setAndContinue(LivingSculkAnimations.WALKING);
                    }

                    if (state.isCurrentAnimation(LivingSculkAnimations.IDLE_HEAD_SPLIT)) {
                        this.allowCustomHeadLook = false;
                        return state.setAndContinue(LivingSculkAnimations.IDLE_HEAD_SPLIT);
                    }

                    if (this.getRandom().nextInt(1800) == 0) {
                        this.allowCustomHeadLook = false;
                        return state.setAndContinue(LivingSculkAnimations.IDLE_HEAD_SPLIT);
                    }

                    this.allowCustomHeadLook = true;
                    return state.setAndContinue(LivingSculkAnimations.IDLE);
                }),

                // Overlay controller for one-shot vibration reaction.
                new AnimationController<>(this, CTRL_VIBRATION, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_VIBRATION_REACT, LivingSculkAnimations.VIBRATION_REACT),

                // Overlay controller for one-shot attack animation.
                new AnimationController<>(this, CTRL_ATTACK, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_ATTACK, LivingSculkAnimations.ATTACK)
        );
    }

    // Returns the entity's GeckoLib animatable cache.
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ============================================================
    //  VIBRATION USER (INNER CLASS)
    // ============================================================

    // Implements vibration system behavior for this entity.
    private class LivingSculkVibrationUser implements VibrationSystem.User {

        // Position source tracks entity listener position at eye height.
        private final PositionSource positionSource =
                new EntityPositionSource(LivingSculkEntity.this, LivingSculkEntity.this.getEyeHeight());

        // Returns vibration listener source position.
        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        // Returns vibration listening radius in blocks.
        @Override
        public int getListenerRadius() {
            return 12;
        }

        // Returns game event tag set this listener can react to.
        @Override
        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.WARDEN_CAN_LISTEN;
        }

        // Allows this listener to trigger vibration avoidance checks.
        @Override
        public boolean canTriggerAvoidVibration() {
            return true;
        }

        // Filters whether an incoming vibration should be accepted.
        @Override
        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> gameEvent, GameEvent.Context context) {
            // Rejects events when AI is disabled or entity is dead.
            if (LivingSculkEntity.this.isNoAi() || LivingSculkEntity.this.isDeadOrDying()) {
                return false;
            }

            // Rejects events outside the world border.
            if (!level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }

            Entity source = context.sourceEntity();

            // Rejects self-generated vibrations.
            if (source == LivingSculkEntity.this) {
                return false;
            }

            // Rejects same-faction tagged vibration sources.
            if (LivingSculkEntity.this.isVibrationFriendlySelf() && isVibrationFriendlyEntity(source)) {
                return false;
            }

            // Ignores vibrations produced by creative players.
            if (isCreativePlayer(source)) {
                return false;
            }

            // Applies cooldown gating.
            long gameTime = level.getGameTime();
            return gameTime >= LivingSculkEntity.this.nextVibrationGameTime;
        }

        // Handles accepted vibration events and updates behavior/animation.
        @Override
        public void onReceiveVibration(ServerLevel level,
                                       BlockPos pos,
                                       Holder<GameEvent> gameEvent,
                                       Entity sourceEntity,
                                       Entity projectileOwner,
                                       float distance) {

            // Ignores creative-mode sources and owners.
            if (isCreativePlayer(sourceEntity) || isCreativePlayer(projectileOwner)) {
                return;
            }

            // Ignores friendly-tagged sources when this entity is also friendly-tagged.
            if (LivingSculkEntity.this.isVibrationFriendlySelf()
                    && (isVibrationFriendlyEntity(sourceEntity) || isVibrationFriendlyEntity(projectileOwner))) {
                return;
            }

            // Ignores events if the entity is already dying.
            if (LivingSculkEntity.this.isDeadOrDying()) {
                return;
            }

            // Enforces vibration cooldown.
            long gameTime = level.getGameTime();
            if (gameTime < LivingSculkEntity.this.nextVibrationGameTime) {
                return;
            }

            LivingSculkEntity.this.nextVibrationGameTime = gameTime + VIBRATION_COOLDOWN_TICKS;

            // Stores vibration location for downstream AI behavior.
            LivingSculkEntity.this.setVibrationLocation(pos);

            // Tries to resolve a player source from direct source or projectile owner.
            Player playerToTarget = null;

            if (sourceEntity instanceof Player p) {
                playerToTarget = p;
            } else if (projectileOwner instanceof Player p) {
                playerToTarget = p;
            }

            // Acquires nearby player as target when within vibration acquire range.
            if (playerToTarget != null) {
                double maxDistSqr = VIBRATION_PLAYER_ACQUIRE_RANGE * VIBRATION_PLAYER_ACQUIRE_RANGE;
                if (LivingSculkEntity.this.distanceToSqr(playerToTarget) <= maxDistSqr) {
                    LivingSculkEntity.this.setTarget(playerToTarget);
                }
            }

            // Broadcasts vibration reaction animation trigger.
            LivingSculkEntity.this.level().broadcastEntityEvent(LivingSculkEntity.this, EVENT_VIBRATION_REACT_ANIM);

            // Plays vibration reaction sound if not silent.
            if (!LivingSculkEntity.this.isSilent()) {
                level.playSound(
                        null,
                        LivingSculkEntity.this.blockPosition(),
                        ModSounds.ENTITY_LIVING_SCULK_VIBRATION_REACT.get(),
                        LivingSculkEntity.this.getSoundSource(),
                        1.0F,
                        1.0F
                );
            }
        }
    }

    // ============================================================
    //  CUSTOM INTERACTIONS
    // ============================================================

    // Schedules totem corruption when a valid player target has an offhand totem.
    private void tryCorruptOffhandTotem(Entity target) {
        if (!(target instanceof Player player)) {
            return;
        }

        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        if (!player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            return;
        }

        ModTotemCorruption.schedule(player);
    }
}