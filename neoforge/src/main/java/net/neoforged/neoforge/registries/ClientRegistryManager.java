/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsPayload;
import net.neoforged.neoforge.network.payload.RegistryDataMapSyncPayload;

// Crelia: server-side stub of NeoForge's client registry sync handler.
public final class ClientRegistryManager {
    private ClientRegistryManager() {}

    public static <R> void handleDataMapSync(final RegistryDataMapSyncPayload<R> payload, final IPayloadContext context) {}

    public static void handleKnownDataMaps(final KnownRegistryDataMapsPayload payload, final IPayloadContext context) {}
}
