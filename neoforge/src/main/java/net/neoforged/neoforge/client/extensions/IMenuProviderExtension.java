/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

// Crelia: server-side stub of NeoForge's client menu provider extension.
public interface IMenuProviderExtension {
    default void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {}
}
