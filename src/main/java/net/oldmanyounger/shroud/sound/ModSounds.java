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

    /** Private constructor to prevent instantiation of the registry holder class */
    private ModSounds() {
    }
}
