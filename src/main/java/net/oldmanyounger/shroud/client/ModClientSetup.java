package net.oldmanyounger.shroud.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Performs client-only setup work for Shroud block rendering.
 *
 * <p>This class is responsible for registering any special render layers needed
 * by custom blocks so they display correctly on the client. That includes blocks
 * which require translucent rendering, cutout rendering, or other non-solid
 * visual handling that differs from default opaque block rendering.
 *
 * <p>In the broader context of the project, this class serves as part of the
 * client bootstrap pipeline, ensuring Shroud's custom block visuals are aligned
 * with Minecraft's rendering system as soon as the client finishes setup.
 */
@SuppressWarnings("deprecation")
@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public class ModClientSetup {

    // Registers client-side render layers for Shroud blocks that need special transparency handling
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Render the Shroud portal using a translucent layer
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SCULK_PORTAL.get(), RenderType.translucent());

            // Render sculk grass using cutout-mipped behavior like vanilla grass blocks
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SCULK_GRASS.get(), RenderType.cutoutMipped());
        });
    }
}