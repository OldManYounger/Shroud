package net.oldmanyounger.shroud;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only mod container entrypoint for Shroud.
 *
 * <p>This class registers NeoForge client extension points that require access to
 * the active mod container.
 *
 * <p>In the broader context of the project, this keeps client container wiring
 * separate from renderer setup, block render layers, and gameplay initialization.
 */
@Mod(value = Shroud.MOD_ID, dist = Dist.CLIENT)
public class ShroudClient {

    // Registers the default NeoForge config screen factory for the Shroud mod container
    public ShroudClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
