package net.oldmanyounger.shroud.item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;

/** Declares and registers all standalone items for the Shroud mod */
public class ModItems {

    /** Central item registry for Shroud, used for all items and BlockItems */
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Shroud.MOD_ID);

    /** Registers this item registry with the NeoForge event bus */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
