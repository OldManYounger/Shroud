package net.oldmanyounger.shroud.ritual.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses corrupted reliquary ritual recipes from JSON objects.
 *
 * <p>This parser converts data-pack style JSON into validated
 * {@link CorruptedReliquaryRecipeDefinition} instances, including altar item
 * requirements, directional anchor mob requirements, and ritual result output.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * ritual data-loading layer that enables content iteration through JSON-driven
 * recipe authoring.
 */
public final class CorruptedReliquaryRecipeJson {

    // JSON key for altar item requirement array
    private static final String KEY_ALTAR_INPUTS = "altar_inputs";

    // JSON key for directional anchor requirement array
    private static final String KEY_ANCHORS = "anchors";

    // JSON key for ritual result object
    private static final String KEY_RESULT = "result";

    // JSON key for item id field
    private static final String KEY_ITEM = "item";

    // JSON key for item count field
    private static final String KEY_COUNT = "count";

    // JSON key for exact entity id field
    private static final String KEY_ENTITY = "entity";

    // JSON key for entity tag field
    private static final String KEY_TAG = "tag";

    // JSON key for per-anchor health drain field
    private static final String KEY_HEALTH_DRAIN = "health_drain";

    // Prevents instantiation of this static utility parser
    private CorruptedReliquaryRecipeJson() {
    }

    // Parses one ritual recipe definition from json
    public static CorruptedReliquaryRecipeDefinition parse(ResourceLocation recipeId, JsonObject json) {
        List<CorruptedReliquaryRecipeDefinition.ItemRequirement> altarInputs = parseAltarInputs(json);
        List<CorruptedReliquaryRecipeDefinition.MobRequirement> anchors = parseAnchors(json);
        ResultData result = parseResult(json);

        return new CorruptedReliquaryRecipeDefinition(
                recipeId,
                altarInputs,
                anchors,
                result.itemId(),
                result.count()
        );
    }

    // Parses altar input requirement array
    private static List<CorruptedReliquaryRecipeDefinition.ItemRequirement> parseAltarInputs(JsonObject json) {
        JsonArray arr = GsonHelper.getAsJsonArray(json, KEY_ALTAR_INPUTS);
        if (arr.isEmpty()) {
            throw new IllegalArgumentException(KEY_ALTAR_INPUTS + " must not be empty");
        }

        List<CorruptedReliquaryRecipeDefinition.ItemRequirement> out = new ArrayList<>(arr.size());

        for (JsonElement element : arr) {
            JsonObject obj = GsonHelper.convertToJsonObject(element, KEY_ALTAR_INPUTS + "[]");

            String itemRaw = GsonHelper.getAsString(obj, KEY_ITEM);
            ResourceLocation itemId = ResourceLocation.parse(itemRaw);

            int count = GsonHelper.getAsInt(obj, KEY_COUNT, 1);
            out.add(new CorruptedReliquaryRecipeDefinition.ItemRequirement(itemId, count));
        }

        return out;
    }

    // Parses directional anchor requirements in north east south west order
    private static List<CorruptedReliquaryRecipeDefinition.MobRequirement> parseAnchors(JsonObject json) {
        JsonArray arr = GsonHelper.getAsJsonArray(json, KEY_ANCHORS);

        if (arr.size() != 4) {
            throw new IllegalArgumentException(KEY_ANCHORS + " must contain exactly 4 entries in north east south west order");
        }

        List<CorruptedReliquaryRecipeDefinition.MobRequirement> out = new ArrayList<>(4);

        for (JsonElement element : arr) {
            JsonObject obj = GsonHelper.convertToJsonObject(element, KEY_ANCHORS + "[]");

            float healthDrain = GsonHelper.getAsFloat(obj, KEY_HEALTH_DRAIN);

            boolean hasEntity = obj.has(KEY_ENTITY);
            boolean hasTag = obj.has(KEY_TAG);

            if (hasEntity == hasTag) {
                throw new IllegalArgumentException("each anchor entry must define exactly one of '" + KEY_ENTITY + "' or '" + KEY_TAG + "'");
            }

            if (hasEntity) {
                String entityRaw = GsonHelper.getAsString(obj, KEY_ENTITY);
                ResourceLocation entityId = ResourceLocation.parse(entityRaw);
                out.add(CorruptedReliquaryRecipeDefinition.MobRequirement.exact(entityId, healthDrain));
            } else {
                String tagRaw = GsonHelper.getAsString(obj, KEY_TAG);
                TagKey<EntityType<?>> entityTag = parseEntityTag(tagRaw);
                out.add(CorruptedReliquaryRecipeDefinition.MobRequirement.tag(entityTag, healthDrain));
            }
        }

        return out;
    }

    // Parses result object containing output item and optional count
    private static ResultData parseResult(JsonObject json) {
        JsonObject resultObj = GsonHelper.getAsJsonObject(json, KEY_RESULT);

        String itemRaw = GsonHelper.getAsString(resultObj, KEY_ITEM);
        ResourceLocation itemId = ResourceLocation.parse(itemRaw);

        int count = GsonHelper.getAsInt(resultObj, KEY_COUNT, 1);
        if (count <= 0) {
            throw new IllegalArgumentException(KEY_RESULT + "." + KEY_COUNT + " must be > 0");
        }

        return new ResultData(itemId, count);
    }

    // Parses entity tag reference string into an entity type tag key
    private static TagKey<EntityType<?>> parseEntityTag(String raw) {
        String trimmed = raw.trim();
        String normalized = trimmed.startsWith("#") ? trimmed.substring(1) : trimmed;

        ResourceLocation id = ResourceLocation.parse(normalized);
        return TagKey.create(Registries.ENTITY_TYPE, id);
    }

    // Parsed output payload holder
    private record ResultData(ResourceLocation itemId, int count) {
    }
}