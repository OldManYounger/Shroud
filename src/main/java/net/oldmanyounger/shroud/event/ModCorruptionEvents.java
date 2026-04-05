package net.oldmanyounger.shroud.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.effect.ModCorruptionMobEffect;
import net.oldmanyounger.shroud.effect.ModMobEffects;

/**
 * Applies per-tick runtime handling for the Corruption effect on players.
 *
 * <p>This event hook enforces Corruption's health cap while the effect is active
 * and removes the temporary cap modifier when the effect is no longer present.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * status-effect enforcement layer that keeps gameplay penalties synchronized
 * with effect state each tick.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public final class ModCorruptionEvents {

    // Prevents instantiation of this static event handler class
    private ModCorruptionEvents() {
    }

    // Applies and maintains corruption health cap behavior on server-side player ticks
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