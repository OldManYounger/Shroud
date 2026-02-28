package net.oldmanyounger.shroud.entity.client;

import software.bernie.geckolib.animation.RawAnimation;

public final class BlightedShadeAnimations {

    public static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("idle");

    public static final RawAnimation IDLE_ALTERNATE =
            RawAnimation.begin().thenLoop("idle_alternate");

    public static final RawAnimation WALKING =
            RawAnimation.begin().thenLoop("walking");

    public static final RawAnimation ATTACK =
            RawAnimation.begin().thenPlay("attack");

    public static final RawAnimation VIBRATION_REACT =
            RawAnimation.begin().thenPlay("vibration_react");

    private BlightedShadeAnimations() {}
}
