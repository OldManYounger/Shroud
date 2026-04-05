package net.oldmanyounger.shroud.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.entity.client.BlightedShadeAnimations;
import net.oldmanyounger.shroud.entity.goal.VibrationGoal;
import net.oldmanyounger.shroud.entity.goal.VibrationListener;
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
 * Defines the Blighted Shade hostile entity and its core gameplay behavior.
 *
 * <p>This entity combines monster combat AI, GeckoLib-driven animation control,
 * vibration-based sensing, and terrain corruption mechanics while moving. It also
 * applies custom death behavior including sculk spreading, particles, and thematic drops.
 *
 * <p>In the broader context of the project, this class is part of Shroud's flagship
 * hostile-mob gameplay layer, bridging custom AI logic, world interaction, sensory
 * systems, and client-synced animation triggers.
 */
public class BlightedShadeEntity extends Monster implements GeoEntity, VibrationListener, VibrationSystem {

    // ============================================================
    //  CONSTANTS
    // ============================================================

    // Cooldown between accepted vibration events.
    private static final int VIBRATION_COOLDOWN_TICKS = 80;

    // Interval for conversion checks beneath the entity.
    private static final int BLOCK_CONVERT_INTERVAL_TICKS = 5;

    // Min and max veins placed after a successful conversion.
    private static final int MIN_SCULK_VEINS_PER_CONVERSION = 2;
    private static final int MAX_SCULK_VEINS_PER_CONVERSION = 4;

    // Passive proximity needed for player targeting without vibration.
    private static final double PASSIVE_PLAYER_DETECT_RANGE = 2.0D;

    // Max distance for acquiring player targets from vibration events.
    private static final double VIBRATION_PLAYER_ACQUIRE_RANGE = 12.0D;

    // Entity event IDs for client animation triggers.
    private static final byte EVENT_ATTACK_ANIM = 60;
    private static final byte EVENT_VIBRATION_REACT_ANIM = 5;

    // GeckoLib animation controller names.
    private static final String CTRL_LOCOMOTION = "blighted_shade_locomotion_controller";
    private static final String CTRL_VIBRATION = "blighted_shade_vibration_controller";
    private static final String CTRL_ATTACK = "blighted_shade_attack_controller";

    // GeckoLib trigger names.
    private static final String TRIG_ATTACK = "attack";
    private static final String TRIG_VIBRATION_REACT = "vibration_react";

    // Cardinal directions used when attempting adjacent vein spread.
    private static final Direction[] CARDINAL_DIRECTIONS = new Direction[] {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

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

    // Most recent vibration location used by vibration-aware goals.
    @Nullable
    private BlockPos vibrationLocation;

    // Last ground position processed for underfoot conversion checks.
    @Nullable
    private BlockPos lastConvertedPos;

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
    public BlightedShadeEntity(EntityType<? extends BlightedShadeEntity> type, Level level) {
        super(type, level);

        // Initializes vibration listener integration.
        this.vibrationUser = new BlightedShadeVibrationUser();
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));

        // Matches vanilla Enderman XP reward.
        this.xpReward = 5;
    }

    // ============================================================
    //  ATTRIBUTES
    // ============================================================

    // Declares baseline combat and movement attributes.
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 15.0D)
                .add(Attributes.ARMOR, 4.0D);
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

    // Ticks vibration state and underfoot conversion logic.
    @Override
    public void tick() {
        // Runs vibration and conversion logic on the server only.
        if (this.level() instanceof ServerLevel serverLevel) {
            VibrationSystem.Ticker.tick(serverLevel, this.vibrationData, this.vibrationUser);
            this.tryConvertBlocksUnderfoot(serverLevel);
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

        // Drops a sculk pearl on death.
        this.spawnAtLocation(ModItems.SCULK_PEARL.get());
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

    // Broadcasts attack animation trigger when a hit lands.
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);

        // Syncs attack animation to clients on successful server-side hits.
        if (result && !this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_ANIM);
        }

        return result;
    }

    // Receives entity event IDs and triggers matching GeckoLib animations.
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
        return ModSounds.ENTITY_BLIGHTED_SHADE_AMBIENT.get();
    }

    // Returns ambient sound interval in ticks.
    @Override
    public int getAmbientSoundInterval() {
        return 180;
    }

    // Returns hurt sound event.
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_BLIGHTED_SHADE_HURT.get();
    }

    // Returns death sound event.
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_BLIGHTED_SHADE_DEATH.get();
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

    // Returns the most recently stored vibration location.
    @Nullable
    @Override
    public BlockPos getVibrationLocation() {
        return this.vibrationLocation;
    }

    // Stores the most recently received vibration location.
    @Override
    public void setVibrationLocation(@Nullable BlockPos pos) {
        this.vibrationLocation = pos;
    }

    // Indicates this entity does not dampen vibration propagation.
    @Override
    public boolean dampensVibrations() {
        return false;
    }

    // Registers or unregisters the dynamic game event listener against the current server level.
    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> listenerConsumer) {
        if (this.level() instanceof ServerLevel serverLevel) {
            listenerConsumer.accept(this.dynamicGameEventListener, serverLevel);
        }
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
                        return state.setAndContinue(BlightedShadeAnimations.WALKING);
                    }

                    if (state.isCurrentAnimation(BlightedShadeAnimations.IDLE_ALTERNATE)) {
                        return state.setAndContinue(BlightedShadeAnimations.IDLE_ALTERNATE);
                    }

                    if (this.getRandom().nextInt(1800) == 0) {
                        return state.setAndContinue(BlightedShadeAnimations.IDLE_ALTERNATE);
                    }

                    return state.setAndContinue(BlightedShadeAnimations.IDLE);
                }),

                // Overlay controller for one-shot vibration reaction.
                new AnimationController<>(this, CTRL_VIBRATION, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_VIBRATION_REACT, BlightedShadeAnimations.VIBRATION_REACT),

                // Overlay controller for one-shot attack animation.
                new AnimationController<>(this, CTRL_ATTACK, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_ATTACK, BlightedShadeAnimations.ATTACK)
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
    private class BlightedShadeVibrationUser implements VibrationSystem.User {

        // Position source tracks entity listener position at eye height.
        private final PositionSource positionSource =
                new EntityPositionSource(BlightedShadeEntity.this, BlightedShadeEntity.this.getEyeHeight());

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
            if (BlightedShadeEntity.this.isNoAi() || BlightedShadeEntity.this.isDeadOrDying()) {
                return false;
            }

            // Rejects events outside the world border.
            if (!level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }

            Entity source = context.sourceEntity();

            // Rejects self-generated vibrations.
            if (source == BlightedShadeEntity.this) {
                return false;
            }

            // Rejects same-faction tagged vibration sources.
            if (BlightedShadeEntity.this.isVibrationFriendlySelf() && isVibrationFriendlyEntity(source)) {
                return false;
            }

            // Ignores vibrations produced by creative players.
            if (isCreativePlayer(source)) {
                return false;
            }

            // Applies cooldown gating.
            return level.getGameTime() >= BlightedShadeEntity.this.nextVibrationGameTime;
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
            if (BlightedShadeEntity.this.isVibrationFriendlySelf()
                    && (isVibrationFriendlyEntity(sourceEntity) || isVibrationFriendlyEntity(projectileOwner))) {
                return;
            }

            // Ignores events if the entity is already dying.
            if (BlightedShadeEntity.this.isDeadOrDying()) {
                return;
            }

            // Enforces vibration cooldown.
            long gameTime = level.getGameTime();
            if (gameTime < BlightedShadeEntity.this.nextVibrationGameTime) {
                return;
            }

            BlightedShadeEntity.this.nextVibrationGameTime = gameTime + VIBRATION_COOLDOWN_TICKS;

            // Stores vibration location for downstream AI behavior.
            BlightedShadeEntity.this.setVibrationLocation(pos);

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
                if (BlightedShadeEntity.this.distanceToSqr(playerToTarget) <= maxDistSqr) {
                    BlightedShadeEntity.this.setTarget(playerToTarget);
                }
            }

            // Broadcasts vibration reaction animation trigger.
            BlightedShadeEntity.this.level().broadcastEntityEvent(BlightedShadeEntity.this, EVENT_VIBRATION_REACT_ANIM);

            // Plays vibration reaction sound if not silent.
            if (!BlightedShadeEntity.this.isSilent()) {
                level.playSound(
                        null,
                        BlightedShadeEntity.this.blockPosition(),
                        ModSounds.ENTITY_BLIGHTED_SHADE_VIBRATION_REACT.get(),
                        BlightedShadeEntity.this.getSoundSource(),
                        1.0F,
                        1.0F
                );
            }
        }
    }

    // ============================================================
    //  BLOCK CONVERSION UNDERNEATH FOOT
    // ============================================================

    // Converts compatible blocks beneath the entity while moving on ground.
    private void tryConvertBlocksUnderfoot(ServerLevel level) {
        // Throttles conversion checks.
        if (this.tickCount % BLOCK_CONVERT_INTERVAL_TICKS != 0) {
            return;
        }

        // Respects mob griefing gamerule for world edits.
        if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }

        // Converts only while grounded.
        if (!this.onGround()) {
            return;
        }

        BlockPos groundPos = this.blockPosition().below();

        // Avoids reprocessing the same block position while stationary.
        if (groundPos.equals(this.lastConvertedPos)) {
            return;
        }

        BlockState current = level.getBlockState(groundPos);
        BlockState replacement = getSculkReplacement(current);

        if (replacement != null) {
            // Uses flag 3 to notify clients and neighbors.
            boolean changed = level.setBlock(groundPos, replacement, 3);

            if (changed) {
                spawnSculkConversionParticles(level, groundPos);
                trySpreadSculkVeins(level, groundPos);
            }

            this.lastConvertedPos = groundPos.immutable();
        } else {
            // Marks position visited even when no conversion occurs.
            this.lastConvertedPos = groundPos.immutable();
        }
    }

    // Spawns short-lived conversion particles on the converted block surface.
    private void spawnSculkConversionParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 1.02D;
        double z = pos.getZ() + 0.5D;

        level.sendParticles(
                ParticleTypes.SCULK_SOUL, x, y, z,
                6,
                0.20D,
                0.08D,
                0.20D,
                0.01D
        );
    }

    // Attempts to place a random number of sculk veins around a converted center block.
    private void trySpreadSculkVeins(ServerLevel level, BlockPos centerPos) {
        int targetPlacements = MIN_SCULK_VEINS_PER_CONVERSION
                + this.random.nextInt(MAX_SCULK_VEINS_PER_CONVERSION - MIN_SCULK_VEINS_PER_CONVERSION + 1);

        int placed = 0;
        int usedDirMask = 0;

        // Uses a 4-bit mask to avoid duplicate direction checks.
        while (placed < targetPlacements && Integer.bitCount(usedDirMask) < 4) {
            int idx = this.random.nextInt(4);
            int bit = 1 << idx;

            if ((usedDirMask & bit) != 0) {
                continue;
            }

            usedDirMask |= bit;

            Direction outward = CARDINAL_DIRECTIONS[idx];
            if (placeSculkVeinOnTopOfAdjacent(level, centerPos, outward)) {
                placed++;
            }
        }
    }

    // Places a top-attached sculk vein above a neighboring block if placement is valid.
    private boolean placeSculkVeinOnTopOfAdjacent(ServerLevel level, BlockPos centerPos, Direction outwardDir) {
        BlockPos adjacentBasePos = centerPos.relative(outwardDir);
        BlockState adjacentBaseState = level.getBlockState(adjacentBasePos);

        // Requires a sturdy top face to support the vein.
        if (!adjacentBaseState.isFaceSturdy(level, adjacentBasePos, Direction.UP)) {
            return false;
        }

        BlockPos veinPos = adjacentBasePos.above();
        BlockState existingAtVeinPos = level.getBlockState(veinPos);

        // Requires replaceable target space for vein placement.
        if (!existingAtVeinPos.canBeReplaced()) {
            return false;
        }

        BlockState veinState = Blocks.SCULK_VEIN.defaultBlockState()
                .setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true);

        // Ensures resulting vein state can survive at target position.
        if (!veinState.canSurvive(level, veinPos)) {
            return false;
        }

        return level.setBlock(veinPos, veinState, 3);
    }

    // Returns replacement state for convertible terrain blocks, or null when no conversion applies.
    @Nullable
    private BlockState getSculkReplacement(BlockState state) {
        Block block = state.getBlock();

        if (block == Blocks.GRASS_BLOCK) {
            return ModBlocks.SCULK_GRASS.get().defaultBlockState();
        }

        if (block == Blocks.DIRT) {
            return Blocks.SCULK.defaultBlockState();
        }

        if (block == Blocks.STONE) {
            return ModBlocks.SCULK_STONE.get().defaultBlockState();
        }

        if (block == Blocks.DEEPSLATE) {
            return ModBlocks.SCULK_DEEPSLATE.get().defaultBlockState();
        }

        return null;
    }
}