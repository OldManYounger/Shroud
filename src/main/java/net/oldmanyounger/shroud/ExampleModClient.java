package net.oldmanyounger.shroud;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Provides client-only bootstrap wiring and setup callbacks for Shroud.
 *
 * <p>This class registers a config screen extension point and handles client setup
 * lifecycle events for logging and client-side initialization tasks.
 *
 * <p>In the broader context of the project, this class is part of Shroud's client
 * startup layer that binds NeoForge client hooks to mod-specific UI and setup behavior.
 */
@Mod(value = Shroud.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public class ExampleModClient {

    // Registers NeoForge config screen factory for this mod container
    public ExampleModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    // Runs client setup actions during mod lifecycle
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Shroud.LOGGER.info("HELLO FROM CLIENT SETUP");
        Shroud.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}