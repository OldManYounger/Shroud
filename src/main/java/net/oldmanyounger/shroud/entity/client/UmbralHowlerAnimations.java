package net.oldmanyounger.shroud.entity.client;

import software.bernie.geckolib.animation.RawAnimation;

/**
 * Centralizes reusable GeckoLib animation definitions for Umbral Howler.
 *
 * <p>This class exposes static {@link RawAnimation} entries for idle, locomotion,
 * attack, and vibration-reaction states so controller code can reference a single,
 * consistent source of animation keys.
 *
 * <p>In the broader context of the project, this class is part of the client
 * animation coordination layer that keeps runtime animation selection aligned with
 * authored GeckoLib animation assets.
 */
public final class UmbralHowlerAnimations {

    // Looped idle animation
    public static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("idle");

    // One-shot alternate idle variation
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

    // Prevents instantiation of this animation constants holder
    private UmbralHowlerAnimations() {
    }
}