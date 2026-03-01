package net.oldmanyounger.shroud.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;

/** Registers all custom sound events for the Shroud mod */
public final class ModSounds {

    /** Deferred register for all Shroud sound events */
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Shroud.MOD_ID);

    /** Sculk portal activation sound event used when activating the portal frame */
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_PORTAL_ACTIVATE =
            SOUND_EVENTS.register(
                    "sculk_portal_activate",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "sculk_portal_activate")
                    )
            );

    // ============================================================
    //  LIVING SCULK
    // ============================================================

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LIVING_SCULK_AMBIENT =
            SOUND_EVENTS.register(
                    "entity.living_sculk.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.living_sculk.ambient")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LIVING_SCULK_HURT =
            SOUND_EVENTS.register(
                    "entity.living_sculk.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.living_sculk.hurt")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LIVING_SCULK_DEATH =
            SOUND_EVENTS.register(
                    "entity.living_sculk.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.living_sculk.death")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LIVING_SCULK_STEP =
            SOUND_EVENTS.register(
                    "entity.living_sculk.step",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.living_sculk.step")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_LIVING_SCULK_VIBRATION_REACT =
            SOUND_EVENTS.register(
                    "entity.living_sculk.vibration_react",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.living_sculk.vibration_react")
                    )
            );

    // ============================================================
    //  UMBRAL HOWLER
    // ============================================================

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_UMBRAL_HOWLER_AMBIENT =
            SOUND_EVENTS.register(
                    "entity.umbral_howler.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.umbral_howler.ambient")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_UMBRAL_HOWLER_HURT =
            SOUND_EVENTS.register(
                    "entity.umbral_howler.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.umbral_howler.hurt")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_UMBRAL_HOWLER_DEATH =
            SOUND_EVENTS.register(
                    "entity.umbral_howler.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.umbral_howler.death")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_UMBRAL_HOWLER_STEP =
            SOUND_EVENTS.register(
                    "entity.umbral_howler.step",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.umbral_howler.step")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_UMBRAL_HOWLER_VIBRATION_REACT =
            SOUND_EVENTS.register(
                    "entity.umbral_howler.vibration_react",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.umbral_howler.vibration_react")
                    )
            );


    // ============================================================
    //  BLIGHTED SHADE
    // ============================================================

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLIGHTED_SHADE_AMBIENT =
            SOUND_EVENTS.register(
                    "entity.blighted_shade.ambient",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.blighted_shade.ambient")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLIGHTED_SHADE_HURT =
            SOUND_EVENTS.register(
                    "entity.blighted_shade.hurt",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.blighted_shade.hurt")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLIGHTED_SHADE_DEATH =
            SOUND_EVENTS.register(
                    "entity.blighted_shade.death",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.blighted_shade.death")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLIGHTED_SHADE_STEP =
            SOUND_EVENTS.register(
                    "entity.blighted_shade.step",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.blighted_shade.step")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLIGHTED_SHADE_VIBRATION_REACT =
            SOUND_EVENTS.register(
                    "entity.blighted_shade.vibration_react",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "entity.blighted_shade.vibration_react")
                    )
            );

    // ============================================================
    //  OTHER SOUNDS
    // ============================================================

    public static final DeferredHolder<SoundEvent, SoundEvent> BLOCK_SCULK_EMITTER_SPEW =
            SOUND_EVENTS.register(
                    "block.sculk_emitter.spew",
                    () -> SoundEvent.createFixedRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "block.sculk_emitter.spew"),
                            40.0F // max audible range in blocks (tune: 32, 40, 48, etc.)
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> LIMBO_AMBIENT_LOOP =
            SOUND_EVENTS.register(
                    "limbo.ambient_loop",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "limbo.ambient_loop")
                    )
            );

    /** Private constructor to prevent instantiation of the registry holder class */
    private ModSounds() {
    }
}