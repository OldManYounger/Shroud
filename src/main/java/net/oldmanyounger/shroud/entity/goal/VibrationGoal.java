package net.oldmanyounger.shroud.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;
import java.util.Objects;

// Inspiration taken from KyaniteMods (DeeperAndDarker)
public class VibrationGoal extends Goal {
    private final PathfinderMob mob;
    private final double speedModifier;

    public VibrationGoal(PathfinderMob mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mob instanceof VibrationListener listener) {
            return listener.getVibrationLocation() != null;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!(mob instanceof VibrationListener listener)) {
            return false;
        }

        // If navigation finished, clear vibration and stop
        if (this.mob.getNavigation().isDone()) {
            listener.setVibrationLocation(null);
            return false;
        }

        BlockPos navTarget = this.mob.getNavigation().getTargetPos();
        BlockPos vibration = listener.getVibrationLocation();
        return Objects.equals(navTarget, vibration);
    }

    @Override
    public void start() {
        if (!(mob instanceof VibrationListener listener)) {
            return;
        }

        BlockPos vibration = listener.getVibrationLocation();
        if (vibration == null) {
            return;
        }

        Path path = this.mob.getNavigation().createPath(vibration, 0);
        if (path != null) {
            this.mob.getNavigation().moveTo(path, this.speedModifier);
        } else {
            // Could not path there; clear vibration so we don't get stuck
            listener.setVibrationLocation(null);
        }
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }
}