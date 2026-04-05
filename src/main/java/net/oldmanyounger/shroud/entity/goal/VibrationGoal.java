package net.oldmanyounger.shroud.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;
import java.util.Objects;

/**
 * Moves a {@link PathfinderMob} toward its last stored vibration location.
 *
 * <p>This goal is designed for mobs implementing {@link VibrationListener}. It
 * starts when a vibration position exists, pathfinds toward that position, and
 * stops once the target is reached, invalid, or no longer actively navigated.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * sensory-AI behavior layer that lets custom hostile entities react to nearby
 * game events and pressure players through sound-driven pursuit.
 */
public class VibrationGoal extends Goal {

    // ============================================================
    //  FIELDS / CONSTANTS
    // ============================================================

    // Mob instance controlled by this goal
    private final PathfinderMob mob;

    // Movement speed modifier used while pathing to vibration positions
    private final double speedModifier;

    // Distance threshold considered "close enough" to the vibration source
    private static final double STOP_RADIUS = 1.5D;

    // Squared stop threshold to avoid repeated square roots.
    private static final double STOP_RADIUS_SQR = STOP_RADIUS * STOP_RADIUS;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================

    // Creates a vibration pursuit goal for the given mob and movement speed
    public VibrationGoal(PathfinderMob mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;

        // This goal only controls movement.
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    // ============================================================
    //  GOAL STATE
    // ============================================================

    // Starts when the mob has a non-null vibration location
    @Override
    public boolean canUse() {
        return mob instanceof VibrationListener listener
                && listener.getVibrationLocation() != null;
    }

    // Continues while target remains valid, unreached, and actively navigated
    @Override
    public boolean canContinueToUse() {
        if (!(mob instanceof VibrationListener listener)) {
            return false;
        }

        BlockPos vibration = listener.getVibrationLocation();
        if (vibration == null) {
            return false;
        }

        // Computes squared distance from mob center to vibration block center
        double dx = (vibration.getX() + 0.5D) - this.mob.getX();
        double dy = (double) vibration.getY() - this.mob.getY();
        double dz = (vibration.getZ() + 0.5D) - this.mob.getZ();
        double distSq = dx * dx + dy * dy + dz * dz;

        // Clears vibration and stops when close enough to target
        if (distSq <= STOP_RADIUS_SQR) {
            listener.setVibrationLocation(null);
            return false;
        }

        // Clears vibration and stops when navigation has ended prematurely
        if (this.mob.getNavigation().isDone()) {
            listener.setVibrationLocation(null);
            return false;
        }

        // Continues only when navigation is still aimed at the same vibration block
        BlockPos navTarget = this.mob.getNavigation().getTargetPos();
        return navTarget != null && Objects.equals(navTarget, vibration);
    }

    // ============================================================
    //  LIFECYCLE
    // ============================================================

    // Builds and starts a path to the current vibration position
    @Override
    public void start() {
        if (!(mob instanceof VibrationListener listener)) {
            return;
        }

        BlockPos vibration = listener.getVibrationLocation();
        if (vibration == null) {
            return;
        }

        // Attempts path creation and movement toward vibration source
        Path path = this.mob.getNavigation().createPath(vibration, 0);
        if (path != null) {
            this.mob.getNavigation().moveTo(path, this.speedModifier);
        } else {
            // Clears vibration target when pathing fails
            listener.setVibrationLocation(null);
        }
    }

    // Stops mob navigation when the goal ends
    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }
}