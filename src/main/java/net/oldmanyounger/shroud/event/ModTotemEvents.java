package net.oldmanyounger.shroud.event;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.effect.ModCorruptionMobEffect;
import net.oldmanyounger.shroud.effect.ModMobEffects;
import net.oldmanyounger.shroud.item.ModItems;
import net.oldmanyounger.shroud.network.payload.ShowLastBreathActivationPayload;

/**
 * Handles activation behavior for the Totem of Last Breath on fatal damage.
 *
 * <p>This event handler intercepts server player death, consumes the custom totem,
 * revives the player with configured status effects, applies Corruption health
 * constraints, and broadcasts visual/audio activation feedback.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * survival-mechanics layer that replaces default death outcomes with custom
 * high-impact revival and corruption gameplay loops.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public final class ModTotemEvents {

    // Prevents instantiation of this static event handler class
    private ModTotemEvents() {
    }

    // Intercepts fatal damage and triggers Last Breath revival flow when available
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        InteractionHand hand = getTotemHand(player);
        if (hand == null) {
            return;
        }

        ItemStack stack = player.getItemInHand(hand);
        ItemStack usedStack = stack.copy();

        event.setCanceled(true);

        if (!player.hasInfiniteMaterials()) {
            stack.shrink(1);
        }

        revivePlayer(player);
        player.awardStat(Stats.ITEM_USED.get(ModItems.TOTEM_OF_LAST_BREATH.get()));
        CriteriaTriggers.USED_TOTEM.trigger(player, usedStack);

        Level level = player.level();

        PacketDistributor.sendToPlayer(player, new ShowLastBreathActivationPayload());

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(),
                    player.getY() + 1.0D,
                    player.getZ(),
                    30,
                    0.5D,
                    0.8D,
                    0.5D,
                    0.1D
            );
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, player.getSoundSource(), 1.0F, 1.0F);
        player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
    }

    // Applies revival cleanup and post-activation effect package
    private static void revivePlayer(ServerPlayer player) {
        player.removeAllEffects();
        player.setAbsorptionAmount(0.0F);
        player.fallDistance = 0.0F;

        player.addEffect(new MobEffectInstance(ModMobEffects.CORRUPTION, 600, 0, false, true, true));
        ModCorruptionMobEffect.applyCorruptionHealthCap(player);
        player.setHealth(ModCorruptionMobEffect.getAllowedHealth(player));

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 900, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 4));
    }

    // Returns the hand containing Totem of Last Breath or null if none is found
    private static InteractionHand getTotemHand(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (player.getItemInHand(hand).is(ModItems.TOTEM_OF_LAST_BREATH.get())) {
                return hand;
            }
        }
        return null;
    }
}