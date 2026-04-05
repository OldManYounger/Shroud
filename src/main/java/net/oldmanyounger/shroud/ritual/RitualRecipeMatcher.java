package net.oldmanyounger.shroud.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.block.entity.ModBindingPedestalBlockEntity;
import net.oldmanyounger.shroud.ritual.recipe.RitualRecipe;
import net.oldmanyounger.shroud.ritual.recipe.RitualRecipeManager;

import java.util.*;

/**
 * Runtime matcher that selects a ritual recipe from reliquary items and nearby bound pedestals.
 *
 * <p>This service performs unordered item matching with extra-item invalidation, then validates
 * mob requirements against nearby pedestals using one-mob-per-pedestal semantics.
 *
 * <p>In the broader context of the project, this class is the first execution-phase bridge
 * between loaded ritual recipe data and world-state validation.
 */
public final class RitualRecipeMatcher {

    // ==================================
    //  FIELDS
    // ==================================

    // Horizontal pedestal search radius from reliquary
    private static final int PEDESTAL_RADIUS_XZ = 8;

    // Vertical pedestal search range above and below reliquary
    private static final int PEDESTAL_RADIUS_Y = 1;

    // Utility class constructor
    private RitualRecipeMatcher() {

    }

    /**
     * Tries to find the first ritual recipe that matches current reliquary and pedestal state.
     *
     * <p>Matching order follows loaded recipe order from the manager.
     *
     * @param level server world level
     * @param reliquaryPos reliquary block position
     * @param reliquaryItems reliquary internal items
     * @return optional successful ritual match context
     */
    public static Optional<RitualMatchContext> findFirstMatch(Level level, BlockPos reliquaryPos, NonNullList<ItemStack> reliquaryItems) {
        List<ItemStack> normalizedItems = normalizeReliquaryItems(reliquaryItems);
        List<PedestalSnapshot> pedestals = collectNearbyPedestals(level, reliquaryPos);

        for (RitualRecipe recipe : RitualRecipeManager.INSTANCE.getAll()) {
            if (!matchesItems(recipe, normalizedItems)) {
                continue;
            }

            Optional<List<PedestalSelection>> pedestalSelection = matchPedestals(recipe, pedestals);
            if (pedestalSelection.isEmpty()) {
                continue;
            }

            return Optional.of(new RitualMatchContext(recipe, pedestalSelection.get()));
        }

        return Optional.empty();
    }

    // ==================================
    //  ITEM MATCHING
    // ==================================

    // Normalizes reliquary slot data into a one-stack-per-entry list
    private static List<ItemStack> normalizeReliquaryItems(NonNullList<ItemStack> reliquaryItems) {
        List<ItemStack> out = new ArrayList<>();

        for (ItemStack stack : reliquaryItems) {
            if (stack.isEmpty()) continue;

            int count = Math.max(1, stack.getCount());
            for (int i = 0; i < count; i++) {
                out.add(stack.copyWithCount(1));
            }
        }

        return out;
    }

    // Returns true when reliquary items match recipe requirements exactly and without extras
    private static boolean matchesItems(RitualRecipe recipe, List<ItemStack> actualItems) {
        List<RitualRecipe.ItemRequirement> selectors = expandItemRequirements(recipe.itemRequirements());

        // Extra-item invalidation rule
        if (actualItems.size() != selectors.size()) {
            return false;
        }

        // Backtracking assignment to support exact item and tag overlap safely
        boolean[] used = new boolean[actualItems.size()];
        return matchSelectorRecursive(selectors, 0, actualItems, used);
    }

    // Expands counted item requirements into one selector entry per required unit
    private static List<RitualRecipe.ItemRequirement> expandItemRequirements(List<RitualRecipe.ItemRequirement> requirements) {
        List<RitualRecipe.ItemRequirement> out = new ArrayList<>();

        for (RitualRecipe.ItemRequirement req : requirements) {
            int count = Math.max(1, req.count());
            for (int i = 0; i < count; i++) {
                out.add(req);
            }
        }

        return out;
    }

    // Recursive selector-to-item assignment with one-to-one usage constraints
    private static boolean matchSelectorRecursive(List<RitualRecipe.ItemRequirement> selectors,
                                                  int selectorIndex,
                                                  List<ItemStack> actualItems,
                                                  boolean[] used) {
        if (selectorIndex >= selectors.size()) {
            return true;
        }

        RitualRecipe.ItemRequirement selector = selectors.get(selectorIndex);

        for (int i = 0; i < actualItems.size(); i++) {
            if (used[i]) continue;

            ItemStack candidate = actualItems.get(i);
            if (!selector.matches(candidate)) continue;

            used[i] = true;
            if (matchSelectorRecursive(selectors, selectorIndex + 1, actualItems, used)) {
                return true;
            }
            used[i] = false;
        }

        return false;
    }

    // ==================================
    //  PEDESTAL MATCHING
    // ==================================

    // Collects snapshots of nearby bound pedestals
    private static List<PedestalSnapshot> collectNearbyPedestals(Level level, BlockPos reliquaryPos) {
        List<PedestalSnapshot> out = new ArrayList<>();

        BlockPos min = reliquaryPos.offset(-PEDESTAL_RADIUS_XZ, -PEDESTAL_RADIUS_Y, -PEDESTAL_RADIUS_XZ);
        BlockPos max = reliquaryPos.offset(PEDESTAL_RADIUS_XZ, PEDESTAL_RADIUS_Y, PEDESTAL_RADIUS_XZ);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!level.getBlockState(pos).is(ModBlocks.BINDING_PEDESTAL.get())) continue;

            var be = level.getBlockEntity(pos);
            if (!(be instanceof ModBindingPedestalBlockEntity pedestalBe)) continue;
            if (!pedestalBe.hasBoundMob()) continue;

            String mobTypeId = pedestalBe.getBoundMobTypeId();
            if (mobTypeId == null || mobTypeId.isBlank()) continue;

            out.add(new PedestalSnapshot(pos.immutable(), ResourceLocation.parse(mobTypeId)));
        }

        return out;
    }

    // Attempts to satisfy recipe mob requirements using one pedestal per mob instance
    private static Optional<List<PedestalSelection>> matchPedestals(RitualRecipe recipe, List<PedestalSnapshot> pedestals) {
        List<RitualRecipe.MobRequirement> requirements = recipe.mobRequirements();
        List<PedestalSelection> selections = new ArrayList<>();
        Set<BlockPos> usedPedestals = new HashSet<>();

        for (RitualRecipe.MobRequirement req : requirements) {
            ResourceLocation wantedTypeId = Objects.requireNonNull(
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(req.entityType())
            );

            int remaining = Math.max(1, req.count());

            for (PedestalSnapshot pedestal : pedestals) {
                if (remaining <= 0) break;
                if (usedPedestals.contains(pedestal.pos())) continue;
                if (!pedestal.boundMobTypeId().equals(wantedTypeId)) continue;

                usedPedestals.add(pedestal.pos());
                selections.add(new PedestalSelection(pedestal.pos(), wantedTypeId));
                remaining--;
            }

            if (remaining > 0) {
                return Optional.empty();
            }
        }

        return Optional.of(List.copyOf(selections));
    }

    // ==================================
    //  RESULT MODELS
    // ==================================

    /**
     * Successful ritual match context used by future activation execution.
     *
     * <p>This includes the matched recipe and the concrete pedestal selections reserved
     * for satisfying mob requirements.
     *
     * <p>In the broader context of the project, this object becomes the transaction input
     * for ritual locking, consumption, and completion logic.
     */
    public record RitualMatchContext(
            RitualRecipe recipe,
            List<PedestalSelection> selectedPedestals
    ) {

    }

    /**
     * Snapshot of one eligible nearby bound pedestal.
     *
     * <p>In the broader context of the project, this isolates pedestal scan state from
     * live block entity mutation during matching.
     */
    private record PedestalSnapshot(
            BlockPos pos,
            ResourceLocation boundMobTypeId
    ) {

    }

    /**
     * One selected pedestal assignment used to satisfy a mob requirement unit.
     *
     * <p>In the broader context of the project, this enables one-mob-per-pedestal
     * enforcement during ritual execution.
     */
    public record PedestalSelection(
            BlockPos pos,
            ResourceLocation mobTypeId
    ) {

    }
}