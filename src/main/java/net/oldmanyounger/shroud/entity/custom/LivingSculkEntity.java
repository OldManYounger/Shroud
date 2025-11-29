package net.oldmanyounger.shroud.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.Level;
import net.oldmanyounger.shroud.entity.ModEntities;
import net.oldmanyounger.shroud.entity.client.LivingSculkAnimations;
import net.oldmanyounger.shroud.entity.goal.VibrationGoal;
import net.oldmanyounger.shroud.entity.goal.VibrationListener;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;

import javax.annotation.Nullable;

public class LivingSculkEntity extends Monster implements GeoEntity, VibrationListener {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public LivingSculkEntity(EntityType<? extends LivingSculkEntity> type, Level level) {
        super(type, level);
    }

    // ============================================================
    //  ATTRIBUTES
    // ============================================================
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    // ============================================================
    //  AI GOALS (HOSTILE MOB)
    // ============================================================
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.8D, 6));
        this.goalSelector.addGoal(4, new VibrationGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ============================================================
    //  ATTACK → TRIGGER GECKOLIB ANIMATION
    // ============================================================
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);

        if (result) {
            // Sync attack animation to client
            this.level().broadcastEntityEvent(this, (byte) 4);
        }

        return result;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.triggerAnim("living_sculk_controller", "attack");
        } else {
            super.handleEntityEvent(id);
        }
    }

    // =====================
    //  ON-DEATH EFFECTS
    // =====================
    @Override
    public void die(DamageSource damageSource) {
        // Let vanilla handle loot, XP, advancement triggers, etc.
        super.die(damageSource);

        // Safety: only run custom death logic once, server side
        if (this.level().isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        BlockPos pos = this.blockPosition();

        // Play sound on death
        serverLevel.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 1.0F, 0.2F);

        // Spawn particles on death
        serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 0.5,
                this.getZ(), 20, 0.4, 0.4, 0.4, 0.02);

        // Drop an item on death
        // this.spawnAtLocation(new ItemStack(ModItems.SCULK_HEART.get()));

        // Summon a different mob on death
        LivingSculkEntity child = ModEntities.LIVING_SCULK.get().create(serverLevel);
        if (child != null) {
            child.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            serverLevel.addFreshEntity(child);
        }
    }

    // ============================================================
    //  GECKOLIB CONTROLLER
    // ============================================================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "living_sculk_controller", 4, state -> {
            // Attack takes priority
            if (this.swinging && this.getTarget() != null) {
                return state.setAndContinue(LivingSculkAnimations.ATTACK);
            }

            boolean moving = this.getNavigation().isInProgress()
                    || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;

            if (moving) {
                return state.setAndContinue(LivingSculkAnimations.WALKING);
            }

            // If currently playing rare idle, let it run to completion
            if (state.isCurrentAnimation(LivingSculkAnimations.IDLE_HEAD_SPLIT)) {
                return state.setAndContinue(LivingSculkAnimations.IDLE_HEAD_SPLIT);
            }

            // Trigger rare idle *once* with a small chance
            // (1 in 600 per idle tick by default)
            if (this.getRandom().nextInt(1800) == 0) {
                return state.setAndContinue(LivingSculkAnimations.IDLE_HEAD_SPLIT);
            }

            // Default idle
            return state.setAndContinue(LivingSculkAnimations.IDLE);
        }));
    }

    @Nullable
    private BlockPos vibrationLocation;

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
        // Living Sculk should not cause sculk sensors / shriekers to trigger
        return true;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}