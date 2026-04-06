package net.oldmanyounger.shroud.compat.jei;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.oldmanyounger.shroud.ritual.recipe.RitualRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Converts ritual runtime recipe models into JEI display models.
 *
 * <p>This mapper expands ritual requirements into a presentation-oriented payload,
 * including item slots and readable mob requirement lines for JEI categories.
 *
 * <p>In the broader context of the project, this class keeps compatibility rendering
 * logic separate from gameplay transaction logic.
 */
public final class JeiRitualDisplayMapper {

    // Utility class constructor
    private JeiRitualDisplayMapper() {

    }

    // Maps one ritual recipe into one JEI display payload
    public static JeiRitualDisplay map(RitualRecipe recipe) {
        List<ItemStack> itemInputs = mapItemInputs(recipe);
        List<String> mobLines = mapMobRequirementLines(recipe);

        return new JeiRitualDisplay(
                recipe.id(),
                itemInputs,
                mobLines,
                recipe.mobDamagePerRequiredMob(),
                recipe.output().copy()
        );
    }

    // Expands recipe item requirements into displayed item stacks
    private static List<ItemStack> mapItemInputs(RitualRecipe recipe) {
        List<ItemStack> inputs = new ArrayList<>();

        for (RitualRecipe.ItemRequirement requirement : recipe.itemRequirements()) {
            int count = Math.max(1, requirement.count());

            if (requirement.isItemRequirement() && requirement.item() != null) {
                inputs.add(new ItemStack(requirement.item(), count));
                continue;
            }

            if (requirement.isTagRequirement() && requirement.tag() != null) {
                Optional<Item> firstTagItem = BuiltInRegistries.ITEM
                        .getTag(requirement.tag())
                        .flatMap(named -> named.stream().findFirst())
                        .map(holder -> holder.value());

                firstTagItem.ifPresent(item -> inputs.add(new ItemStack(item, count)));
            }
        }

        return List.copyOf(inputs);
    }

    // Builds human-readable mob requirement lines
    private static List<String> mapMobRequirementLines(RitualRecipe recipe) {
        List<String> lines = new ArrayList<>();

        for (RitualRecipe.MobRequirement requirement : recipe.mobRequirements()) {
            int count = Math.max(1, requirement.count());

            String mobName = requirement.entityType().getDescription().getString();
            if (mobName == null || mobName.isBlank()) {
                mobName = BuiltInRegistries.ENTITY_TYPE.getKey(requirement.entityType()).toString();
            }

            lines.add(count + "x " + mobName);
        }

        return List.copyOf(lines);
    }
}