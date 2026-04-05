package net.oldmanyounger.shroud.ritual.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Defines one corrupted reliquary ritual recipe in a data-oriented form.
 *
 * <p>This model captures altar item requirements, anchor mob requirements,
 * per-anchor health drain values, and output item data without tying directly
 * to block entities or networking concerns.
 *
 * <p>In the broader context of the project, this class is part of the ritual
 * crafting domain layer that provides a stable contract for recipe loading,
 * validation, and execution logic.
 */
public final class CorruptedReliquaryRecipeDefinition {

    // ==================================
    //  FIELDS
    // ==================================

    // Unique recipe id
    private final ResourceLocation id;

    // Required altar item inputs
    private final List<ItemRequirement> altarInputs;

    // Required anchor mob inputs in north east south west order
    private final List<MobRequirement> anchorRequirements;

    // Output item id
    private final ResourceLocation resultItemId;

    // Output stack count
    private final int resultCount;

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Creates a recipe definition and validates core invariants
    public CorruptedReliquaryRecipeDefinition(
            ResourceLocation id,
            List<ItemRequirement> altarInputs,
            List<MobRequirement> anchorRequirements,
            ResourceLocation resultItemId,
            int resultCount
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.altarInputs = List.copyOf(Objects.requireNonNull(altarInputs, "altarInputs"));
        this.anchorRequirements = List.copyOf(Objects.requireNonNull(anchorRequirements, "anchorRequirements"));
        this.resultItemId = Objects.requireNonNull(resultItemId, "resultItemId");
        this.resultCount = resultCount;

        if (this.altarInputs.isEmpty()) {
            throw new IllegalArgumentException("altarInputs must contain at least one entry");
        }

        if (this.anchorRequirements.size() != 4) {
            throw new IllegalArgumentException("anchorRequirements must contain exactly 4 entries");
        }

        if (this.resultCount <= 0) {
            throw new IllegalArgumentException("resultCount must be > 0");
        }
    }

    // ==================================
    //  ACCESSORS
    // ==================================

    // Returns recipe id
    public ResourceLocation id() {
        return id;
    }

    // Returns immutable altar input requirements
    public List<ItemRequirement> altarInputs() {
        return altarInputs;
    }

    // Returns immutable anchor requirements in north east south west order
    public List<MobRequirement> anchorRequirements() {
        return anchorRequirements;
    }

    // Returns output item id
    public ResourceLocation resultItemId() {
        return resultItemId;
    }

    // Returns output count
    public int resultCount() {
        return resultCount;
    }

    // ==================================
    //  MATCH HELPERS
    // ==================================

    // Returns true when provided anchor entity types satisfy all directional requirements
    public boolean matchesAnchors(
            EntityType<?> north,
            EntityType<?> east,
            EntityType<?> south,
            EntityType<?> west
    ) {
        return anchorRequirements.get(0).matches(north)
                && anchorRequirements.get(1).matches(east)
                && anchorRequirements.get(2).matches(south)
                && anchorRequirements.get(3).matches(west);
    }

    // Builds output stack from registered item id
    public ItemStack buildResultStack() {
        var item = BuiltInRegistries.ITEM.get(this.resultItemId);
        return new ItemStack(item, this.resultCount);
    }

    // ==================================
    //  NESTED TYPES
    // ==================================

    // One required altar item and count
    public record ItemRequirement(ResourceLocation itemId, int count) {

        // Creates an item requirement with validation
        public ItemRequirement {
            Objects.requireNonNull(itemId, "itemId");
            if (count <= 0) {
                throw new IllegalArgumentException("count must be > 0");
            }
        }
    }

    // One required anchor mob condition plus health drain
    public static final class MobRequirement {

        // Exact required entity id or null when using tag requirement
        private final @Nullable ResourceLocation entityId;

        // Required entity type tag or null when using exact entity id
        private final @Nullable TagKey<EntityType<?>> entityTag;

        // Health drained from matched anchor mob
        private final float healthDrain;

        // Creates an exact-id mob requirement
        public static MobRequirement exact(ResourceLocation entityId, float healthDrain) {
            return new MobRequirement(entityId, null, healthDrain);
        }

        // Creates a tag-based mob requirement
        public static MobRequirement tag(TagKey<EntityType<?>> entityTag, float healthDrain) {
            return new MobRequirement(null, entityTag, healthDrain);
        }

        // Creates a mob requirement with validation
        private MobRequirement(
                @Nullable ResourceLocation entityId,
                @Nullable TagKey<EntityType<?>> entityTag,
                float healthDrain
        ) {
            this.entityId = entityId;
            this.entityTag = entityTag;
            this.healthDrain = healthDrain;

            if ((entityId == null) == (entityTag == null)) {
                throw new IllegalArgumentException("exactly one of entityId or entityTag must be set");
            }

            if (healthDrain <= 0.0F) {
                throw new IllegalArgumentException("healthDrain must be > 0");
            }
        }

        // Returns true when this requirement accepts the provided entity type
        public boolean matches(EntityType<?> type) {
            if (this.entityId != null) {
                ResourceLocation actual = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                return this.entityId.equals(actual);
            }

            return type.is(this.entityTag);
        }

        // Returns optional exact entity id
        public @Nullable ResourceLocation entityId() {
            return entityId;
        }

        // Returns optional entity tag
        public @Nullable TagKey<EntityType<?>> entityTag() {
            return entityTag;
        }

        // Returns configured health drain
        public float healthDrain() {
            return healthDrain;
        }
    }

    // ==================================
    //  BUILDERS
    // ==================================

    // Creates a simple exact-mob recipe with directional requirements
    public static CorruptedReliquaryRecipeDefinition ofExact(
            ResourceLocation id,
            List<ItemRequirement> altarInputs,
            ResourceLocation northMob,
            float northDrain,
            ResourceLocation eastMob,
            float eastDrain,
            ResourceLocation southMob,
            float southDrain,
            ResourceLocation westMob,
            float westDrain,
            ResourceLocation resultItemId,
            int resultCount
    ) {
        List<MobRequirement> requirements = new ArrayList<>(4);
        requirements.add(MobRequirement.exact(northMob, northDrain));
        requirements.add(MobRequirement.exact(eastMob, eastDrain));
        requirements.add(MobRequirement.exact(southMob, southDrain));
        requirements.add(MobRequirement.exact(westMob, westDrain));

        return new CorruptedReliquaryRecipeDefinition(
                id,
                altarInputs,
                requirements,
                resultItemId,
                resultCount
        );
    }
}