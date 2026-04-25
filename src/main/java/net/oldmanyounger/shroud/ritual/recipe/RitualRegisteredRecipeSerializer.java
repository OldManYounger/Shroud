package net.oldmanyounger.shroud.ritual.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

/**
 * Serializer for `shroud:ritual` recipes using explicit ritual field codecs.
 *
 * <p>This serializer decodes recipe JSON authored by datapacks and KubeJS `event.custom`,
 * then keeps packet sync consistent by round-tripping recipe payload as JSON text.
 *
 * <p>In the broader context of the project, this class enables ritual recipes to participate
 * in the same recipe registration and loading workflow used by standard modded recipe types.
 */
public class RitualRegisteredRecipeSerializer implements RecipeSerializer<RitualRegisteredRecipe> {

    // ==================================
    //  FIELDS
    // ==================================

    // Map codec for datapack json decoding
    private static final MapCodec<RitualRegisteredRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    RitualRegisteredRecipe.ItemRequirementData.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(RitualRegisteredRecipe::items),
                    RitualRegisteredRecipe.MobRequirementData.CODEC.listOf().optionalFieldOf("mobs", List.of()).forGetter(RitualRegisteredRecipe::mobs),
                    Codec.FLOAT.optionalFieldOf("mob_damage", 2.0F).forGetter(RitualRegisteredRecipe::mobDamagePerRequiredMob),
                    Codec.INT.optionalFieldOf("duration_seconds", 6).forGetter(RitualRegisteredRecipe::durationSeconds),
                    RitualRegisteredRecipe.OutputData.CODEC.fieldOf("output").forGetter(RitualRegisteredRecipe::output)
            ).apply(instance, RitualRegisteredRecipe::new)
    );

    // Stream codec for packet sync
    private static final StreamCodec<RegistryFriendlyByteBuf, RitualRegisteredRecipe> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public void encode(RegistryFriendlyByteBuf buffer, RitualRegisteredRecipe value) {
                    JsonElement encoded = RitualRegisteredRecipe.CODEC.encodeStart(JsonOps.INSTANCE, value)
                            .getOrThrow(error -> new IllegalStateException("Failed to encode ritual recipe: " + error));
                    buffer.writeUtf(encoded.toString());
                }

                @Override
                public RitualRegisteredRecipe decode(RegistryFriendlyByteBuf buffer) {
                    String raw = buffer.readUtf();
                    JsonElement element = JsonParser.parseString(raw);
                    return RitualRegisteredRecipe.CODEC.parse(JsonOps.INSTANCE, element)
                            .getOrThrow(error -> new IllegalStateException("Failed to decode ritual recipe: " + error));
                }
            };

    // Returns json map codec
    @Override
    public MapCodec<RitualRegisteredRecipe> codec() {
        return CODEC;
    }

    // Returns packet stream codec
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RitualRegisteredRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}