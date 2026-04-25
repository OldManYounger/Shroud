package net.oldmanyounger.shroud.ritual.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Serializer-backed ritual recipe model for the vanilla recipe pipeline.
 *
 * <p>This class stores raw ritual fields directly from recipe JSON so KubeJS `event.custom`
 * recipes with `type: "shroud:ritual"` can be loaded through the normal recipe manager path.
 *
 * <p>In the broader context of the project, this class is the compatibility bridge that makes
 * ritual recipes behave like standard modded recipes while still mapping into Shroud's ritual runtime model.
 */
public record RitualRegisteredRecipe(
        List<ItemRequirementData> items,
        List<MobRequirementData> mobs,
        float mobDamagePerRequiredMob,
        int durationSeconds,
        OutputData output
) implements Recipe<RecipeInput> {

    // ==================================
    //  FIELDS
    // ==================================

    // Main json codec for ritual recipe fields
    public static final Codec<RitualRegisteredRecipe> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemRequirementData.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(RitualRegisteredRecipe::items),
                    MobRequirementData.CODEC.listOf().optionalFieldOf("mobs", List.of()).forGetter(RitualRegisteredRecipe::mobs),
                    Codec.FLOAT.optionalFieldOf("mob_damage", 2.0F).forGetter(RitualRegisteredRecipe::mobDamagePerRequiredMob),
                    Codec.INT.optionalFieldOf("duration_seconds", 6).forGetter(RitualRegisteredRecipe::durationSeconds),
                    OutputData.CODEC.fieldOf("output").forGetter(RitualRegisteredRecipe::output)
            ).apply(instance, RitualRegisteredRecipe::new)
    );

    /**
     * One ritual item requirement entry that can be item or tag based.
     *
     * <p>In the broader context of the project, this class preserves JSON-level selector data
     * so recipes authored by external tools can map into runtime requirement matching.
     */
    public record ItemRequirementData(
            Optional<ResourceLocation> item,
            Optional<ResourceLocation> tag,
            int count
    ) {
        public static final Codec<ItemRequirementData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.optionalFieldOf("item").forGetter(ItemRequirementData::item),
                        ResourceLocation.CODEC.optionalFieldOf("tag").forGetter(ItemRequirementData::tag),
                        Codec.INT.optionalFieldOf("count", 1).forGetter(ItemRequirementData::count)
                ).apply(instance, ItemRequirementData::new)
        );
    }

    /**
     * One ritual mob requirement entry keyed by entity id.
     *
     * <p>In the broader context of the project, this class keeps recipe JSON and ritual runtime
     * requirements aligned through a stable intermediate representation.
     */
    public record MobRequirementData(
            ResourceLocation entity,
            int count
    ) {
        public static final Codec<MobRequirementData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("entity").forGetter(MobRequirementData::entity),
                        Codec.INT.optionalFieldOf("count", 1).forGetter(MobRequirementData::count)
                ).apply(instance, MobRequirementData::new)
        );
    }

    /**
     * Ritual output entry with item id and count.
     *
     * <p>In the broader context of the project, this class decouples external recipe authoring
     * formats from internal item stack construction.
     */
    public record OutputData(
            ResourceLocation item,
            int count
    ) {
        public static final Codec<OutputData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("item").forGetter(OutputData::item),
                        Codec.INT.optionalFieldOf("count", 1).forGetter(OutputData::count)
                ).apply(instance, OutputData::new)
        );
    }

    // Converts this serializer-backed recipe into the runtime ritual model
    public RitualRecipe toRuntime(ResourceLocation id) {
        List<RitualRecipe.ItemRequirement> runtimeItems = new ArrayList<>();
        for (ItemRequirementData req : items) {
            boolean hasItem = req.item().isPresent();
            boolean hasTag = req.tag().isPresent();
            if (hasItem == hasTag) {
                throw new IllegalArgumentException("Recipe " + id + " item requirement must define exactly one of 'item' or 'tag'");
            }

            int count = Math.max(1, req.count());

            if (hasItem) {
                Item itemValue = BuiltInRegistries.ITEM.get(req.item().get());
                if (itemValue == null || itemValue == net.minecraft.world.item.Items.AIR) {
                    throw new IllegalArgumentException("Recipe " + id + " references unknown item " + req.item().get());
                }
                runtimeItems.add(new RitualRecipe.ItemRequirement(itemValue, null, count));
            } else {
                TagKey<Item> tagValue = TagKey.create(net.minecraft.core.registries.Registries.ITEM, req.tag().get());
                runtimeItems.add(new RitualRecipe.ItemRequirement(null, tagValue, count));
            }
        }

        List<RitualRecipe.MobRequirement> runtimeMobs = new ArrayList<>();
        for (MobRequirementData req : mobs) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(req.entity());
            if (entityType == null) {
                throw new IllegalArgumentException("Recipe " + id + " references unknown entity type " + req.entity());
            }
            runtimeMobs.add(new RitualRecipe.MobRequirement(entityType, Math.max(1, req.count())));
        }

        Item outputItem = BuiltInRegistries.ITEM.get(output.item());
        if (outputItem == null || outputItem == net.minecraft.world.item.Items.AIR) {
            throw new IllegalArgumentException("Recipe " + id + " references unknown output item " + output.item());
        }

        ItemStack outputStack = new ItemStack(outputItem, Math.max(1, output.count()));
        return new RitualRecipe(
                id,
                List.copyOf(runtimeItems),
                List.copyOf(runtimeMobs),
                Math.max(0.0F, mobDamagePerRequiredMob),
                Math.max(1, durationSeconds),
                outputStack
        );
    }

    // Returns false because ritual matching is handled by Shroud systems
    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    // Returns empty output because this recipe does not assemble via vanilla grids
    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    // Returns false because this is not a grid recipe
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    // Returns output item for recipe book and integrations
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        Item itemValue = BuiltInRegistries.ITEM.get(output.item());
        if (itemValue == null || itemValue == net.minecraft.world.item.Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(itemValue, Math.max(1, output.count()));
    }

    // Returns ritual serializer
    @Override
    public RecipeSerializer<?> getSerializer() {
        return RitualRecipeRegistries.RITUAL_SERIALIZER.get();
    }

    // Returns ritual recipe type
    @Override
    public RecipeType<?> getType() {
        return RitualRecipeRegistries.RITUAL_TYPE.get();
    }

    // Marks this as special to avoid vanilla crafting behavior
    @Override
    public boolean isSpecial() {
        return true;
    }
}