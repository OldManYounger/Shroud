package net.oldmanyounger.shroud.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.ModEntities;
import net.oldmanyounger.shroud.entity.custom.LivingSculkEntity;

@EventBusSubscriber(modid = Shroud.MOD_ID)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // Ensure LivingSculkEntity has a static createAttributes() returning AttributeSupplier.Builder
        event.put(ModEntities.LIVING_SCULK.get(), LivingSculkEntity.createAttributes().build());
    }
}