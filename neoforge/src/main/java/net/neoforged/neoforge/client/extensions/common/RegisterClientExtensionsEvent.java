/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions.common;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

// Crelia: server-side stub of a NeoForge client-only type. The real class lives in NeoForge's
// client source, which a Folia/Paper (server-only) base does not have. Mods reference it from their
// client handlers; on a dedicated server it is either never loaded (Dist.CLIENT handlers) or only
// resolved as a bare type during subscriber registration, so an empty shell is enough. Never used at
// runtime on a server, so no behaviour and zero cost.
public class RegisterClientExtensionsEvent extends Event implements IModBusEvent {
}
