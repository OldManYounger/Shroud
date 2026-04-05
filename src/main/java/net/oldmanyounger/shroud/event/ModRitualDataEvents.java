package net.oldmanyounger.shroud.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.ritual.recipe.RitualRecipeManager;

/**
 * Registers ritual datapack reload listeners.
 *
 * <p>This subscriber hooks ritual JSON loading into the server datapack reload cycle so ritual
 * recipes are available after world load and when datapacks are reloaded.
 *
 * <p>In the broader context of the project, this class connects ritual data definitions to the
 * runtime system without hardcoding recipes in Java.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public class ModRitualDataEvents {

    // Registers ritual recipe loader during reload listener setup
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(RitualRecipeManager.INSTANCE);
    }
}