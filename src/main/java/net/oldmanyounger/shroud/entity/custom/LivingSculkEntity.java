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
 * Represents the custom Living Sculk hostile entity for the Shroud mod.
 * <p>
 * This entity:
 * <ul>
 *   <li>Extends {@link Monster} to participate in vanilla hostile mob AI and combat systems</li>
 *   <li>Implements {@link GeoEntity} to drive animations through GeckoLib</li>
 *   <li>Implements {@link VibrationListener} and {@link VibrationSystem} to react to
 *       in-world game events such as footsteps and projectiles via the vibration system</li>
 * </ul>
 */
public class LivingSculkEntity extends Monster implements GeoEntity, VibrationListener, VibrationSystem {

    // ============================================================
    //  CONSTANTS
    // ============================================================

    private static final int VIBRATION_COOLDOWN_TICKS = 80;
    private static final int CONVERSION_COOLDOWN_TICKS = 40;

    private static final double PASSIVE_PLAYER_DETECT_RANGE = 2.0D;
    private static final double VIBRATION_PLAYER_ACQUIRE_RANGE = 12.0D;

    private static final byte EVENT_ATTACK_ANIM = 60;
    private static final byte EVENT_VIBRATION_REACT_ANIM = 5;

    private static final String CTRL_LOCOMOTION = "living_sculk_locomotion_controller";
    private static final String CTRL_VIBRATION = "living_sculk_vibration_controller";
    private static final String CTRL_ATTACK = "living_sculk_attack_controller";

    private static final String TRIG_ATTACK = "attack";
    private static final String TRIG_VIBRATION_REACT = "vibration_react";

    // ============================================================
    //  FIELDS
    // ============================================================

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final VibrationSystem.Data vibrationData = new VibrationSystem.Data();
    private final VibrationSystem.User vibrationUser;
    private final DynamicGameEventListener<VibrationSystem.Listener> dynamicGameEventListener;

    private long nextVibrationGameTime = 0L;
    private long nextConversionGameTime = 0L;

    @Nullable
    private BlockPos vibrationLocation;

    private static boolean isCreativePlayer(@javax.annotation.Nullable Entity entity) {
        return entity instanceof Player p && p.isCreative();
    }

    private boolean isVibrationFriendlySelf() {
        return this.getType().is(ModEntityTypeTags.VIBRATION_FRIENDLY);
    }

    private static boolean isVibrationFriendlyEntity(@javax.annotation.Nullable Entity entity) {
        return entity != null && entity.getType().is(ModEntityTypeTags.VIBRATION_FRIENDLY);
    }

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    public LivingSculkEntity(EntityType<? extends LivingSculkEntity> type, Level level) {
        super(type, level);

        // Vibration system wiring
        this.vibrationUser = new LivingSculkVibrationUser();
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));
    }

    // ============================================================
    //  ATTRIBUTES
    // ============================================================

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

    @Override
    protected void registerGoals() {
        // Movement and combat
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.8D, 20));

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

        // Hostile to vanilla zombies and variants via tag
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

    @Override
    public void tick() {
        // Advance vibration system state only on the server side
        if (this.level() instanceof ServerLevel serverLevel) {
            VibrationSystem.Ticker.tick(serverLevel, this.vibrationData, this.vibrationUser);
        }

        super.tick();
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        // Skip server-side effects when on the client
        if (this.level().isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        BlockPos pos = this.blockPosition();

        // Sculk spread charge based on XP reward
        this.spreadSculkOnDeath(serverLevel, pos, damageSource);

        // Soul-like sculk particles at the death position
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

        // Thematic drop
        this.spawnAtLocation(Items.ECHO_SHARD);
    }

    private void spreadSculkOnDeath(ServerLevel level, BlockPos pos, DamageSource damageSource) {
        // Convert XP reward into sculk spread charge
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

        // Sync attack animation to clients when main-hand swinging on server
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_ANIM);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);

        if (result) {
            this.tryCorruptOffhandTotem(target);

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

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity victim) {
        boolean result = super.killedEntity(level, victim);

        // Attempt zombie conversion after a successful kill
        this.tryConvertZombieOnHit(victim);

        return result;
    }

    private void tryConvertZombieOnHit(Entity target) {
        // Server-only conversion logic
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Per-attacker cooldown gate
        long gameTime = serverLevel.getGameTime();
        if (gameTime < this.nextConversionGameTime) {
            return;
        }

        // Only living entities are convertible
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        // Only zombies (and variants) via tag
        if (!livingTarget.getType().is(EntityTypeTags.ZOMBIES)) {
            return;
        }

        // Only convert when the victim is actually dying/removed state is consistent with killedEntity flow
        if (!livingTarget.isDeadOrDying() || livingTarget.isRemoved()) {
            return;
        }

        // Exclusions
        if (livingTarget instanceof Player) {
            return;
        }

        if (livingTarget instanceof LivingSculkEntity) {
            return;
        }

        this.nextConversionGameTime = gameTime + CONVERSION_COOLDOWN_TICKS;

        // Create replacement entity
        LivingSculkEntity replacement = ModEntities.LIVING_SCULK.get().create(serverLevel);
        if (replacement == null) {
            return;
        }

        // Copy position and rotation
        replacement.moveTo(
                livingTarget.getX(),
                livingTarget.getY(),
                livingTarget.getZ(),
                livingTarget.getYRot(),
                livingTarget.getXRot()
        );

        // Spawn at full health
        replacement.setHealth(replacement.getMaxHealth());

        // Copy name and visibility
        if (livingTarget.hasCustomName()) {
            replacement.setCustomName(livingTarget.getCustomName());
            replacement.setCustomNameVisible(livingTarget.isCustomNameVisible());
        }

        // Preserve persistence requirement when applicable
        if (livingTarget instanceof Mob mobTarget && mobTarget.isPersistenceRequired()) {
            replacement.setPersistenceRequired();
        }

        // Copy equipment
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = livingTarget.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                replacement.setItemSlot(slot, stack.copy());
            }
        }

        // Finalize spawn as a conversion
        replacement.finalizeSpawn(
                serverLevel,
                serverLevel.getCurrentDifficultyAt(replacement.blockPosition()),
                MobSpawnType.CONVERSION,
                null
        );

        // Visual conversion effect at the victim's location
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

        // Add replacement and remove old entity
        serverLevel.addFreshEntity(replacement);
        livingTarget.discard();
    }

    @Override
    public void handleEntityEvent(byte id) {
        // GeckoLib triggers are routed through entity events
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
        return ModSounds.ENTITY_LIVING_SCULK_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 180;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_LIVING_SCULK_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_LIVING_SCULK_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.ENTITY_LIVING_SCULK_STEP.get(), 0.15F, 1.0F);
    }

    // ============================================================
    //  VIBRATION SYSTEM IMPLEMENTATION
    // ============================================================

    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> listenerConsumer) {
        // Register or unregister the dynamic game event listener on the server level
        if (this.level() instanceof ServerLevel level) {
            listenerConsumer.accept(this.dynamicGameEventListener, level);
        }
    }

    @Override
    public BlockPos getVibrationLocation() {
        return this.vibrationLocation;
    }

    @Override
    public void setVibrationLocation(BlockPos pos) {
        this.vibrationLocation = pos;
    }

    @Override
    public boolean dampensVibrations() {
        return true;
    }

    // ============================================================
    //  GECKOLIB ANIMATION
    // ============================================================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                // Base locomotion controller
                new AnimationController<>(this, CTRL_LOCOMOTION, 4, state -> {
                    boolean moving = this.getNavigation().isInProgress()
                            || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;

                    if (moving) {
                        return state.setAndContinue(LivingSculkAnimations.WALKING);
                    }

                    if (state.isCurrentAnimation(LivingSculkAnimations.IDLE_HEAD_SPLIT)) {
                        return state.setAndContinue(LivingSculkAnimations.IDLE_HEAD_SPLIT);
                    }

                    if (this.getRandom().nextInt(1800) == 0) {
                        return state.setAndContinue(LivingSculkAnimations.IDLE_HEAD_SPLIT);
                    }

                    return state.setAndContinue(LivingSculkAnimations.IDLE);
                }),

                // Vibration react overlay controller
                new AnimationController<>(this, CTRL_VIBRATION, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_VIBRATION_REACT, LivingSculkAnimations.VIBRATION_REACT),

                // Attack overlay controller
                new AnimationController<>(this, CTRL_ATTACK, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_ATTACK, LivingSculkAnimations.ATTACK)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ============================================================
    //  VIBRATION USER (INNER CLASS)
    // ============================================================

    private class LivingSculkVibrationUser implements VibrationSystem.User {

        // Listener position is tracked at eye height
        private final PositionSource positionSource =
                new EntityPositionSource(LivingSculkEntity.this, LivingSculkEntity.this.getEyeHeight());

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
            // Basic validity checks
            if (LivingSculkEntity.this.isNoAi() || LivingSculkEntity.this.isDeadOrDying()) {
                return false;
            }

            if (!level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }

            Entity source = context.sourceEntity();
            if (source == LivingSculkEntity.this) { // class-specific in each file
                return false;
            }

            // If both listener and source are tagged, ignore this vibration
            if (LivingSculkEntity.this.isVibrationFriendlySelf() && isVibrationFriendlyEntity(source)) {
                return false;
            }

            if (isCreativePlayer(source)) {
                return false;
            }

            // Cooldown gate
            long gameTime = level.getGameTime();
            return gameTime >= LivingSculkEntity.this.nextVibrationGameTime;
        }

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

            if (LivingSculkEntity.this.isVibrationFriendlySelf()
                    && (isVibrationFriendlyEntity(sourceEntity) || isVibrationFriendlyEntity(projectileOwner))) {
                return;
            }

            // Ignore late events while dying
            if (LivingSculkEntity.this.isDeadOrDying()) {
                return;
            }

            // Cooldown gate
            long gameTime = level.getGameTime();
            if (gameTime < LivingSculkEntity.this.nextVibrationGameTime) {
                return;
            }

            LivingSculkEntity.this.nextVibrationGameTime = gameTime + VIBRATION_COOLDOWN_TICKS;

            // Store vibration location for AI goals
            LivingSculkEntity.this.setVibrationLocation(pos);

            // Acquire a player target if the source is a player or their projectile owner
            Player playerToTarget = null;

            if (sourceEntity instanceof Player p) {
                playerToTarget = p;
            } else if (projectileOwner instanceof Player p) {
                playerToTarget = p;
            }

            if (playerToTarget != null) {
                double maxDistSqr = VIBRATION_PLAYER_ACQUIRE_RANGE * VIBRATION_PLAYER_ACQUIRE_RANGE;
                if (LivingSculkEntity.this.distanceToSqr(playerToTarget) <= maxDistSqr) {
                    LivingSculkEntity.this.setTarget(playerToTarget);
                }
            }

            // Sync vibration reaction animation to clients
            LivingSculkEntity.this.level().broadcastEntityEvent(LivingSculkEntity.this, EVENT_VIBRATION_REACT_ANIM);

            // Play vibration react sound on server if not silent
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