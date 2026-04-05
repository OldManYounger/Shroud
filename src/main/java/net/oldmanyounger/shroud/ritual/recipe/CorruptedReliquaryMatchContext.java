package net.oldmanyounger.shroud.ritual.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.oldmanyounger.shroud.ritual.RitualAnchorLayout;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents current reliquary runtime inputs used for recipe matching and validation.
 *
 * <p>This context captures altar item stacks and directional anchor mob snapshots,
 * then provides deterministic checks for item requirements, mob requirements,
 * and health drain sufficiency.
 *
 * <p>In the broader context of the project, this class is part of Shroud's ritual
 * execution layer that isolates matching logic from block entity ticking and
 * interaction code.
 */
public final class CorruptedReliquaryMatchContext {

    // ==================================
    //  FIELDS
    // ==================================

    // Altar input stacks in insertion order
    private final List<ItemStack> altarStacks;

    // Anchor mob snapshots keyed by canonical directional slot
    private final EnumMap<RitualAnchorLayout.AnchorSlot, AnchorMobState> anchorMobs;

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Creates a context with defensive copies of all mutable state
    public CorruptedReliquaryMatchContext(
            List<ItemStack> altarStacks,
            Map<RitualAnchorLayout.AnchorSlot, AnchorMobState> anchorMobs
    ) {
        Objects.requireNonNull(altarStacks, "altarStacks");
        Objects.requireNonNull(anchorMobs, "anchorMobs");

        this.altarStacks = altarStacks.stream().map(ItemStack::copy).toList();
        this.anchorMobs = new EnumMap<>(RitualAnchorLayout.AnchorSlot.class);
        this.anchorMobs.putAll(anchorMobs);

        for (RitualAnchorLayout.AnchorSlot slot : RitualAnchorLayout.ORDERED_SLOTS) {
            if (!this.anchorMobs.containsKey(slot)) {
                throw new IllegalArgumentException("missing anchor slot state for " + slot);
            }
        }
    }

    // ==================================
    //  ACCESSORS
    // ==================================

    // Returns defensive copies of altar stacks
    public List<ItemStack> altarStacks() {
        return altarStacks.stream().map(ItemStack::copy).toList();
    }

    // Returns immutable anchor mob map view
    public Map<RitualAnchorLayout.AnchorSlot, AnchorMobState> anchorMobs() {
        return Map.copyOf(anchorMobs);
    }

    // ==================================
    //  MATCHING
    // ==================================

    // Returns true when both altar item requirements and anchor mob requirements match
    public boolean matches(CorruptedReliquaryRecipeDefinition recipe) {
        return matchesAltarItems(recipe) && matchesAnchors(recipe);
    }

    // Returns true when altar contains all required item counts
    public boolean matchesAltarItems(CorruptedReliquaryRecipeDefinition recipe) {
        Map<ResourceLocation, Integer> availableCounts = countAltarItemsById();

        for (CorruptedReliquaryRecipeDefinition.ItemRequirement req : recipe.altarInputs()) {
            int available = availableCounts.getOrDefault(req.itemId(), 0);
            if (available < req.count()) {
                return false;
            }
        }

        return true;
    }

    // Returns true when all directional anchor mob requirements are satisfied
    public boolean matchesAnchors(CorruptedReliquaryRecipeDefinition recipe) {
        List<CorruptedReliquaryRecipeDefinition.MobRequirement> reqs = recipe.anchorRequirements();

        AnchorMobState north = anchorMobs.get(RitualAnchorLayout.AnchorSlot.NORTH);
        AnchorMobState east = anchorMobs.get(RitualAnchorLayout.AnchorSlot.EAST);
        AnchorMobState south = anchorMobs.get(RitualAnchorLayout.AnchorSlot.SOUTH);
        AnchorMobState west = anchorMobs.get(RitualAnchorLayout.AnchorSlot.WEST);

        return reqs.get(0).matches(north.entityType())
                && reqs.get(1).matches(east.entityType())
                && reqs.get(2).matches(south.entityType())
                && reqs.get(3).matches(west.entityType());
    }

    // Returns true when all anchor mobs have sufficient health for configured drains
    public boolean hasSufficientAnchorHealth(CorruptedReliquaryRecipeDefinition recipe) {
        List<CorruptedReliquaryRecipeDefinition.MobRequirement> reqs = recipe.anchorRequirements();

        return anchorMobs.get(RitualAnchorLayout.AnchorSlot.NORTH).currentHealth() >= reqs.get(0).healthDrain()
                && anchorMobs.get(RitualAnchorLayout.AnchorSlot.EAST).currentHealth() >= reqs.get(1).healthDrain()
                && anchorMobs.get(RitualAnchorLayout.AnchorSlot.SOUTH).currentHealth() >= reqs.get(2).healthDrain()
                && anchorMobs.get(RitualAnchorLayout.AnchorSlot.WEST).currentHealth() >= reqs.get(3).healthDrain();
    }

    // ==================================
    //  HELPERS
    // ==================================

    // Counts altar items by registry id using total stack counts
    private Map<ResourceLocation, Integer> countAltarItemsById() {
        Map<ResourceLocation, Integer> counts = new java.util.HashMap<>();

        for (ItemStack stack : altarStacks) {
            if (stack.isEmpty()) {
                continue;
            }

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            counts.merge(itemId, stack.getCount(), Integer::sum);
        }

        return counts;
    }

    // ==================================
    //  NESTED TYPES
    // ==================================

    // Snapshot of one anchored mob at ritual validation time
    public record AnchorMobState(
            UUID entityUuid,
            net.minecraft.world.entity.EntityType<?> entityType,
            float currentHealth
    ) {
        // Creates validated anchor mob snapshot
        public AnchorMobState {
            Objects.requireNonNull(entityUuid, "entityUuid");
            Objects.requireNonNull(entityType, "entityType");
            if (currentHealth < 0.0F) {
                throw new IllegalArgumentException("currentHealth must be >= 0");
            }
        }
    }

    // ==================================
    //  BUILDERS
    // ==================================

    // Creates context from directional anchor list in canonical order north, east, south, west
    public static CorruptedReliquaryMatchContext ofOrderedAnchors(
            List<ItemStack> altarStacks,
            List<AnchorMobState> orderedAnchorMobs
    ) {
        if (orderedAnchorMobs.size() != 4) {
            throw new IllegalArgumentException("orderedAnchorMobs must contain exactly 4 entries");
        }

        EnumMap<RitualAnchorLayout.AnchorSlot, AnchorMobState> mapped = new EnumMap<>(RitualAnchorLayout.AnchorSlot.class);
        List<RitualAnchorLayout.AnchorSlot> slots = RitualAnchorLayout.ORDERED_SLOTS;

        for (int i = 0; i < 4; i++) {
            mapped.put(slots.get(i), orderedAnchorMobs.get(i));
        }

        return new CorruptedReliquaryMatchContext(altarStacks, mapped);
    }

    // Creates mutable list copy helper for callers collecting altar stacks incrementally
    public static List<ItemStack> mutableAltarList() {
        return new ArrayList<>();
    }
}