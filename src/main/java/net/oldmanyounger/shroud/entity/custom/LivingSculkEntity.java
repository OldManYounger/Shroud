package net.oldmanyounger.shroud.entity.custom;

import net.minecraft.tags.GameEventTags;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.oldmanyounger.shroud.entity.client.LivingSculkAnimations;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;

public class LivingSculkEntity extends Monster implements GeoEntity {

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
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
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
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.0D, 24));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

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

    // ============================================================
    //  GECKOLIB CONTROLLER
    // ============================================================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "living_sculk_controller", 4, state -> {
            // Attack takes priority over everything
            if (this.swinging && this.getTarget() != null) {
                return state.setAndContinue(LivingSculkAnimations.ATTACK);
            }

            // Treat the mob as "moving" while pathfinding or if it has horizontal velocity.
            // This avoids tiny pauses where GeckoLib would otherwise think it's idle.
            boolean moving = this.getNavigation().isInProgress()
                    || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;

            if (moving) {
                return state.setAndContinue(LivingSculkAnimations.WALKING);
            }

            // Fallback idle
            return state.setAndContinue(LivingSculkAnimations.IDLE);
        }));
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
