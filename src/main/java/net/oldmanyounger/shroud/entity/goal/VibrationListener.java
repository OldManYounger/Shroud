package net.oldmanyounger.shroud.entity.goal;

import net.minecraft.core.BlockPos;

/**
 * Simple contract for entities that can react to vibration-based events
 * <p>
 * This interface is intended to be implemented by mobs or other entities that
 * participate in a vibration-driven AI system. It exposes a single mutable
 * {@link BlockPos} representing the current vibration target that:
 * <ul>
 *   <li>Can be set when a vibration is detected by a vibration system user</li>
 *   <li>Can be read by AI goals such as {@code VibrationGoal} to drive movement</li>
 *   <li>Can be cleared when the vibration has been reached or is no longer valid</li>
 * </ul>
 * Implementations are free to store the vibration position however they choose,
 * but should ensure that:
 * <ul>
 *   <li>{@link #getVibrationLocation()} returns {@code null} when there is no active target</li>
 *   <li>{@link #setVibrationLocation(BlockPos)} is used to update or clear the target</li>
 * </ul>
 * The concept and behavior are inspired by KyaniteMods' Deeper And Darker implementation,
 * adapted here for the Shroud mod to provide a lightweight abstraction to decouple vibration
 * detection from pathfinding logic.
 */
public interface VibrationListener {
    BlockPos getVibrationLocation();
    void setVibrationLocation(BlockPos pos);
}
