package net.oldmanyounger.shroud.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;
import java.util.Objects;

/**
 * AI goal that moves a {@link PathfinderMob} toward the last known vibration location
 * <p>
 * This goal is intended to be used with entities that implement a {@code VibrationListener}
 * interface exposing a vibration target position. When a vibration position is available,
 * the goal:
 * <ul>
 *   <li>Begins pathfinding toward that block position</li>
 *   <li>Continues pathing while the navigation remains valid and the position is unchanged</li>
 *   <li>Stops when the entity is within a configurable radius of the vibration target</li>
 *   <li>Clears the vibration location when reached or when pathing fails</li>
 * </ul>
 * The concept and behavior are inspired by KyaniteMods' Deeper And Darker implementation,
 * adapted here for the Shroud mod to allow custom mobs to home in on sound-based cues
 */
public class VibrationGoal extends Goal {

    // Mob instance that will be moved by this goal
    private final PathfinderMob mob;

    // Movement speed modifier used when pathing toward the vibration
    private final double speedModifier;

    // How close is "close enough" to the vibration source
    private static final double STOP_RADIUS = 1.5D;

    // Precomputed squared stop radius to avoid repeated square root operations
    private static final double STOP_RADIUS_SQR = STOP_RADIUS * STOP_RADIUS;

    // Constructs a new vibration goal for the given mob at the specified movement speed
    public VibrationGoal(PathfinderMob mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;

        // This goal only affects movement
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    // Determines whether the goal can start based on the presence of a vibration location
    @Override
    public boolean canUse() {
        if (mob instanceof VibrationListener listener) {
            return listener.getVibrationLocation() != null;
        }
        return false;
    }

    // Determines whether the goal should continue running while pathing toward the vibration
    @Override
    public boolean canContinueToUse() {
        if (!(mob instanceof VibrationListener listener)) {
            return false;
        }

        BlockPos vibration = listener.getVibrationLocation();
        if (vibration == null) {
            return false;
        }

        // Distance from mob to the vibration location
        double dx = (vibration.getX() + 0.5D) - this.mob.getX();
        double dy = (double) vibration.getY() - this.mob.getY();
        double dz = (vibration.getZ() + 0.5D) - this.mob.getZ();
        double distSq = dx * dx + dy * dy + dz * dz;

        // If within STOP_RADIUS, we consider the vibration "reached"
        if (distSq <= STOP_RADIUS_SQR) {
            listener.setVibrationLocation(null);
            return false;
        }

        // If navigation finished and we still aren't close enough, stop and clear
        if (this.mob.getNavigation().isDone()) {
            listener.setVibrationLocation(null);
            return false;
        }

        // Otherwise, keep going as long as we're still pathing toward that same block
        BlockPos navTarget = this.mob.getNavigation().getTargetPos();
        return navTarget != null && Objects.equals(navTarget, vibration);
    }

    // Starts moving toward the current vibration location when the goal begins
    @Override
    public void start() {
        if (!(mob instanceof VibrationListener listener)) {
            return;
        }

        BlockPos vibration = listener.getVibrationLocation();
        if (vibration == null) {
            return;
        }

        // Attempt to create a path toward the vibration position
        Path path = this.mob.getNavigation().createPath(vibration, 0);
        if (path != null) {
            this.mob.getNavigation().moveTo(path, this.speedModifier);
        } else {
            // Could not path there; clear vibration so we don't get stuck
            listener.setVibrationLocation(null);
        }
    }

    // Stops navigation when the goal ends
    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }
}
