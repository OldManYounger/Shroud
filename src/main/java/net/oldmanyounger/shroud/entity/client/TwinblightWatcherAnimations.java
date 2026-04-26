package net.oldmanyounger.shroud.entity.client;

import software.bernie.geckolib.animation.RawAnimation;

/**
 * Centralizes GeckoLib animation definitions for Twinblight Watcher
 *
 * <p>This class exposes animation keys for locomotion and triggerable action states, including the renamed secondary idle animation key {@code idle_eye_watch}.
 *
 * <p>In the broader context of the project, this class is part of Shroud's client animation coordination layer that keeps entity controller logic and authored animation asset names synchronized.
 */
public final class TwinblightWatcherAnimations {

    // ==================================
    //  ANIMATION KEYS
    // ==================================

    // Looped idle animation
    public static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("idle");

    // One-shot alternate idle variation
    public static final RawAnimation IDLE_EYE_WATCH =
            RawAnimation.begin().thenPlay("idle_eye_watch");

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
    private TwinblightWatcherAnimations() {
    }
}