package net.oldmanyounger.shroud.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.network.payload.ShowLastBreathActivationPayload;

/**
 * Registers custom network payload handlers for Shroud.
 *
 * <p>This class subscribes to payload registration events and binds packet types,
 * codecs, and handlers to a protocol versioned registrar.
 *
 * <p>In the broader context of the project, this class is part of Shroud's network
 * bootstrap layer that enables synchronized client-server behavior for custom
 * gameplay and visual systems.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public final class ModPayloads {

    // Prevents instantiation of this static registration class
    private ModPayloads() {
    }

    // Registers payload types and handlers for the current network protocol version
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