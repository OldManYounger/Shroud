package net.oldmanyounger.shroud.entity.client;

import software.bernie.geckolib.animation.RawAnimation;

/**
 * Centralizes reusable GeckoLib animation definitions for the Blighted Shade.
 *
 * <p>This class provides named raw animation sequences used by the entity's
 * animation controller logic, including looped locomotion states and one-shot
 * reaction/attack clips.
 *
 * <p>In the broader context of the project, this class is part of the client
 * animation wiring layer that keeps animation keys consistent between code and
 * the exported animation JSON assets.
 */
public final class BlightedShadeAnimations {

    // Looped base idle animation
    public static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("idle");

    // Looped alternate idle animation for variation
    public static final RawAnimation IDLE_ALTERNATE =
            RawAnimation.begin().thenLoop("idle_alternate");

    // Looped walking animation
    public static final RawAnimation WALKING =
            RawAnimation.begin().thenLoop("walking");

    // One-shot melee attack animation
    public static final RawAnimation ATTACK =
            RawAnimation.begin().thenPlay("attack");

    // One-shot vibration reaction animation
    public static final RawAnimation VIBRATION_REACT =
            RawAnimation.begin().thenPlay("vibration_react");

    // Prevents instantiation of this animation constants holder
    private BlightedShadeAnimations() {}
}