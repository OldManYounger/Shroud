package net.oldmanyounger.shroud.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Defines the custom item behavior for the Totem of Last Breath.
 *
 * <p>This item always renders with an enchanted foil glint and appends a themed
 * localized tooltip line to communicate its special functionality to players.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * custom item identity layer that reinforces progression and tone through
 * bespoke visual and tooltip presentation behavior.
 */
public class ModTotemOfLastBreathItem extends Item {

    // Creates the item with provided item properties
    public ModTotemOfLastBreathItem(Properties properties) {
        super(properties);
    }

    // Forces enchanted foil rendering for the item
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    // Appends the localized flavor tooltip line
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.shroud.totem_of_last_breath.desc").withStyle(ChatFormatting.DARK_PURPLE));
    }
}