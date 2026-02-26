package net.oldmanyounger.shroud.entity.client;

import software.bernie.geckolib.animation.RawAnimation;

public final class BlightedShadeAnimations {

    // IMPORTANT: replace these names with the exact animation names from your Blockbench export
    public static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("animation.blighted_shade.idle");

    public static final RawAnimation IDLE_ALTERNATE =
            RawAnimation.begin().thenLoop("animation.blighted_shade.idle_alternate");

    public static final RawAnimation WALKING =
            RawAnimation.begin().thenLoop("animation.blighted_shade.walking");

    public static final RawAnimation ATTACK =
            RawAnimation.begin().thenPlay("animation.blighted_shade.attack");

    public static final RawAnimation VIBRATION_REACT =
            RawAnimation.begin().thenPlay("animation.blighted_shade.vibration_react");

    private BlightedShadeAnimations() {}
}
