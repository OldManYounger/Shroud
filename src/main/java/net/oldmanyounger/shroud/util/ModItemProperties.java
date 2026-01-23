package net.oldmanyounger.shroud.util;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.oldmanyounger.shroud.item.ModItems;

/** Registers client-side item property predicates used by item model overrides */
public class ModItemProperties {

    /** Call this during client setup (enqueueWork) */
    public static void addCustomItemProperties() {
        makeCustomBow(ModItems.EVENTIDE_BOW.get());
    }

    private static void makeCustomBow(Item item) {
        ItemProperties.register(
                item,
                ResourceLocation.withDefaultNamespace("pull"),
                (stack, level, entity, seed) -> {
                    if (entity == null) {
                        return 0.0F;
                    }
                    return entity.getUseItem() != stack
                            ? 0.0F
                            : (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F;
                }
        );

        ItemProperties.register(
                item,
                ResourceLocation.withDefaultNamespace("pulling"),
                (stack, level, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F
        );
    }
}
