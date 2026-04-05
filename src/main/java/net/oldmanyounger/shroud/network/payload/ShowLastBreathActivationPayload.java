package net.oldmanyounger.shroud.network.payload;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.item.ModItems;

/**
 * Clientbound payload that triggers the Last Breath activation visual.
 *
 * <p>This payload carries no data and exists solely to signal the client to display
 * the item activation animation for the Totem of Last Breath.
 *
 * <p>In the broader context of the project, this class is part of Shroud's network
 * presentation layer that synchronizes server-side gameplay events with client-side
 * visual feedback.
 */
public record ShowLastBreathActivationPayload() implements CustomPacketPayload {

    // Packet payload type identifier
    public static final Type<ShowLastBreathActivationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "show_last_breath_activation"));

    // Codec for encoding and decoding this unit payload
    public static final StreamCodec<RegistryFriendlyByteBuf, ShowLastBreathActivationPayload> STREAM_CODEC =
            StreamCodec.unit(new ShowLastBreathActivationPayload());

    // Returns this payload's network type token
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Handles payload on the client and displays the activation item animation
    public static void handle(ShowLastBreathActivationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.gameRenderer != null) {
                minecraft.gameRenderer.displayItemActivation(new ItemStack(ModItems.TOTEM_OF_LAST_BREATH.get()));
            }
        });
    }
}