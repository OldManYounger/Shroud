package net.oldmanyounger.shroud.entity.client;

import software.bernie.geckolib.animation.RawAnimation;

/**
 * Central repository of GeckoLib {@link RawAnimation} definitions
 * for the {@code UmbralHowlerEntity}
 * <p>
 * This class defines all animation sequences used by the Umbral Howler,
 * including idle loops, walk cycles, attack animations, and vibration
 * reaction cues. Each animation is referenced by its controller inside
 * the entity class and is built using GeckoLib's fluent
 * {@link RawAnimation} builder API.
 * <p>
 * The class is declared {@code final} and provides a private constructor
 * to prevent instantiation, serving solely as a static animation container
 */
public final class UmbralHowlerAnimations {

    // Prevent instantiation
    private UmbralHowlerAnimations() {
    }

    // Looped idle animation
    public static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("idle");

    // Idle alternative animation (non-looping)
    public static final RawAnimation IDLE_SPIKE =
            RawAnimation.begin().thenPlay("idle_spike");

    // Looped walking animation
    public static final RawAnimation WALKING =
            RawAnimation.begin().thenLoop("walking");

    // One-shot melee attack animation
    public static final RawAnimation ATTACK =
            RawAnimation.begin().thenPlay("attack");

    // One-shot reaction animation for vibration events
    public static final RawAnimation VIBRATION_REACT =
            RawAnimation.begin().thenPlay("vibration_react");
}
