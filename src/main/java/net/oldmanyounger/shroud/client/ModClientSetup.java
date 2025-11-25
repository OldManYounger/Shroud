package net.oldmanyounger.shroud.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Registers client-side render layers used by custom Shroud blocks.
 */
@SuppressWarnings("deprecation")
@EventBusSubscriber(modid = Shroud.MOD_ID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class ModClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SCULK_PORTAL.get(), RenderType.translucent());
        });
    }
}
