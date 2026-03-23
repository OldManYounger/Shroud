package net.oldmanyounger.shroud.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerHeartTypeEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.effect.ModMobEffects;

/**
 * Client-side event handler that swaps the player's heart rendering to the
 * custom corruption heart type whenever the Corruption effect is active.
 *
 * <p>Rather than replacing the entire vanilla health HUD, this handler hooks
 * into the heart-type selection event and tells Minecraft to use Shroud's
 * custom heart sprites for normal heart rendering. That keeps the vanilla
 * layout, absorption handling, blinking behavior, and row logic intact while
 * still changing the visual presentation of the player's health under the
 * Corruption effect.
 *
 * <p>In the broader context of the project, this class is the glue between the
 * gameplay-side Corruption effect and the client-side HUD presentation, allowing
 * the effect to feel integrated with vanilla rendering instead of being a fully
 * custom overlay system.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID, value = Dist.CLIENT)
public final class ModCorruptionHeartRenderHandler {

    // Prevent instantiation because this class is only used as a static event subscriber
    private ModCorruptionHeartRenderHandler() {
    }

    // Replaces the player's default heart type with the custom corruption heart type while corrupted
    @SubscribeEvent
    public static void onPlayerHeartType(PlayerHeartTypeEvent event) {
        // Ignore players who do not currently have the Corruption effect
        if (!event.getEntity().hasEffect(ModMobEffects.CORRUPTION)) {
            return;
        }

        // Swap the heart type so vanilla health rendering uses the corruption sprites
        event.setType(ModCorruptionHeartTypes.corrupted());
    }
}