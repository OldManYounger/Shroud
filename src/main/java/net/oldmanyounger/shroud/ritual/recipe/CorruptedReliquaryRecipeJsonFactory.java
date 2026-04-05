package net.oldmanyounger.shroud.ritual.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Objects;

/**
 * Builds canonical JSON objects for corrupted reliquary ritual recipes.
 *
 * <p>This factory centralizes JSON shape construction so generated base-mod
 * recipes and manually-authored datapack recipes follow the same schema.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * ritual data authoring layer that keeps recipe structure stable across
 * datagen, manual JSON edits, and future KubeJS integrations.
 */
public final class CorruptedReliquaryRecipeJsonFactory {

    // Root key for altar input array
    public static final String KEY_ALTAR_INPUTS = "altar_inputs";

    // Root key for directional anchor requirement array
    public static final String KEY_ANCHORS = "anchors";

    // Root key for result object
    public static final String KEY_RESULT = "result";

    // Field key for item id
    public static final String KEY_ITEM = "item";

    // Field key for count
    public static final String KEY_COUNT = "count";

    // Field key for exact entity id
    public static final String KEY_ENTITY = "entity";

    // Field key for entity tag id
    public static final String KEY_TAG = "tag";

    // Field key for health drain
    public static final String KEY_HEALTH_DRAIN = "health_drain";

    // Prevents instantiation of this static utility class
    private CorruptedReliquaryRecipeJsonFactory() {
    }

    // Builds a full ritual recipe json object from item and anchor specs
    public static JsonObject recipe(
            List<ItemInput> altarInputs,
            List<AnchorInput> anchors,
            ItemOutput result
    ) {
        Objects.requireNonNull(altarInputs, "altarInputs");
        Objects.requireNonNull(anchors, "anchors");
        Objects.requireNonNull(result, "result");

        if (altarInputs.isEmpty()) {
            throw new IllegalArgumentException("altarInputs must not be empty");
        }

        if (anchors.size() != 4) {
            throw new IllegalArgumentException("anchors must contain exactly 4 entries in north east south west order");
        }

        JsonObject root = new JsonObject();
        root.add(KEY_ALTAR_INPUTS, toAltarArray(altarInputs));
        root.add(KEY_ANCHORS, toAnchorArray(anchors));
        root.add(KEY_RESULT, toResultObject(result));
        return root;
    }

    // Builds a recipe json object for exact-entity anchor requirements
    public static JsonObject exactEntityRecipe(
            List<ItemInput> altarInputs,
            ResourceLocation northEntity,
            float northDrain,
            ResourceLocation eastEntity,
            float eastDrain,
            ResourceLocation southEntity,
            float southDrain,
            ResourceLocation westEntity,
            float westDrain,
            ItemOutput result
    ) {
        return recipe(
                altarInputs,
                List.of(
                        AnchorInput.exactEntity(northEntity, northDrain),
                        AnchorInput.exactEntity(eastEntity, eastDrain),
                        AnchorInput.exactEntity(southEntity, southDrain),
                        AnchorInput.exactEntity(westEntity, westDrain)
                ),
                result
        );
    }

    // Converts altar item specs into json array
    private static JsonArray toAltarArray(List<ItemInput> altarInputs) {
        JsonArray arr = new JsonArray();

        for (ItemInput input : altarInputs) {
            JsonObject obj = new JsonObject();
            obj.addProperty(KEY_ITEM, input.itemId().toString());
            obj.addProperty(KEY_COUNT, input.count());
            arr.add(obj);
        }

        return arr;
    }

    // Converts directional anchor specs into json array
    private static JsonArray toAnchorArray(List<AnchorInput> anchors) {
        JsonArray arr = new JsonArray();

        for (AnchorInput input : anchors) {
            JsonObject obj = new JsonObject();

            if (input.entityId() != null) {
                obj.addProperty(KEY_ENTITY, input.entityId().toString());
            } else if (input.entityTag() != null) {
                obj.addProperty(KEY_TAG, "#" + input.entityTag().location());
            } else {
                throw new IllegalStateException("anchor input must have either entity id or tag");
            }

            obj.addProperty(KEY_HEALTH_DRAIN, input.healthDrain());
            arr.add(obj);
        }

        return arr;
    }

    // Converts result spec into json object
    private static JsonObject toResultObject(ItemOutput result) {
        JsonObject obj = new JsonObject();
        obj.addProperty(KEY_ITEM, result.itemId().toString());
        obj.addProperty(KEY_COUNT, result.count());
        return obj;
    }

    // One altar item input spec
    public record ItemInput(ResourceLocation itemId, int count) {
        // Validates altar input values
        public ItemInput {
            Objects.requireNonNull(itemId, "itemId");
            if (count <= 0) {
                throw new IllegalArgumentException("count must be > 0");
            }
        }

        // Convenience factory for one-count input
        public static ItemInput of(ResourceLocation itemId) {
            return new ItemInput(itemId, 1);
        }
    }

    // One anchor mob requirement spec
    public record AnchorInput(ResourceLocation entityId, TagKey<EntityType<?>> entityTag, float healthDrain) {
        // Validates anchor input values
        public AnchorInput {
            if ((entityId == null) == (entityTag == null)) {
                throw new IllegalArgumentException("exactly one of entityId or entityTag must be set");
            }
            if (healthDrain <= 0.0F) {
                throw new IllegalArgumentException("healthDrain must be > 0");
            }
        }

        // Creates exact entity id anchor input
        public static AnchorInput exactEntity(ResourceLocation entityId, float healthDrain) {
            return new AnchorInput(entityId, null, healthDrain);
        }

        // Creates tag-based anchor input
        public static AnchorInput entityTag(TagKey<EntityType<?>> entityTag, float healthDrain) {
            return new AnchorInput(null, entityTag, healthDrain);
        }
    }

    // Ritual output item spec
    public record ItemOutput(ResourceLocation itemId, int count) {
        // Validates output values
        public ItemOutput {
            Objects.requireNonNull(itemId, "itemId");
            if (count <= 0) {
                throw new IllegalArgumentException("count must be > 0");
            }
        }

        // Convenience factory for one-count result
        public static ItemOutput of(ResourceLocation itemId) {
            return new ItemOutput(itemId, 1);
        }
    }
}