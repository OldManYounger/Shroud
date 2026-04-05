package net.oldmanyounger.shroud.util;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.oldmanyounger.shroud.item.ModItems;

/**
 * Registers client-side item property predicates for model override behavior.
 *
 * <p>This class configures custom property functions used by item models, such as
 * bow pull and pulling states for animated model predicate switching.
 *
 * <p>In the broader context of the project, this class is part of Shroud's client
 * rendering configuration layer that connects gameplay item state to visual model
 * presentation in first-person and inventory views.
 */
public class ModItemProperties {

    // Registers all custom item model predicates during client setup
    public static void addCustomItemProperties() {
        makeCustomBow(ModItems.EVENTIDE_BOW.get());
    }

    // Registers pull and pulling predicates for a custom bow item
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