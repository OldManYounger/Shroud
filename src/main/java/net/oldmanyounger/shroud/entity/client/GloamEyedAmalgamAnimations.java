// File: src/main/java/net/oldmanyounger/shroud/entity/client/GloamEyedAmalgamAnimations.java
package net.oldmanyounger.shroud.entity.client;

import software.bernie.geckolib.animation.RawAnimation;

/**
 * Centralizes GeckoLib animation definitions for Gloam Eyed Amalgam.
 *
 * <p>This class exposes animation keys for locomotion and triggerable action states, including roar-on-target-acquire sequencing support.
 *
 * <p>In the broader context of the project, this class is part of Shroud's client animation coordination layer that keeps entity controller logic and authored animation asset names synchronized.
 */
public final class GloamEyedAmalgamAnimations {

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

    // Looped faster walking animation used while pursuing an acquired target
    public static final RawAnimation WALKING_PURSUIT =
            RawAnimation.begin().thenLoop("walking_pursuit");

    // One-shot melee attack animation
    public static final RawAnimation ATTACK =
            RawAnimation.begin().thenPlay("attack");

    // One-shot reaction animation for vibration events
    public static final RawAnimation VIBRATION_REACT =
            RawAnimation.begin().thenPlay("vibration_react");

    // One-shot roar animation when a target is acquired
    public static final RawAnimation ROAR =
            RawAnimation.begin().thenPlay("roar");

    // One-shot pull aura windup animation
    public static final RawAnimation PULL_AURA =
            RawAnimation.begin().thenPlay("pull_aura");

    // One-shot emergence animation used when summoned by a sculk shrieker
    public static final RawAnimation ARISE =
            RawAnimation.begin().thenPlay("arise");

    // Prevents instantiation of this animation constants holder
    private GloamEyedAmalgamAnimations() {
    }
}