package net.oldmanyounger.shroud.ritual.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.oldmanyounger.shroud.Shroud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Datapack JSON loader for ritual recipes under data/<namespace>/ritual_recipe.
 *
 * <p>This loader parses ritual recipe definitions into immutable runtime recipe objects and
 * stores them in memory for later validation and activation logic.
 *
 * <p>In the broader context of the project, this class is the data-loading backbone for
 * ritual crafting, allowing ritual recipes to be authored and extended through JSON.
 */
public class RitualRecipeManager extends SimpleJsonResourceReloadListener {

    // ==================================
    //  FIELDS
    // ==================================

    // Gson parser used for ritual recipe JSON documents
    private static final Gson GSON = new GsonBuilder().create();

    // Default ritual duration in seconds for backwards compatibility
    private static final int DEFAULT_DURATION_SECONDS = 6;

    // Shared singleton instance used by reload registration and lookup callers
    public static final RitualRecipeManager INSTANCE = new RitualRecipeManager();

    // In-memory map of loaded ritual recipes by id
    private Map<ResourceLocation, RitualRecipe> recipes = Map.of();

    // Creates a JSON reload listener for ritual recipe files
    private RitualRecipeManager() {
        super(GSON, "recipes/ritual");
    }

    // Applies freshly loaded ritual recipe JSON documents
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectMap, net.minecraft.server.packs.resources.ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, RitualRecipe> loaded = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objectMap.entrySet()) {
            ResourceLocation id = entry.getKey();

            try {
                JsonObject root = GsonHelper.convertToJsonObject(entry.getValue(), "ritual_recipe");

                String type = GsonHelper.getAsString(root, "type", "shroud:ritual");
                if (!"shroud:ritual".equals(type)) {
                    throw new IllegalArgumentException("Recipe " + id + " must declare type 'shroud:ritual'");
                }

                List<RitualRecipe.ItemRequirement> itemRequirements = parseItemRequirements(id, root);
                List<RitualRecipe.MobRequirement> mobRequirements = parseMobRequirements(id, root);
                float mobDamage = Math.max(0.0F, GsonHelper.getAsFloat(root, "mob_damage", 2.0F));
                int durationSeconds = Math.max(1, GsonHelper.getAsInt(root, "duration_seconds", DEFAULT_DURATION_SECONDS));
                ItemStack output = parseOutput(id, root);

                RitualRecipe recipe = new RitualRecipe(id, itemRequirements, mobRequirements, mobDamage, durationSeconds, output);
                loaded.put(id, recipe);
            } catch (Exception ex) {
                Shroud.LOGGER.error("Failed to parse ritual recipe {}", id, ex);
            }
        }

        this.recipes = Map.copyOf(loaded);
        Shroud.LOGGER.info("Loaded {} ritual recipes", this.recipes.size());
    }

    // Returns all loaded ritual recipes
    public Collection<RitualRecipe> getAll() {
        return recipes.values();
    }

    // Returns loaded ritual recipe by id if present
    public Optional<RitualRecipe> get(ResourceLocation id) {
        return Optional.ofNullable(recipes.get(id));
    }

    // Parses item requirement list from JSON
    private List<RitualRecipe.ItemRequirement> parseItemRequirements(ResourceLocation id, JsonObject root) {
        List<RitualRecipe.ItemRequirement> out = new ArrayList<>();

        JsonArray items = GsonHelper.getAsJsonArray(root, "items", new JsonArray());
        for (JsonElement element : items) {
            JsonObject req = GsonHelper.convertToJsonObject(element, "item_requirement");

            int count = Math.max(1, GsonHelper.getAsInt(req, "count", 1));

            boolean hasItem = req.has("item");
            boolean hasTag = req.has("tag");

            if (hasItem == hasTag) {
                throw new IllegalArgumentException("Recipe " + id + " item requirement must define exactly one of 'item' or 'tag'");
            }

            if (hasItem) {
                ResourceLocation itemId = ResourceLocation.parse(GsonHelper.getAsString(req, "item"));
                Item item = BuiltInRegistries.ITEM.get(itemId);
                if (item == null || item == net.minecraft.world.item.Items.AIR) {
                    throw new IllegalArgumentException("Recipe " + id + " references unknown item " + itemId);
                }

                out.add(new RitualRecipe.ItemRequirement(item, null, count));
            } else {
                ResourceLocation tagId = ResourceLocation.parse(GsonHelper.getAsString(req, "tag"));
                TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
                out.add(new RitualRecipe.ItemRequirement(null, tag, count));
            }
        }

        return List.copyOf(out);
    }

    // Parses mob requirement list from JSON
    private List<RitualRecipe.MobRequirement> parseMobRequirements(ResourceLocation id, JsonObject root) {
        List<RitualRecipe.MobRequirement> out = new ArrayList<>();

        JsonArray mobs = GsonHelper.getAsJsonArray(root, "mobs", new JsonArray());
        for (JsonElement element : mobs) {
            JsonObject req = GsonHelper.convertToJsonObject(element, "mob_requirement");

            ResourceLocation entityId = ResourceLocation.parse(GsonHelper.getAsString(req, "entity"));
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
            if (entityType == null) {
                throw new IllegalArgumentException("Recipe " + id + " references unknown entity type " + entityId);
            }

            int count = Math.max(1, GsonHelper.getAsInt(req, "count", 1));
            out.add(new RitualRecipe.MobRequirement(entityType, count));
        }

        return List.copyOf(out);
    }

    // Parses ritual output item stack from JSON
    private ItemStack parseOutput(ResourceLocation id, JsonObject root) {
        JsonObject outputObj = GsonHelper.getAsJsonObject(root, "output");

        ResourceLocation outputId = ResourceLocation.parse(GsonHelper.getAsString(outputObj, "item"));
        Item outputItem = BuiltInRegistries.ITEM.get(outputId);
        if (outputItem == null || outputItem == net.minecraft.world.item.Items.AIR) {
            throw new IllegalArgumentException("Recipe " + id + " references unknown output item " + outputId);
        }

        int count = Math.max(1, GsonHelper.getAsInt(outputObj, "count", 1));
        return new ItemStack(outputItem, count);
    }
}