package net.oldmanyounger.shroud.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.oldmanyounger.shroud.entity.ModEntities;

/**
 * Custom shulker-style homing projectile fired by the Gloam Eyed Amalgam.
 *
 * <p>This entity uses vanilla {@link ShulkerBullet} movement and hit behavior while keeping
 * a custom entity type so the projectile can use a custom renderer, texture, and model.
 */
public class GloamEyedAmalgamSculkShotEntity extends ShulkerBullet {

    // Creates the projectile for normal entity loading and network spawning
    public GloamEyedAmalgamSculkShotEntity(EntityType<? extends GloamEyedAmalgamSculkShotEntity> entityType, Level level) {
        super(entityType, level);
    }

    // Creates a homing projectile from a custom source position toward the target
    public GloamEyedAmalgamSculkShotEntity(Level level, LivingEntity shooter, Entity target, Direction.Axis axis, Vec3 source) {
        this(ModEntities.GLOAM_EYED_AMALGAM_SCULK_SHOT.get(), level);
        this.setOwner(shooter);
        this.moveTo(source.x, source.y, source.z, this.getYRot(), this.getXRot());
        this.finalTarget = target;
        this.currentMoveDirection = Direction.UP;
        this.selectNextMoveDirection(axis);
    }

    // Runs the shulker bullet movement loop with a custom sculk trail instead of vanilla end rod particles
    @Override
    public void tick() {
        this.tickProjectileBase();

        if (!this.level().isClientSide()) {
            if (this.finalTarget == null && this.targetId != null) {
                this.finalTarget = ((ServerLevel) this.level()).getEntity(this.targetId);

                if (this.finalTarget == null) {
                    this.targetId = null;
                }
            }

            if (this.finalTarget != null
                    && this.finalTarget.isAlive()
                    && (!(this.finalTarget instanceof Player player) || !player.isSpectator())) {
                this.targetDeltaX = Mth.clamp(this.targetDeltaX * 1.025D, -1.0D, 1.0D);
                this.targetDeltaY = Mth.clamp(this.targetDeltaY * 1.025D, -1.0D, 1.0D);
                this.targetDeltaZ = Mth.clamp(this.targetDeltaZ * 1.025D, -1.0D, 1.0D);

                Vec3 movement = this.getDeltaMovement();
                this.setDeltaMovement(movement.add(
                        (this.targetDeltaX - movement.x) * 0.2D,
                        (this.targetDeltaY - movement.y) * 0.2D,
                        (this.targetDeltaZ - movement.z) * 0.2D
                ));
            } else {
                this.applyGravity();
            }

            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

            if (hitResult.getType() != HitResult.Type.MISS && !EventHooks.onProjectileImpact(this, hitResult)) {
                this.hitTargetOrDeflectSelf(hitResult);
            }
        }

        this.checkInsideBlocks();

        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
        ProjectileUtil.rotateTowardsMovement(this, 0.5F);

        if (this.level().isClientSide()) {
            this.spawnSculkTrailParticle(movement);
        } else if (this.finalTarget != null && !this.finalTarget.isRemoved()) {
            this.updateHomingDirection();
        }
    }

    // Runs the projectile base tick without calling ShulkerBullet#tick, which would spawn end rod particles
    private void tickProjectileBase() {
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.hasBeenShot = true;
        }

        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }

        this.baseTick();
    }

    // Spawns the replacement sculk trail particle at the projectile's previous position
    private void spawnSculkTrailParticle(Vec3 movement) {
        this.level().addParticle(
                ParticleTypes.SCULK_SOUL,
                this.getX() - movement.x,
                this.getY() - movement.y + 0.15D,
                this.getZ() - movement.z,
                0.0D,
                0.0D,
                0.0D
        );
    }

    // Updates the vanilla shulker-style homing direction after each flight step expires
    private void updateHomingDirection() {
        if (this.flightSteps > 0) {
            this.flightSteps--;

            if (this.flightSteps == 0) {
                this.selectNextMoveDirection(this.currentMoveDirection == null ? null : this.currentMoveDirection.getAxis());
            }
        }

        if (this.currentMoveDirection != null) {
            BlockPos blockPos = this.blockPosition();
            Direction.Axis axis = this.currentMoveDirection.getAxis();

            if (this.level().loadedAndEntityCanStandOn(blockPos.relative(this.currentMoveDirection), this)) {
                this.selectNextMoveDirection(axis);
            } else {
                BlockPos targetPos = this.finalTarget.blockPosition();

                if (axis == Direction.Axis.X && blockPos.getX() == targetPos.getX()
                        || axis == Direction.Axis.Z && blockPos.getZ() == targetPos.getZ()
                        || axis == Direction.Axis.Y && blockPos.getY() == targetPos.getY()) {
                    this.selectNextMoveDirection(axis);
                }
            }
        }
    }

}

