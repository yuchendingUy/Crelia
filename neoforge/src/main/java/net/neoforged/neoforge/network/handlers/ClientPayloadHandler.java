/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handlers;

import net.neoforged.neoforge.network.handling.IPayloadContext;

// Crelia: server-side stub of NeoForge's client payload handler.
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static <T> void handle(T payload, IPayloadContext context) {}
}
