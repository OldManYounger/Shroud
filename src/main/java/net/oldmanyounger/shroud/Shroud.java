package net.oldmanyounger.shroud;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.item.ModCreativeModeTabs;
import net.oldmanyounger.shroud.item.ModItems;
import net.oldmanyounger.shroud.sound.ModSounds;
import org.slf4j.Logger;

/** Main entry point for the Shroud mod, responsible for all top-level initialization */
@Mod(Shroud.MOD_ID)
public class Shroud {

    /** Unique namespace identifier for all Shroud mod content */
    public static final String MOD_ID = "shroud";

    /** Logger instance used for diagnostic and debugging messages */
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Registers blocks, items, and creative tabs when the mod loads */
    public Shroud(IEventBus modEventBus) {

        modEventBus.addListener(this::commonSetup);

        ModCreativeModeTabs.register(modEventBus);

        ModBlocks.register(modEventBus);

        ModItems.register(modEventBus);

        ModSounds.SOUND_EVENTS.register(modEventBus);
    }

    /** Runs common initialization after registry preparation on both client and server */
    private void commonSetup(FMLCommonSetupEvent event) {
        // Reserved for future networking, capability registration, or synced setup tasks
    }
}
