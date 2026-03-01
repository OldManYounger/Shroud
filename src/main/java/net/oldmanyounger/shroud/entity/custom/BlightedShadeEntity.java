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
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
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
 * Represents the custom Blighted Shade hostile entity for the Shroud mod.
 * <p>
 * This entity:
 * <ul>
 *   <li>Extends {@link Monster} to participate in vanilla hostile mob AI and combat systems</li>
 *   <li>Implements {@link GeoEntity} to drive animations through GeckoLib</li>
 *   <li>Implements {@link VibrationListener} and {@link VibrationSystem} to react to
 *       in-world game events such as footsteps and projectiles via the vibration system</li>
 * </ul>
 */
public class BlightedShadeEntity extends Monster implements GeoEntity, VibrationListener, VibrationSystem {

    // ============================================================
    //  CONSTANTS
    // ============================================================

    private static final int VIBRATION_COOLDOWN_TICKS = 80;

    private static final int BLOCK_CONVERT_INTERVAL_TICKS = 5;
    private static final int MIN_SCULK_VEINS_PER_CONVERSION = 2;
    private static final int MAX_SCULK_VEINS_PER_CONVERSION = 4;

    private static final double PASSIVE_PLAYER_DETECT_RANGE = 2.0D;
    private static final double VIBRATION_PLAYER_ACQUIRE_RANGE = 12.0D;

    private static final byte EVENT_ATTACK_ANIM = 60;
    private static final byte EVENT_VIBRATION_REACT_ANIM = 5;

    private static final String CTRL_LOCOMOTION = "blighted_shade_locomotion_controller";
    private static final String CTRL_VIBRATION = "blighted_shade_vibration_controller";
    private static final String CTRL_ATTACK = "blighted_shade_attack_controller";

    private static final String TRIG_ATTACK = "attack";
    private static final String TRIG_VIBRATION_REACT = "vibration_react";

    // ============================================================
    //  FIELDS
    // ============================================================

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final VibrationSystem.Data vibrationData = new VibrationSystem.Data();
    private final VibrationSystem.User vibrationUser;
    private final DynamicGameEventListener<VibrationSystem.Listener> dynamicGameEventListener;

    private static final Direction[] CARDINAL_DIRECTIONS = new Direction[] {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    private long nextVibrationGameTime = 0L;

    @Nullable
    private BlockPos vibrationLocation;

    @Nullable
    private BlockPos lastConvertedPos;

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

    public BlightedShadeEntity(EntityType<? extends BlightedShadeEntity> type, Level level) {
        super(type, level);

        // Vibration system wiring
        this.vibrationUser = new BlightedShadeVibrationUser();
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));

        // Match vanilla Enderman XP reward
        this.xpReward = 5;
    }

    // ============================================================
    //  ATTRIBUTES
    // ============================================================

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

            // Convert valid blocks beneath the shade while moving
            this.tryConvertBlocksUnderfoot(serverLevel);
        }

        super.tick();
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource damageSource) {
        // Intentionally no sculk spreading mechanic
        super.die(damageSource);
    }

    // ============================================================
    //  COMBAT / ENTITY EVENTS
    // ============================================================

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);

        // Sync attack animation to clients on successful hit (server only)
        if (result && !this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, EVENT_ATTACK_ANIM);
        }

        return result;
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
        return ModSounds.ENTITY_BLIGHTED_SHADE_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 180;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.ENTITY_BLIGHTED_SHADE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_BLIGHTED_SHADE_DEATH.get();
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

    @Nullable
    @Override
    public BlockPos getVibrationLocation() {
        return this.vibrationLocation;
    }

    @Override
    public void setVibrationLocation(@Nullable BlockPos pos) {
        this.vibrationLocation = pos;
    }

    @Override
    public boolean dampensVibrations() {
        return false;
    }

    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> listenerConsumer) {
        // Register or unregister the dynamic game event listener on the server level
        if (this.level() instanceof ServerLevel serverLevel) {
            listenerConsumer.accept(this.dynamicGameEventListener, serverLevel);
        }
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

                // Vibration react overlay controller
                new AnimationController<>(this, CTRL_VIBRATION, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_VIBRATION_REACT, BlightedShadeAnimations.VIBRATION_REACT),

                // Attack overlay controller
                new AnimationController<>(this, CTRL_ATTACK, 2, state -> PlayState.STOP)
                        .triggerableAnim(TRIG_ATTACK, BlightedShadeAnimations.ATTACK)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ============================================================
    //  VIBRATION USER (INNER CLASS)
    // ============================================================

    private class BlightedShadeVibrationUser implements VibrationSystem.User {

        // Listener position is tracked at eye height
        private final PositionSource positionSource =
                new EntityPositionSource(BlightedShadeEntity.this, BlightedShadeEntity.this.getEyeHeight());

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
            if (BlightedShadeEntity.this.isNoAi() || BlightedShadeEntity.this.isDeadOrDying()) {
                return false;
            }

            if (!level.getWorldBorder().isWithinBounds(pos)) {
                return false;
            }

            Entity source = context.sourceEntity();
            if (source == BlightedShadeEntity.this) { // class-specific in each file
                return false;
            }

            // If both listener and source are tagged, ignore this vibration
            if (BlightedShadeEntity.this.isVibrationFriendlySelf() && isVibrationFriendlyEntity(source)) {
                return false;
            }

            if (isCreativePlayer(source)) {
                return false;
            }

            // Cooldown gate
            return level.getGameTime() >= BlightedShadeEntity.this.nextVibrationGameTime;
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

            if (BlightedShadeEntity.this.isVibrationFriendlySelf()
                    && (isVibrationFriendlyEntity(sourceEntity) || isVibrationFriendlyEntity(projectileOwner))) {
                return;
            }

            // Ignore late events while dying
            if (BlightedShadeEntity.this.isDeadOrDying()) {
                return;
            }

            // Cooldown gate
            long gameTime = level.getGameTime();
            if (gameTime < BlightedShadeEntity.this.nextVibrationGameTime) {
                return;
            }

            BlightedShadeEntity.this.nextVibrationGameTime = gameTime + VIBRATION_COOLDOWN_TICKS;

            // Store vibration location for AI goals
            BlightedShadeEntity.this.setVibrationLocation(pos);

            // Acquire a player target if the source is a player or their projectile owner
            Player playerToTarget = null;

            if (sourceEntity instanceof Player p) {
                playerToTarget = p;
            } else if (projectileOwner instanceof Player p) {
                playerToTarget = p;
            }

            if (playerToTarget != null) {
                double maxDistSqr = VIBRATION_PLAYER_ACQUIRE_RANGE * VIBRATION_PLAYER_ACQUIRE_RANGE;
                if (BlightedShadeEntity.this.distanceToSqr(playerToTarget) <= maxDistSqr) {
                    BlightedShadeEntity.this.setTarget(playerToTarget);
                }
            }

            // Sync vibration reaction animation to clients
            BlightedShadeEntity.this.level().broadcastEntityEvent(BlightedShadeEntity.this, EVENT_VIBRATION_REACT_ANIM);

            // Play vibration react sound on server if not silent
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

    private void tryConvertBlocksUnderfoot(ServerLevel level) {
        // Keep behavior server-side and reasonably lightweight
        if (this.tickCount % BLOCK_CONVERT_INTERVAL_TICKS != 0) {
            return;
        }

        // Optional: respect mob griefing so players/admins can disable world edits
        if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }

        // Only convert while on ground (walking across terrain)
        if (!this.onGround()) {
            return;
        }

        BlockPos groundPos = this.blockPosition().below();

        // Avoid repeatedly processing the exact same position
        if (groundPos.equals(this.lastConvertedPos)) {
            return;
        }

        BlockState current = level.getBlockState(groundPos);
        BlockState replacement = getSculkReplacement(current);

        if (replacement != null) {
            // 3 = notify clients + update neighbors
            boolean changed = level.setBlock(groundPos, replacement, 3);

            if (changed) {
                spawnSculkConversionParticles(level, groundPos);
                trySpreadSculkVeins(level, groundPos);
            }

            this.lastConvertedPos = groundPos.immutable();
        } else {
            // Still mark as visited so we don't re-check every 5 ticks while stationary
            this.lastConvertedPos = groundPos.immutable();
        }
    }

    private void spawnSculkConversionParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 1.02D; // top surface of converted block
        double z = pos.getZ() + 0.5D;

        // Brief spurt of sculk soul particles
        level.sendParticles(
                ParticleTypes.SCULK_SOUL, x, y, z,
                6,          // particle count
                0.20D,      // x spread
                0.08D,      // y spread
                0.20D,      // z spread
                0.01D       // speed
        );
    }

    private void trySpreadSculkVeins(ServerLevel level, BlockPos centerPos) {
        // Randomly choose how many veins to place this conversion (2..4)
        int targetPlacements = MIN_SCULK_VEINS_PER_CONVERSION
                + this.random.nextInt(MAX_SCULK_VEINS_PER_CONVERSION - MIN_SCULK_VEINS_PER_CONVERSION + 1);

        int placed = 0;
        int usedDirMask = 0;

        // Random unique-direction picks via 4-bit mask (low allocation / low overhead)
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

    private boolean placeSculkVeinOnTopOfAdjacent(ServerLevel level, BlockPos centerPos, Direction outwardDir) {
        // Adjacent ground block around the converted center block
        BlockPos adjacentBasePos = centerPos.relative(outwardDir);
        BlockState adjacentBaseState = level.getBlockState(adjacentBasePos);

        // Need a solid top face to support vein on top
        if (!adjacentBaseState.isFaceSturdy(level, adjacentBasePos, Direction.UP)) {
            return false;
        }

        // Vein goes in the air/replaceable block above that adjacent base block
        BlockPos veinPos = adjacentBasePos.above();
        BlockState existingAtVeinPos = level.getBlockState(veinPos);

        if (!existingAtVeinPos.canBeReplaced()) {
            return false;
        }

        // Attach vein to block below (top-surface placement)
        BlockState veinState = Blocks.SCULK_VEIN.defaultBlockState()
                .setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true);

        if (!veinState.canSurvive(level, veinPos)) {
            return false;
        }

        return level.setBlock(veinPos, veinState, 3);
    }

    @Nullable
    private BlockState getSculkReplacement(BlockState state) {
        Block block = state.getBlock();

        // grass -> sculk grass
        if (block == Blocks.GRASS_BLOCK) {
            return ModBlocks.SCULK_GRASS.get().defaultBlockState();
        }

        // dirt -> vanilla sculk
        if (block == Blocks.DIRT) {
            return Blocks.SCULK.defaultBlockState();
        }

        // stone -> sculk stone
        if (block == Blocks.STONE) {
            return ModBlocks.SCULK_STONE.get().defaultBlockState();
        }

        // deepslate -> sculk deepslate
        if (block == Blocks.DEEPSLATE) {
            return ModBlocks.SCULK_DEEPSLATE.get().defaultBlockState();
        }

        return null;
    }
}