package net.oldmanyounger.shroud.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.effect.ModCorruptionMobEffect;
import net.oldmanyounger.shroud.effect.ModMobEffects;

@EventBusSubscriber(modid = Shroud.MOD_ID)
public final class ModCorruptionEvents {
    private ModCorruptionEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        if (player.hasEffect(ModMobEffects.CORRUPTION)) {
            ModCorruptionMobEffect.applyCorruptionHealthCap(player);

            float allowedHealth = ModCorruptionMobEffect.getAllowedHealth(player);
            if (player.getHealth() > allowedHealth) {
                player.setHealth(allowedHealth);
            }
        } else {
            ModCorruptionMobEffect.removeCorruptionHealthCap(player);
        }
    }
}