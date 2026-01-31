package net.oldmanyounger.shroud.client;

import net.neoforged.fml.common.EventBusSubscriber;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.GrassColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public final class ModColorHandlers {

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> {
                    if (tintIndex != 0) {
                        return -1;
                    }

                    if (level != null && pos != null) {
                        return BiomeColors.getAverageGrassColor(level, pos);
                    }

                    return GrassColor.get(0.5D, 1.0D);
                },
                ModBlocks.SCULK_GRASS.get()
        );
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        final int sculkGrassItemTint = 0xFF000000 | 18011;

        event.register(
                (stack, tintIndex) -> tintIndex == 0 ? sculkGrassItemTint : -1,
                ModBlocks.SCULK_GRASS.asItem()
        );
    }

    private ModColorHandlers() {}
}
