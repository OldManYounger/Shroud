package net.oldmanyounger.shroud.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.ritual.recipe.CorruptedReliquaryRecipeManager;

/**
 * Registers datapack reload listeners used by Shroud runtime systems.
 *
 * <p>This class wires custom JSON-backed managers into the server resource reload
 * lifecycle so recipe and data definitions are refreshed when datapacks reload.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * data synchronization layer that keeps runtime registries aligned with
 * datapack-authored content changes.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public final class ModDatapackReloadEvents {

    // Prevents instantiation of this static event handler class
    private ModDatapackReloadEvents() {
    }

    // Registers custom reload listeners during reload listener collection
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(CorruptedReliquaryRecipeManager.INSTANCE);
    }
}