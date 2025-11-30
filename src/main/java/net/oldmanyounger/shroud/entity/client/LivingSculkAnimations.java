package net.oldmanyounger.shroud.entity.client;

import software.bernie.geckolib.animation.RawAnimation;

/**
 * Central repository of GeckoLib {@link RawAnimation} definitions
 * for the {@code LivingSculkEntity}
 * <p>
 * This class defines all animation sequences used by the Living Sculk,
 * including idle loops, walk cycles, attack animations, and vibration
 * reaction cues. Each animation is referenced by its controller inside
 * the entity class and is built using GeckoLib's fluent
 * {@link RawAnimation} builder API.
 * <p>
 * The class is declared {@code final} and provides a private constructor
 * to prevent instantiation, serving solely as a static animation container
 */
public final class LivingSculkAnimations {

    // Prevent instantiation
    private LivingSculkAnimations() {
    }

    // Looped idle animation
    public static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("animation.living_sculk.idle");

    // Idle alternative animation (non-looping)
    public static final RawAnimation IDLE_HEAD_SPLIT =
            RawAnimation.begin().thenPlay("animation.living_sculk.idle_head_split");

    // Looped walking animation
    public static final RawAnimation WALKING =
            RawAnimation.begin().thenLoop("animation.living_sculk.walking");

    // One-shot melee attack animation
    public static final RawAnimation ATTACK =
            RawAnimation.begin().thenPlay("animation.living_sculk.attack");

    // One-shot reaction animation for vibration events
    public static final RawAnimation VIBRATION_REACT =
            RawAnimation.begin().thenPlay("animation.living_sculk.vibration_react");
}
