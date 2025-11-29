package net.oldmanyounger.shroud.item;

import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.ModEntities;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Shroud.MOD_ID);

    public static final DeferredItem<Item> LIVING_SCULK_SPAWN_EGG = ITEMS.register("living_sculk_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.LIVING_SCULK, 0x31afaf, 0xffac00,
                    new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}