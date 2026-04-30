package net.oldmanyounger.shroud.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;

/**
 * Registers all custom sound events used by Shroud content.
 *
 * <p>This class declares entity vocalizations, portal and block utility sounds,
 * and ambient loops through a deferred sound event registry bound to the mod ID.
 *
 * <p>In the broader context of the project, this class is part of Shroud's audio
 * content layer that centralizes sound identifiers for entities, blocks, ambience,
 * and gameplay feedback systems.
 */
public final class ModSounds {

    // ==================================
    //  FIELDS
    // ==================================

    // Deferred register for all Shroud sound events
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Shroud.MOD_ID);

    // Portal activation sound used when starting frame activation
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_PORTAL_ACTIVATE =
            SOUND_EVENTS.register(
                    "sculk_portal_activate",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "sculk_portal_activate")
                    )
            );

    // ==================================
    //  LIVING SCULK
    // ==================================

    // Living Sculk ambient sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LIVING_SCULK_AMBIENT =
            SOUND_EVENTS.register(
                    "entity.living_sculk.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.living_sculk.ambient")
                    )
            );

    // Living Sculk hurt sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LIVING_SCULK_HURT =
            SOUND_EVENTS.register(
                    "entity.living_sculk.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.living_sculk.hurt")
                    )
            );

    // Living Sculk death sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LIVING_SCULK_DEATH =
            SOUND_EVENTS.register(
                    "entity.living_sculk.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.living_sculk.death")
                    )
            );

    // Living Sculk step sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LIVING_SCULK_STEP =
            SOUND_EVENTS.register(
                    "entity.living_sculk.step",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.living_sculk.step")
                    )
            );

    // Living Sculk vibration reaction sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LIVING_SCULK_VIBRATION_REACT =
            SOUND_EVENTS.register(
                    "entity.living_sculk.vibration_react",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.living_sculk.vibration_react")
                    )
            );

    // ==================================
    //  UMBRAL HOWLER
    // ==================================

    // Umbral Howler ambient sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_UMBRAL_HOWLER_AMBIENT =
            SOUND_EVENTS.register(
                    "entity.umbral_howler.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.umbral_howler.ambient")
                    )
            );

    // Umbral Howler hurt sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_UMBRAL_HOWLER_HURT =
            SOUND_EVENTS.register(
                    "entity.umbral_howler.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.umbral_howler.hurt")
                    )
            );

    // Umbral Howler death sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_UMBRAL_HOWLER_DEATH =
            SOUND_EVENTS.register(
                    "entity.umbral_howler.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.umbral_howler.death")
                    )
            );

    // Umbral Howler step sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_UMBRAL_HOWLER_STEP =
            SOUND_EVENTS.register(
                    "entity.umbral_howler.step",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.umbral_howler.step")
                    )
            );

    // Umbral Howler vibration reaction sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_UMBRAL_HOWLER_VIBRATION_REACT =
            SOUND_EVENTS.register(
                    "entity.umbral_howler.vibration_react",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.umbral_howler.vibration_react")
                    )
            );

    // ==================================
    //  BLIGHTED SHADE
    // ==================================

    // Blighted Shade ambient sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLIGHTED_SHADE_AMBIENT =
            SOUND_EVENTS.register(
                    "entity.blighted_shade.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.blighted_shade.ambient")
                    )
            );

    // Blighted Shade hurt sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLIGHTED_SHADE_HURT =
            SOUND_EVENTS.register(
                    "entity.blighted_shade.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.blighted_shade.hurt")
                    )
            );

    // Blighted Shade death sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLIGHTED_SHADE_DEATH =
            SOUND_EVENTS.register(
                    "entity.blighted_shade.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.blighted_shade.death")
                    )
            );

    // Blighted Shade step sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLIGHTED_SHADE_STEP =
            SOUND_EVENTS.register(
                    "entity.blighted_shade.step",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.blighted_shade.step")
                    )
            );

    // Blighted Shade vibration reaction sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLIGHTED_SHADE_VIBRATION_REACT =
            SOUND_EVENTS.register(
                    "entity.blighted_shade.vibration_react",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.blighted_shade.vibration_react")
                    )
            );

    // ==================================
    //  GLOAM-EYED AMALGAM
    // ==================================

    // Gloam Eyed Amalgam heartbeat sound with vanilla-like variable range attenuation
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_GLOAM_EYED_AMALGAM_HEARTBEAT =
            SOUND_EVENTS.register(
                    "entity.gloam_eyed_amalgam.heartbeat",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.gloam_eyed_amalgam.heartbeat")
                    )
            );

    // ==================================
    //  OTHER SOUNDS
    // ==================================

    // Sculk emitter spew block sound with fixed hearing range
    public static final DeferredHolder<SoundEvent, SoundEvent> BLOCK_SCULK_EMITTER_SPEW =
            SOUND_EVENTS.register(
                    "block.sculk_emitter.spew",
                    () -> SoundEvent.createFixedRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "block.sculk_emitter.spew"),
                            40.0F
                    )
            );

    // Limbo ambient loop
    public static final DeferredHolder<SoundEvent, SoundEvent> LIMBO_AMBIENT_LOOP =
            SOUND_EVENTS.register(
                    "limbo.ambient_loop",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "limbo.ambient_loop")
                    )
            );

    // Prevents instantiation of this static registry holder class
    private ModSounds() {
    }
}