package net.oldmanyounger.shroud.client;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.GrassColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * Registers block and item tint handlers for Shroud content that should respond
 * to biome coloring or custom inventory coloration.
 *
 * <p>This class is currently focused on color logic for the mod's sculk grass,
 * allowing the placed block to use biome-sensitive grass coloring while the item
 * form uses a fixed tint value for consistent inventory rendering. Keeping those
 * handlers together in one class makes the client-side color pipeline easier to
 * expand as more tint-aware content is added.
 *
 * <p>In the broader context of the project, this class helps Shroud's custom
 * vegetation and terrain blocks feel visually integrated with Minecraft's world
 * coloring systems instead of appearing as flat, untinted textures.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public final class ModColorHandlers {

    // Prevent instantiation because this class only contains static color registration hooks
    private ModColorHandlers() {
    }

    // Registers biome-aware block tint handlers
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> {
                    // Only tint the primary grass layer
                    if (tintIndex != 0) {
                        return -1;
                    }

                    // Use biome grass coloring when world context is available
                    if (level != null && pos != null) {
                        return BiomeColors.getAverageGrassColor(level, pos);
                    }

                    // Fall back to a default grass color when no world context exists
                    return GrassColor.get(0.5D, 1.0D);
                },
                ModBlocks.SCULK_GRASS.get()
        );
    }

    // Registers fixed inventory/item tint handlers
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Preserve alpha while applying the intended item tint color
        final int sculkGrassItemTint = 0xFF000000 | 18011;

        event.register(
                (stack, tintIndex) -> tintIndex == 0 ? sculkGrassItemTint : -1,
                ModBlocks.SCULK_GRASS.asItem()
        );
    }
}