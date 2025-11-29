package net.oldmanyounger.shroud;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.entity.ModEntities;
import net.oldmanyounger.shroud.entity.client.LivingSculkRenderer;
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

        ModEntities.register(modEventBus);

        ModSounds.SOUND_EVENTS.register(modEventBus);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.LIVING_SCULK.get(), LivingSculkRenderer::new);
        }
    }

    /** Runs common initialization after registry preparation on both client and server */
    private void commonSetup(FMLCommonSetupEvent event) {
        // Reserved for future networking, capability registration, or synced setup tasks
    }
}
