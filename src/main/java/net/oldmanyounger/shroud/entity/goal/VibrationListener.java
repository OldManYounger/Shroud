package net.oldmanyounger.shroud.entity.goal;

import net.minecraft.core.BlockPos;
import javax.annotation.Nullable;

/**
 * Contract for entities that expose a mutable vibration target position.
 *
 * <p>Implementers provide read/write access to the current vibration location so
 * vibration-detection systems can publish target positions and AI goals can
 * consume and clear them during navigation.
 *
 * <p>In the broader context of the project, this interface is part of Shroud's
 * sensory-AI abstraction layer, decoupling vibration event handling from specific
 * movement-goal implementations.
 */
public interface VibrationListener {

    // Returns the current vibration target, or null when no target is active
    @Nullable
    BlockPos getVibrationLocation();

    // Sets or clears the current vibration target
    void setVibrationLocation(@Nullable BlockPos pos);
}