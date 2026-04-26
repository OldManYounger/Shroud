package net.oldmanyounger.shroud.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.entity.ModBlockEntities;
import net.oldmanyounger.shroud.client.render.ModCorruptedReliquaryBlockEntityRenderer;
import net.oldmanyounger.shroud.entity.ModEntities;
import net.oldmanyounger.shroud.entity.client.LivingSculkRenderer;
import net.oldmanyounger.shroud.entity.client.TwinblightWatcherRenderer;
import net.oldmanyounger.shroud.item.ModItems;

/**
 * Handles client-only mod event hooks for rendering and camera behavior.
 *
 * <p>This subscriber registers entity renderers and applies dynamic FOV adjustments
 * while specific mod items are in use.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * client integration layer that connects custom content to rendering and
 * first-person presentation systems.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {

    // Registers client entity renderers
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LIVING_SCULK.get(), LivingSculkRenderer::new);
        event.registerEntityRenderer(ModEntities.TWINBLIGHT_WATCHER.get(), TwinblightWatcherRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CORRUPTED_RELIQUARY.get(), ModCorruptedReliquaryBlockEntityRenderer::new);
    }

    // Adjusts FOV while drawing the Eventide bow
    @SubscribeEvent
    public static void onComputeFovModifierEvent(ComputeFovModifierEvent event) {
        if (event.getPlayer().isUsingItem() && event.getPlayer().getUseItem().getItem() == ModItems.EVENTIDE_BOW.get()) {
            float fovModifier = 1f;
            int ticksUsingItem = event.getPlayer().getTicksUsingItem();
            float deltaTicks = (float) ticksUsingItem / 20f;

            if (deltaTicks > 1f) {
                deltaTicks = 1f;
            } else {
                deltaTicks *= deltaTicks;
            }

            fovModifier *= 1f - deltaTicks * 0.15f;
            event.setNewFovModifier(fovModifier);
        }
    }
}