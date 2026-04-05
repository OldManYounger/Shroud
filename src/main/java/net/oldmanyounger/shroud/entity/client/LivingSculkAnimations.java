package net.oldmanyounger.shroud.entity.client;

import software.bernie.geckolib.animation.RawAnimation;

/**
 * Centralizes reusable GeckoLib animation definitions for Living Sculk.
 *
 * <p>This class exposes static {@link RawAnimation} entries for idle, locomotion,
 * attack, and vibration-reaction states so controller code can reference a single,
 * consistent source of animation keys.
 *
 * <p>In the broader context of the project, this class is part of the client
 * animation coordination layer that keeps runtime animation selection aligned with
 * authored GeckoLib animation assets.
 */
public final class LivingSculkAnimations {

    // Looped idle animation
    public static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("animation.living_sculk.idle");

    // One-shot alternate idle variation
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

    // Prevents instantiation of this animation constants holder.
    private LivingSculkAnimations() {
    }
}