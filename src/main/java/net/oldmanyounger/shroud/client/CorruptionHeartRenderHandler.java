package net.oldmanyounger.shroud.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerHeartTypeEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.effect.ModMobEffects;

@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public final class CorruptionHeartRenderHandler {
    private CorruptionHeartRenderHandler() {
    }

    @SubscribeEvent
    public static void onPlayerHeartType(PlayerHeartTypeEvent event) {
        if (!event.getEntity().hasEffect(ModMobEffects.CORRUPTION)) {
            return;
        }

        event.setType(CorruptionHeartTypes.corrupted());
    }
}