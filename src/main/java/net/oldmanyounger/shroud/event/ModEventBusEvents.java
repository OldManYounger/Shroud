package net.oldmanyounger.shroud.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.ModEntities;
import net.oldmanyounger.shroud.entity.custom.BlightedShadeEntity;
import net.oldmanyounger.shroud.entity.custom.LivingSculkEntity;
import net.oldmanyounger.shroud.entity.custom.UmbralHowlerEntity;

/**
 * Registers entity attribute sets during the mod event bus lifecycle.
 *
 * <p>This class wires each custom Shroud entity type to its corresponding
 * attribute builder so health, speed, damage, and related stats are available
 * at runtime.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * initialization pipeline that finalizes custom entity definitions before play.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public class ModEventBusEvents {

    // Registers base attributes for all Shroud custom entities
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.LIVING_SCULK.get(), LivingSculkEntity.createAttributes().build());
        event.put(ModEntities.UMBRAL_HOWLER.get(), UmbralHowlerEntity.createAttributes().build());
        event.put(ModEntities.BLIGHTED_SHADE.get(), BlightedShadeEntity.createAttributes().build());
    }
}