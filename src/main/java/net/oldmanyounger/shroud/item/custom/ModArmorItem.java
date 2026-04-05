package net.oldmanyounger.shroud.item.custom;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.oldmanyounger.shroud.item.ModArmorMaterials;

import java.util.List;
import java.util.Map;

/**
 * Defines custom armor item behavior for full-set bonus effects.
 *
 * <p>This armor item checks player equipment state on a timed tick cadence and
 * applies configured potion effects when a full matching armor set is equipped.
 *
 * <p>In the broader context of the project, this class is part of Shroud's item
 * progression layer that gives custom armor materials identity through set-based
 * gameplay bonuses.
 */
public class ModArmorItem extends ArmorItem {

    // Maps armor materials to effect lists granted when a full set is worn
    private static final Map<Holder<ArmorMaterial>, List<MobEffectInstance>> MATERIAL_TO_EFFECT_MAP =
            (new ImmutableMap.Builder<Holder<ArmorMaterial>, List<MobEffectInstance>>())
                    .put(ModArmorMaterials.EVENTIDE_ARMOR_MATERIAL,
                            List.of(
                                    // Eventide full-set effects
                                    new MobEffectInstance(MobEffects.JUMP, 200, 1, false, false),
                                    new MobEffectInstance(MobEffects.GLOWING, 200, 1, false, false)
                            ))
                    .build();

    // Creates a custom armor item with the provided material, type, and properties
    public ModArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    // Evaluates armor-set effects on server-side player inventory ticks
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player) || level.isClientSide()) {
            return;
        }

        // Runs once per second
        if (player.tickCount % 20 != 0) {
            return;
        }

        if (hasFullSuitOfArmorOn(player)) {
            evaluateArmorEffects(player);
        }
    }

    // Applies effect groups for any matching full armor material set
    private void evaluateArmorEffects(Player player) {
        for (Map.Entry<Holder<ArmorMaterial>, List<MobEffectInstance>> entry : MATERIAL_TO_EFFECT_MAP.entrySet()) {
            Holder<ArmorMaterial> mapArmorMaterial = entry.getKey();
            List<MobEffectInstance> mapEffect = entry.getValue();

            if (hasPlayerCorrectArmorOn(mapArmorMaterial, player)) {
                addEffectToPlayer(player, mapEffect);
            }
        }
    }

    // Applies missing effects from the configured list to the player
    private void addEffectToPlayer(Player player, List<MobEffectInstance> mapEffect) {
        // Checks whether all effects are already active
        boolean hasPlayerEffect = mapEffect.stream().allMatch(effect -> player.hasEffect(effect.getEffect()));

        // Reapplies only when one or more effects are absent
        if (!hasPlayerEffect) {
            for (MobEffectInstance effect : mapEffect) {
                // Copies each effect into a new instance before applying
                player.addEffect(new MobEffectInstance(
                        effect.getEffect(),
                        effect.getDuration(),
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.isVisible()
                ));
            }
        }
    }

    // Verifies all armor slots contain armor pieces of the requested material
    private boolean hasPlayerCorrectArmorOn(Holder<ArmorMaterial> mapArmorMaterial, Player player) {
        // Ensures every equipped armor slot is an ArmorItem
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (!(armorStack.getItem() instanceof ArmorItem)) {
                return false;
            }
        }

        // Reads armor slots in inventory order 0..3 as boots to helmet
        ArmorItem boots = ((ArmorItem) player.getInventory().getArmor(0).getItem());
        ArmorItem leggings = ((ArmorItem) player.getInventory().getArmor(1).getItem());
        ArmorItem chestplate = ((ArmorItem) player.getInventory().getArmor(2).getItem());
        ArmorItem helmet = ((ArmorItem) player.getInventory().getArmor(3).getItem());

        // Returns true only when all four pieces share the same material
        return boots.getMaterial() == mapArmorMaterial && leggings.getMaterial() == mapArmorMaterial
                && chestplate.getMaterial() == mapArmorMaterial && helmet.getMaterial() == mapArmorMaterial;
    }

    // Checks whether all four armor slots are occupied
    private boolean hasFullSuitOfArmorOn(Player player) {
        ItemStack boots = player.getInventory().getArmor(0);
        ItemStack leggings = player.getInventory().getArmor(1);
        ItemStack chestplate = player.getInventory().getArmor(2);
        ItemStack helmet = player.getInventory().getArmor(3);

        return !boots.isEmpty() && !leggings.isEmpty() && !chestplate.isEmpty() && !helmet.isEmpty();
    }
}