/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.brigadier.CommandDispatcher;
import net.neoforged.bus.api.Event;

// Crelia: server-side stub of NeoForge's client command event.
public class RegisterClientCommandsEvent extends Event {
    public CommandDispatcher<?> getDispatcher() {
        return new CommandDispatcher<>();
    }
}
