package net.oldmanyounger.shroud.item.custom;

import com.google.common.collect.ImmutableMap;
import net.oldmanyounger.shroud.item.ModArmorMaterials;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

// Custom armor item that grants special effects when a full matching set is worn
// Credit to KaupenJoe for the majority of the class
public class ModArmorItem extends ArmorItem {

    // Maps armor materials to the list of potion effects they should grant when a full set is equipped
    private static final Map<Holder<ArmorMaterial>, List<MobEffectInstance>> MATERIAL_TO_EFFECT_MAP =
            (new ImmutableMap.Builder<Holder<ArmorMaterial>, List<MobEffectInstance>>())
                    .put(ModArmorMaterials.EVENTIDE_ARMOR_MATERIAL,
                            List.of(
                                    // Jump-boost and glowing for wearing full Eventide armor
                                    new MobEffectInstance(MobEffects.JUMP, 200, 1, false, false),
                                    new MobEffectInstance(MobEffects.GLOWING, 200, 1, false, false)
                            ))
                    .build();

    // Standard armor item constructor delegating to ArmorItem
    public ModArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    // Called every tick while the armor piece is in a player inventory slot
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player) || level.isClientSide()) {
            return;
        }

        // Run every 20 ticks (1 second)
        if (player.tickCount % 20 != 0) {
            return;
        }

        if (hasFullSuitOfArmorOn(player)) {
            evaluateArmorEffects(player);
        }
    }

    // Checks the player's armor material against the map and applies the corresponding effects
    private void evaluateArmorEffects(Player player) {
        for(Map.Entry<Holder<ArmorMaterial>, List<MobEffectInstance>> entry : MATERIAL_TO_EFFECT_MAP.entrySet()) {
            Holder<ArmorMaterial> mapArmorMaterial = entry.getKey();
            List<MobEffectInstance> mapEffect = entry.getValue();

            // If the player is wearing a full set of this material, grant its configured effects
            if(hasPlayerCorrectArmorOn(mapArmorMaterial, player)) {
                addEffectToPlayer(player, mapEffect);
            }
        }
    }

    // Adds all configured effects to the player if they do not already have them
    private void addEffectToPlayer(Player player, List<MobEffectInstance> mapEffect) {
        // Check if the player already has every effect in the list
        boolean hasPlayerEffect = mapEffect.stream().allMatch(effect -> player.hasEffect(effect.getEffect()));

        // Only re-apply effects if one or more are missing
        if(!hasPlayerEffect) {
            for (MobEffectInstance effect : mapEffect) {
                // Create new instances to avoid mutating shared MobEffectInstance objects
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

    // Verifies that every armor slot is filled with an ArmorItem of the given material
    private boolean hasPlayerCorrectArmorOn(Holder<ArmorMaterial> mapArmorMaterial, Player player) {
        // First ensure all armor slots actually contain an ArmorItem
        for(ItemStack armorStack : player.getArmorSlots()) {
            if(!(armorStack.getItem() instanceof ArmorItem)) {
                return false;
            }
        }

        // Extract each armor piece as an ArmorItem from the inventory armor slots (0–3: boots → helmet)
        ArmorItem boots = ((ArmorItem) player.getInventory().getArmor(0).getItem());
        ArmorItem leggings = ((ArmorItem) player.getInventory().getArmor(1).getItem());
        ArmorItem chestplate = ((ArmorItem) player.getInventory().getArmor(2).getItem());
        ArmorItem helmet = ((ArmorItem) player.getInventory().getArmor(3).getItem());

        // True only if all four pieces share the same armor material as the map key
        return boots.getMaterial() == mapArmorMaterial && leggings.getMaterial() == mapArmorMaterial
                && chestplate.getMaterial() == mapArmorMaterial && helmet.getMaterial() == mapArmorMaterial;
    }

    // Simple check that all four armor slots are non-empty (does not validate material)
    private boolean hasFullSuitOfArmorOn(Player player) {
        ItemStack boots = player.getInventory().getArmor(0);
        ItemStack leggings = player.getInventory().getArmor(1);
        ItemStack chestplate = player.getInventory().getArmor(2);
        ItemStack helmet = player.getInventory().getArmor(3);

        // Returns true if all armor slots are occupied by some item
        return !boots.isEmpty() && !leggings.isEmpty() && !chestplate.isEmpty() && !helmet.isEmpty();
    }
}
