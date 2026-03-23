package net.oldmanyounger.shroud.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.network.payload.ShowLastBreathActivationPayload;

@EventBusSubscriber(modid = Shroud.MOD_ID)
public final class ModPayloads {
    private ModPayloads() {
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                ShowLastBreathActivationPayload.TYPE,
                ShowLastBreathActivationPayload.STREAM_CODEC,
                ShowLastBreathActivationPayload::handle
        );
    }
}