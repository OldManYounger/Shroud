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

public record ShowLastBreathActivationPayload() implements CustomPacketPayload {
    public static final Type<ShowLastBreathActivationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "show_last_breath_activation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShowLastBreathActivationPayload> STREAM_CODEC =
            StreamCodec.unit(new ShowLastBreathActivationPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ShowLastBreathActivationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.gameRenderer != null) {
                minecraft.gameRenderer.displayItemActivation(new ItemStack(ModItems.TOTEM_OF_LAST_BREATH.get()));
            }
        });
    }
}