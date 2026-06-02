/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// Crelia: server-side stub of NeoForge's client-only ClientPacketDistributor. A dedicated
// (Folia/Paper) server never sends client->server packets, so sendToServer is a no-op here.
// Style copied from the sibling net.neoforged.neoforge.network.PacketDistributor.
public final class ClientPacketDistributor {
    private ClientPacketDistributor() {}

    public static void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        // Crelia: no client->server send on a server-only build
    }
}
