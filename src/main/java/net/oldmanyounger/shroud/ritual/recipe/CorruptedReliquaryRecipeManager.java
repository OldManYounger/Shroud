package net.oldmanyounger.shroud.ritual.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.oldmanyounger.shroud.Shroud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads and stores corrupted reliquary recipes from datapack JSON resources.
 *
 * <p>This manager reads recipe files from a dedicated resource folder, parses
 * them into {@link CorruptedReliquaryRecipeDefinition} instances, and exposes
 * lookup and match helpers for ritual runtime systems.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * data-driven ritual infrastructure that enables external content tools such as
 * KubeJS and datapacks to define additional ritual recipes cleanly.
 */
public final class CorruptedReliquaryRecipeManager extends SimpleJsonResourceReloadListener {

    // ==================================
    //  FIELDS
    // ==================================

    // Datapack folder containing ritual recipe json definitions
    public static final String RESOURCE_FOLDER = "shroud_reliquary_recipes";

    // Singleton manager instance
    public static final CorruptedReliquaryRecipeManager INSTANCE = new CorruptedReliquaryRecipeManager();

    // Current loaded recipe map keyed by full recipe id
    private final Map<ResourceLocation, CorruptedReliquaryRecipeDefinition> recipesById = new LinkedHashMap<>();

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Creates recipe manager bound to ritual resource folder
    private CorruptedReliquaryRecipeManager() {
        super(Shroud.GSON, RESOURCE_FOLDER);
    }

    // ==================================
    //  RELOAD
    // ==================================

    // Rebuilds in-memory recipe map from loaded json elements
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, net.minecraft.server.packs.resources.ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, CorruptedReliquaryRecipeDefinition> rebuilt = new LinkedHashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
            ResourceLocation recipeId = entry.getKey();
            JsonElement raw = entry.getValue();

            try {
                JsonObject obj = raw.getAsJsonObject();
                CorruptedReliquaryRecipeDefinition parsed = CorruptedReliquaryRecipeJson.parse(recipeId, obj);
                rebuilt.put(recipeId, parsed);
            } catch (Exception ex) {
                Shroud.LOGGER.error("Failed to parse corrupted reliquary recipe '{}': {}", recipeId, ex.getMessage());
            }
        }

        recipesById.clear();
        recipesById.putAll(rebuilt);

        Shroud.LOGGER.info("Loaded {} corrupted reliquary recipes", recipesById.size());
    }

    // ==================================
    //  LOOKUPS
    // ==================================

    // Returns immutable collection view of all loaded recipes
    public Collection<CorruptedReliquaryRecipeDefinition> all() {
        return Collections.unmodifiableCollection(recipesById.values());
    }

    // Returns optional recipe by exact id
    public Optional<CorruptedReliquaryRecipeDefinition> byId(ResourceLocation id) {
        return Optional.ofNullable(recipesById.get(id));
    }

    // Finds first recipe that matches altar items and anchor mob requirements
    public Optional<CorruptedReliquaryRecipeDefinition> findMatch(CorruptedReliquaryMatchContext context) {
        for (CorruptedReliquaryRecipeDefinition recipe : recipesById.values()) {
            if (context.matches(recipe)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    // Finds first recipe that matches and also has sufficient anchor health for drain
    public Optional<CorruptedReliquaryRecipeDefinition> findExecutableMatch(CorruptedReliquaryMatchContext context) {
        for (CorruptedReliquaryRecipeDefinition recipe : recipesById.values()) {
            if (context.matches(recipe) && context.hasSufficientAnchorHealth(recipe)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    // Returns deterministic list copy of loaded recipes in load order
    public java.util.List<CorruptedReliquaryRecipeDefinition> asList() {
        return new ArrayList<>(recipesById.values());
    }
}