/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.network.event;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.ApiStatus;

// Crelia: server-side stub of NeoForge's client-only event. The real class lives in NeoForge's
// client source tree, which a Folia/Paper (server-only) base does not have. Mods such as GlitchCore
// reference this class while wiring networking for both sides; on a dedicated server the listener
// registers but the event never fires, so registration here is a harmless no-op. The skeleton is
// copied from the sibling net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent.
public class RegisterClientPayloadHandlersEvent extends Event implements IModBusEvent {
    @ApiStatus.Internal
    public RegisterClientPayloadHandlersEvent() {}

    public <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
        // Crelia: client payload handlers are never used on a server-only build
    }
}
