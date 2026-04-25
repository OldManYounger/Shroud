package net.oldmanyounger.shroud.ritual.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Minimal serializer for the registered ritual bridge recipe type.
 *
 * <p>This serializer provides a no-payload network codec so recipe sync packets
 * can encode and decode ritual bridge recipes without requiring per-recipe
 * binary fields.
 *
 * <p>In the broader context of the project, this class keeps `shroud:ritual`
 * registry compatibility stable for datapacks and KubeJS integrations while
 * ritual execution remains managed by Shroud's dedicated ritual systems.
 */
public class RitualRegisteredRecipeSerializer implements RecipeSerializer<RitualRegisteredRecipe> {

    // ==================================
    //  FIELDS
    // ==================================

    // Codec used for datapack json loading
    private static final MapCodec<RitualRegisteredRecipe> CODEC =
            MapCodec.unit(RitualRegisteredRecipe::new);

    // Stream codec used for recipe sync packets
    private static final StreamCodec<RegistryFriendlyByteBuf, RitualRegisteredRecipe> STREAM_CODEC =
            new StreamCodec<>() {

                // Encodes no payload for the bridge recipe
                @Override
                public void encode(RegistryFriendlyByteBuf buffer, RitualRegisteredRecipe value) {
                    // No-op by design
                }

                // Decodes a fresh bridge recipe instance
                @Override
                public RitualRegisteredRecipe decode(RegistryFriendlyByteBuf buffer) {
                    return new RitualRegisteredRecipe();
                }
            };

    // Returns the json codec
    @Override
    public MapCodec<RitualRegisteredRecipe> codec() {
        return CODEC;
    }

    // Returns the packet stream codec
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RitualRegisteredRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}