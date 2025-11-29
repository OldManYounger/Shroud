package net.oldmanyounger.shroud.entity.client;

import software.bernie.geckolib.animation.RawAnimation;

public final class LivingSculkAnimations {
    private LivingSculkAnimations() {
    }

    // Looped idle
    public static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("animation.living_sculk.idle");

    // Looped walk
    public static final RawAnimation WALKING =
            RawAnimation.begin().thenLoop("animation.living_sculk.walking");

    // One-shot attack
    public static final RawAnimation ATTACK =
            RawAnimation.begin().thenPlay("animation.living_sculk.attack");
}
